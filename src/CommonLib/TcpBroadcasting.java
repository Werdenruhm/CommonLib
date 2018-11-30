package CommonLib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * 
 */
public class TcpBroadcasting {
    final String looseHalfComment;
    final long earlySocketWritePeriodSeconds;
    final ServerSocket serverSocket;
    final Thread daemonAcceptingThread;
    final Thread daemonTransmittingThread;
    public TcpBroadcasting(int Port, String looseHalfComment, int earlySocketWritePeriodSeconds) throws IOException
    {
        this.looseHalfComment = looseHalfComment;
        this.earlySocketWritePeriodSeconds = earlySocketWritePeriodSeconds;
        serverSocket = new ServerSocket(Port);
        daemonAcceptingThread = new Thread(new TcpBroadcastingAcceptingThread(this));
        daemonAcceptingThread.setDaemon(true);
        daemonAcceptingThread.setName("Thread for TcpBroadcasting (Accepting connections), port " + Port);
        daemonTransmittingThread = new Thread(new TcpBroadcastingTransmittingThread(this));
        daemonTransmittingThread.setDaemon(true);
        daemonTransmittingThread.setName("Thread for TcpBroadcasting (Transmitting data to clients), port " + Port);
        Common.setTimeout(()->{
            daemonAcceptingThread.start();
            daemonTransmittingThread.start();
        }, 100);
    }
    final Object connectedSocketsLOCK = new Object();
    final ArrayList<Socket> connectedSockets = new ArrayList<>();
    volatile long firstSocketWrite_ts = 0;
    boolean isEarlySocketWritePeriod()
    {
        return earlySocketWritePeriodBuffer != null && (firstSocketWrite_ts == 0 || (System.currentTimeMillis() - firstSocketWrite_ts) < (earlySocketWritePeriodSeconds * 1000));
    }
    ByteArrayOutputStream earlySocketWritePeriodBuffer = new ByteArrayOutputStream();
    static class TcpBroadcastingAcceptingThread implements Runnable
    {
        final TcpBroadcasting parent;
        TcpBroadcastingAcceptingThread(TcpBroadcasting parent)
        {
            this.parent = parent;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                while (true)
                {
                    Socket connectionSocket = parent.serverSocket.accept();
                    synchronized(parent.connectedSocketsLOCK)
                    {
                        if (parent.isEarlySocketWritePeriod())
                        {
                            byte[] a = parent.earlySocketWritePeriodBuffer.toByteArray();
                            connectionSocket.getOutputStream().write(a, 0, a.length);
                        }
                        else if (parent.earlySocketWritePeriodBuffer != null)
                            parent.earlySocketWritePeriodBuffer = null;
                        parent.connectedSockets.add(connectionSocket);
                    }
                    Thread.sleep(100);
                }
            } catch (Exception ex) {throw new RuntimeException(ex);}
        }            
    }

    final ByteBuffer WritePriQueue = ByteBuffer.allocate(1000000);
    final Object WritePriQueueLOCK = new Object();
    final Object WritePriQueueLOCK2 = new Object();

    static class TcpBroadcastingTransmittingThread implements Runnable
    {
        final TcpBroadcasting parent;
        TcpBroadcastingTransmittingThread(TcpBroadcasting parent)
        {
            this.parent = parent;
        }
        final ByteBuffer WriteSecQueue = ByteBuffer.allocate(1000000);
        @Override
        public void run() {
            try {
                Thread.sleep(500);
                boolean wasConnected = false;
                while (true)
                {
                    boolean fastCycle = false;
                    if (wasConnected || parent.connectedSockets.size() > 0)
                    {
                        wasConnected = true;
                        if (parent.WritePriQueue.position() > 0)
                        {
                            if (WriteSecQueue.position() == 0)
                            {
                                synchronized(parent.WritePriQueueLOCK)
                                {
                                    WriteSecQueue.put(parent.WritePriQueue.array(), 0, parent.WritePriQueue.position());
                                    parent.WritePriQueue.position(0); 
                                }
                            }
                            else
                                fastCycle = true;
                        }
                        if (WriteSecQueue.position() > 0)
                        {
                            synchronized(parent.connectedSocketsLOCK)
                            {
                                if (parent.firstSocketWrite_ts == 0)
                                    parent.firstSocketWrite_ts = System.currentTimeMillis();
                                if (parent.isEarlySocketWritePeriod())
                                    parent.earlySocketWritePeriodBuffer.write(WriteSecQueue.array(), 0, WriteSecQueue.position());
                                else if (parent.earlySocketWritePeriodBuffer != null)
                                    parent.earlySocketWritePeriodBuffer = null;
                                
                                boolean whereDone = false;
                                for(int n = parent.connectedSockets.size() - 1; n >= 0; n--)
                                {
                                    Socket sckt = parent.connectedSockets.get(n);
                                    try 
                                    {
                                        sckt.getOutputStream().write(WriteSecQueue.array(), 0, WriteSecQueue.position());
                                        whereDone = true;
                                    }
                                    catch (Exception ex) 
                                    {
                                        try {
                                            sckt.close();
                                        }
                                        catch (Exception ex2) {
                                        }                                            
                                        parent.connectedSockets.remove(n);
                                    }
                                }
                                //if (!whereDone && parent.ifNoWhereToWrite != null)
                                    //parent.ifNoWhereToWrite.call(Arrays.copyOf(WriteSecQueue.array(), WriteSecQueue.position()));
                            }
                            WriteSecQueue.position(0);
                        }

                        Thread.sleep(10);
                        if (parent.WritePriQueue.position() > 0)
                            fastCycle = true;
                        if (!fastCycle)
                            Thread.sleep(200);
                    }
                    Thread.sleep(100);
                }
            } catch (Exception ex) {throw new RuntimeException(ex);}
        }            
    }
    Common.Action1<byte[]> ifNoWhereToWrite;
    public void Write(byte[] data)
    {
        synchronized(WritePriQueueLOCK2)
        {
            if (WritePriQueue.position() > WritePriQueue.limit() / 2)
                try { Thread.sleep(1); } catch (Throwable ex) { }

            if (data.length > WritePriQueue.limit())
            {
                int dataReadOffset = 0;                
                for(int n = 0; true; n++)
                {
                    WritePriQueue.position(WritePriQueue.position()/2);//loose half
                    try {
                        WritePriQueue.put(("\r\n...\r\n[" + Common.NowToString() + "] " + looseHalfComment + "\r\n...\r\n").getBytes("utf-8"));
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }

                    try { Thread.sleep(10); } catch (Throwable ex) { }

                    synchronized(WritePriQueueLOCK)
                    {
                        int WPQavailable = WritePriQueue.limit() - WritePriQueue.position();
                        int dataLeft = data.length - dataReadOffset;   
                        if (WPQavailable > 0)
                        {
                            int WPQtoWrite = dataLeft < WPQavailable ? dataLeft : WPQavailable;
                            System.arraycopy(data, dataReadOffset, WritePriQueue.array(), WritePriQueue.position(), WPQtoWrite);
                            WritePriQueue.position(WritePriQueue.position() + WPQtoWrite);
                            dataReadOffset += WPQtoWrite;
                            if (data.length == dataReadOffset)
                                break;
                        }
                    }
                }
            }
            else
            {
                if (data.length > WritePriQueue.limit() - WritePriQueue.position())
                {
                    WritePriQueue.position(WritePriQueue.position()/2);//loose half
                    WritePriQueue.put("...\r\n\r\n...\r\n".getBytes());

                    try { Thread.sleep(10); } catch (Throwable ex) { }
                }
                synchronized(WritePriQueueLOCK)
                {
                    WritePriQueue.put(data);
                }
            }
        }
    }
}
