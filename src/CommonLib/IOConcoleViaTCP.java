package CommonLib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;

/**
 *
 * 
 */
public class IOConcoleViaTCP extends TcpBroadcasting {
    final ReadingErrAndOutThread readingErrAndOutThread;
    final WritingInThread writingInThread;
    public IOConcoleViaTCP(int Port) throws IOException
    {
        super(Port, "", 1);
        Thread th1 = new Thread(readingErrAndOutThread = new ReadingErrAndOutThread(this));        
        th1.setDaemon(true);
        th1.setName("Thread for IOConcoleViaTCP (Reading System.err and System.out), port " + Port);
        Thread th2 = new Thread(writingInThread = new WritingInThread(this));        
        th2.setDaemon(true);
        th2.setName("Thread for IOConcoleViaTCP (Writing System.in), port " + Port);
        Common.setTimeout(()->{
            th1.start();
            th2.start();
        }, 100);
    }
    static class ReadingErrAndOutThread implements Runnable
    {
        final IOConcoleViaTCP parent;
        ReadingErrAndOutThread(IOConcoleViaTCP parent)
        {
            this.parent = parent;
        }
        final ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        final PrintStream psOut = new PrintStream(baosOut);
        PrintStream oldOut;
        final ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        final PrintStream psErr = new PrintStream(baosErr);
        PrintStream oldErr;
        volatile boolean errAndOutRedirected;
        @Override
        public void run() {
            try 
            {
                while (true)
                {
                    if (!errAndOutRedirected && !parent.connectedSockets.isEmpty())
                    {
                        oldOut = System.out;
                        System.setOut(psOut);
                        oldErr = System.err;
                        System.setErr(psErr);
                        errAndOutRedirected = true;
                    }
                    if (errAndOutRedirected && parent.connectedSockets.isEmpty())
                    {
                        System.setOut(oldOut);
                        System.setErr(oldErr);
                        errAndOutRedirected = false;
                    }
                    if (errAndOutRedirected)
                    {
                        synchronized(psOut)
                        {
                            baosOut.flush();
                            byte[] b = baosOut.toByteArray();
                            if (b != null && b.length > 0)
                            {
                                //old.write(0x3e);
                                oldOut.write(b, 0, b.length);
                                try
                                {
                                    parent.Write(new String(b, "utf-8").getBytes("utf-8"));
                                }
                                catch (Exception ex)
                                {
                                    parent.Write(b);
                                    parent.Write(ex.getMessage().getBytes());
                                }
                                baosOut.reset();
                            }
                        }
                        synchronized(psErr)
                        {
                            baosErr.flush();
                            byte[] b = baosErr.toByteArray();
                            if (b != null && b.length > 0)
                            {
                                //old.write(0x3e);
                                oldErr.write(b, 0, b.length);
                                try
                                {
                                    parent.Write(("Err:" + new String(b, "utf-8")).getBytes("utf-8"));
                                }
                                catch (Exception ex)
                                {
                                    parent.Write(b);
                                    parent.Write(ex.getMessage().getBytes());
                                }
                                baosErr.reset();
                            }
                        }
                    }
                    Thread.sleep(100);
                }
            } 
            catch (Exception ex) 
            {
                oldErr.println("ReadingErrAndOutThread exception: " + Common.throwableToString(ex, Common.getCurrentSTE()));
            }
            finally
            {
                System.setOut(oldOut);
                System.setErr(oldErr);
            }
        }            
    }        
    static class WritingInThread implements Runnable
    {
        final IOConcoleViaTCP parent;
        WritingInThread(IOConcoleViaTCP parent)
        {
            this.parent = parent;
        }
        final ByteArrayInputStreamEx baiseIn = new ByteArrayInputStreamEx();
        final byte[] readbuffer = new byte[2048]; 
        InputStream oldIn;
        volatile boolean inRedirected;
        @Override
        public void run() 
        {
            try 
            {
                while (true)
                {
                    if (!inRedirected && !parent.connectedSockets.isEmpty())
                    {
                        oldIn = System.in;
                        System.setIn(baiseIn);
                        inRedirected = true;
                    }
                    if (inRedirected && parent.connectedSockets.isEmpty())
                    {
                        System.setIn(oldIn);
                        inRedirected = false;
                    }
                    if (inRedirected)
                    {
                        synchronized(parent.connectedSocketsLOCK)
                        {
                            for(int n = parent.connectedSockets.size() - 1; n >= 0; n--)
                            {
                                Socket socket = parent.connectedSockets.get(n);
                                try {
                                    if (socket.getInputStream().available() > 0)
                                    {
                                        int read = socket.getInputStream().read(readbuffer);
                                        if (read > 0)
                                        {
                                            baiseIn.write(Arrays.copyOf(readbuffer, read));
                                        }
                                    }
                                }
                                catch (Exception ex) 
                                {
                                    if (Common.IsDebug())
                                        System.out.println("WritingInThread socket exception: " + Common.throwableToString(ex, Common.getCurrentSTE()));
                                }
                            }
                        }
                    }                    
                    Thread.sleep(100);
                }
            } 
            catch (Exception ex) 
            {
                System.out.println("WritingInThread exception: " + Common.throwableToString(ex, Common.getCurrentSTE()));
            }
            finally
            {
                System.setIn(oldIn);
            }
        }            
    }       
    public static class ByteArrayInputStreamEx extends java.io.ByteArrayInputStream 
    {
        public ByteArrayInputStreamEx()
        {
            super(new byte[8192]);
            pos = buf.length;
        }
        public synchronized void write(byte[] src)
        {
            if (pos < buf.length)
            {
                System.arraycopy(buf, pos, buf, pos - src.length, buf.length - pos);
            }
            pos -= src.length;
            System.arraycopy(src, 0, buf, buf.length - src.length, src.length);
        }                
    }    
}
