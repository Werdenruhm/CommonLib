package CommonLib;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 *
 *  
 */
public class Common {
    private static Boolean isWin;
    public static boolean IsWin()
    {
        if (isWin == null) isWin = System.getProperty("os.name").toLowerCase().contains("windows");
        return isWin;
    }
    private static String fileSeparator; 
    public static String getFileSeparator()
    {
        if (fileSeparator == null) fileSeparator = System.getProperty("file.separator"); 
        if (fileSeparator == null) fileSeparator = (isWin ? "\\" : "/");
        return fileSeparator;
    }
    public static String SlashOnEnd(String value)
    {
        if (value != null && value.length() > 0 && !value.endsWith(getFileSeparator()))
            value += fileSeparator;
        return value;
    }
    public static String NoSlashOnEnd(String value)
    {
        while (value.endsWith(getFileSeparator()))
            value = value.substring(0, value.length()-1);
        return value;
    }
    public static String ReplaceBadPathChars(String s, String stringToReplaceWith)
    {
        String[] badchars = new String[] { "\\", "/", ":", "*", "?", "\"", "<", ">", "|" };
        for(String badchar : badchars)
        {
            s = s.replace(badchar, stringToReplaceWith);
        }
        return s;
    }
    private static String pathSeparator; 
    public static String getPathSeparator()
    {
        if (pathSeparator == null) pathSeparator = System.getProperty("path.separator"); 
        if (pathSeparator == null) pathSeparator = (isWin ? ";" : ":");
        return pathSeparator;
    }    
    private static String newLine; 
    public static String NewLine()
    {
        if (newLine == null) newLine = System.getProperty("line.separator"); 
        if (newLine == null) newLine = (isWin ? "\r\n" : "\n");
        return newLine;
    }  
    public static String br()
    {
        return NewLine();
    }         
    public static String br(int quantity)
    {
        if (quantity <= 0) return null;
        if (quantity == 1) return br();
        String s = br();
        for (int n = 1; n < quantity; n++)
            s += br();
        return s;
    }         
    
    public static String StringPadLeft(String value, char chr, int length)
    {
        if (value == null)
            return null;
        char[] result = new char[length];
        int frst = length - value.length();
        for (int n = 0; n < length; n++)
            if (n >= frst)
                result[n] = value.charAt(n - frst);
            else
                result[n] = chr;            
        return new String(result);
    } 
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    public static String WriteTempDatedLog(boolean Async, String Logname, String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags)
    {
        return WriteLog(Async, Logname, true, true, null, Method, Msg, AddTags, false);
    }
    public static String WriteTempLog(boolean Async, String Logname, String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags)
    {
        return WriteLog(Async, Logname, false, true, null, Method, Msg, AddTags, false);
    }
    public static String WriteLog(boolean Async, String Logname, boolean Dated, boolean InTemp, String LogPath, String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags)
    {
        return WriteLog(Async, Logname, Dated, InTemp, LogPath, Method, Msg, AddTags, false);
    }
    public static String[][] StringSimpleEntryArrayList2Array(ArrayList<AbstractMap.SimpleEntry<String, String>> v)
    {
        String[][] result = null; 
        if (v!=null)
        {
            result = new String[v.size()][];
            for(int n = 0; n < result.length; n++)
                if (v.get(n) != null)
                    result[n] = new String[] { v.get(n).getKey(), v.get(n).getValue() };
        }
        return result;
    }
    public static String WriteLog(boolean Async, String Logname, boolean Dated, boolean InTemp, String LogPath, String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags, boolean AsyncWait)
    {
        return WriteLog(Async, Logname, Dated, InTemp, new LogPathInitInfo[] { new LogPathInitInfo(LogPath) }, Method, Msg, StringSimpleEntryArrayList2Array(AddTags), AsyncWait);
    }
    public static String WriteLog(boolean Async, String Logname, boolean Dated, boolean InTemp, LogPathInitInfo[] LPIs, String Method, String Msg, String[][] AddTags, boolean AsyncWait)
    {
        return WriteLog(new LogInfo(Async, Logname, Dated, InTemp, LPIs), Method, Msg, AddTags, AsyncWait);
    }
    public static String WriteLog(boolean Async, String FullFilePath, String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags)
    {
        return WriteLog(new LogInfo(Async, FullFilePath), Method, Msg, StringSimpleEntryArrayList2Array(AddTags), false);
    }
    public static String jreID()
    {
        try
        {
            return ManagementFactory.getRuntimeMXBean().getName();
        }
        catch (Exception ex)
        {
            return "-@-";
        }
    }
    public static String PrepareTextForXml(String Value)
    {
        return Value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
    private static final byte[] WriteLog_LogPrefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?><log>".getBytes();
    private static final byte[] WriteLog_LogAppendix = "</log>".getBytes();
    public static String WriteLog(LogInfo LI, String Method, String Msg, String[][] AddTags, boolean AsyncWait)
    {
        if (IsDebug())
        {
            System.out.println(java.time.LocalDateTime.now().toString() + "    " + Msg);
            if (AddTags != null)
                System.out.println(ArrayToString(AddTags, (t)->{ return ArrayToString(t, (tv)->{ return tv; }, "=" ); }, ", "));
        }
        String data = "<le><ts>" + java.time.LocalDateTime.now().toString() + "</ts>"
                + "<jvmID>" + jreID() + "</jvmID>"
                + (Method != null && !Method.isEmpty() ? "<method>" + Method + "</method>\r\n" : "")
                + "<msg>" + PrepareTextForXml(Msg) + "</msg>" ;
        if (AddTags != null && AddTags.length != 0)
        {
            data += "\r\n";
            for (String[] tag : AddTags)
            {
                if (tag != null && tag.length > 1 && tag[0] != null && tag[1] != null)
                    data += "<" + tag[0] + ">" + PrepareTextForXml(tag[1]) + "</" + tag[0] + ">\r\n";
            }
        }
        data += "</le>        " + "\r\n";
        byte[] dataB = data.getBytes(StandardCharsets.UTF_8);
        if (!LI.async)
        {
            String FAILEDmethod = "CommonLib.Common.WriteLog";
            if (FAILEDmethod.equals(Method))
                return "prevent looping";
            else
                return AppendAllBytesFlex(LI.LPIs, dataB, WriteLog_LogPrefix, WriteLog_LogAppendix, FAILEDmethod);
        }
        else
        {
            AsyncWriteLog awl;
            String awlKey = ArrayToString(LI.LPIs, (lpi) -> { 
                return lpi.fullFilePath(); 
            }, ":" );
            if (AsyncWriteLogThreads.containsKey(awlKey))
                awl = AsyncWriteLogThreads.get(awlKey);
            else
            {
                synchronized (AsyncWriteLogThreadsLOCK)
                {
                    if (AsyncWriteLogThreads.containsKey(awlKey))
                        awl = AsyncWriteLogThreads.get(awlKey);
                    else
                    {
                        awl = new AsyncWriteLog(LI.LPIs, WriteLog_LogPrefix, WriteLog_LogAppendix);
                        AsyncWriteLogThreads.put(awlKey, awl);
                        Thread awlt = new Thread(awl);
                        awlt.setDaemon(true);
                        String s = LI.LPIs != null && LI.LPIs.length > 0 && LI.LPIs[0].logname != null ? LI.LPIs[0].logname : "";
                        awlt.setName("AsyncWriteLog thread - " + s);
                        awlt.start();
                    }
                }
            }
            return awl.Write(dataB, AsyncWait);
        }
    }
    static HashMap<String, AsyncWriteLog> AsyncWriteLogThreads = new HashMap<>();
    static final Object AsyncWriteLogThreadsLOCK = new Object();
    static class AsyncWriteLog implements Runnable
    {
        static boolean stopAllNow = false;
        final LogPathInfo[] LPIs;
        final byte[] LogPrefix;
        final byte[] LogAppendix;
        AsyncWriteLog(LogPathInfo[] LPIs, byte[] LogPrefix, byte[] LogAppendix)
        {
            this.LPIs = LPIs;
            this.LogPrefix = LogPrefix;
            this.LogAppendix = LogAppendix;
        }
        ByteBuffer WritePriQueue = ByteBuffer.allocate(1000000);
        final Object WritePriQueueLOCK = new Object();
        final Object WritePriQueueLOCK2 = new Object();
        ByteBuffer WriteSecQueue = ByteBuffer.allocate(1000000);
        @Override
        public void run() 
        {
            while (!stopAllNow)
            {
                boolean fastCycle = false;
                try
                {
                    if (WritePriQueue.position() > 0)
                    {
                        if (WriteSecQueue.position() == 0)
                        {
                            synchronized(WritePriQueueLOCK)
                            {
                                WriteSecQueue.put(WritePriQueue.array(), 0, WritePriQueue.position());
                                WritePriQueue.position(0); 
                            }
                        }
                        else
                            fastCycle = true;
                    }
                    if (WriteSecQueue.position() > 0)
                    {
                        byte[] dataB = Arrays.copyOf(WriteSecQueue.array(), WriteSecQueue.position());
                        String FAILEDmethod = "CommonLib.Common.AsyncWriteLog.run";
                        
                        lastError = AppendAllBytesFlex(LPIs, dataB, LogPrefix, LogAppendix, FAILEDmethod);
                       
                        if (lastError == null)
                            WriteSecQueue.position(0);
                    }
                    
                    Thread.sleep(30);
                    if (WritePriQueue.position() > 0)
                        fastCycle = true;
                    if (!fastCycle)
                        Thread.sleep(1000);
                }
                catch (Throwable ex)
                {
                    lastError = "Error: " + ex.toString().trim() + "\r\n\r\n" + getGoodStackTrace(ex, 0);
                    WriteLog(false, FAILEDLOGPATH(LPIs), "CommonLib.Common.AsyncWriteLog.run", lastError, null);
                    try { Thread.sleep(1000); } catch (InterruptedException dummy) { }                
                }
            }
        }
        volatile String lastError = "";
        public String Write(byte[] data, boolean AsyncWait)
        {
            try
            {
                synchronized(WritePriQueueLOCK2)
                {
                    if (WritePriQueue.position() > WritePriQueue.limit() / 2)
                        try { Thread.sleep(1); } catch (InterruptedException dummy) { }

                    if (data.length > WritePriQueue.limit())
                    {
                        int dataReadOffset = 0;                
                        for(int n = 0; true; n++)
                        {
                            if (n > 60 * 100)
                                return ("Log write error (" + LogPathInfo.toExString(LPIs) + "): " + lastError);

                            try { Thread.sleep(10); } catch (InterruptedException dummy) { }

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
                            for(int n = 0; data.length > WritePriQueue.limit() - WritePriQueue.position(); n++)
                            {
                                if (n > 28 * 100)
                                    return ("Log write error (" + LogPathInfo.toExString(LPIs) + "): " + lastError);
                                try { Thread.sleep(10); } catch (InterruptedException dummy) { }
                            }
                        }
                        synchronized(WritePriQueueLOCK)
                        {
                            WritePriQueue.put(data);
                        }
                    }
                    if (AsyncWait)
                    {
                        for (int n = 0; WritePriQueue.position() > 0 || WriteSecQueue.position() > 0; n++)
                        {
                            if (n > 28 * 100)
                                return ("Log write awaiting timeout (" + LogPathInfo.toExString(LPIs) + "): " + lastError);
                            try { Thread.sleep(10); } catch (InterruptedException dummy) { }
                        }
                    }
                }
            }
            catch (Throwable th)
            {
                return ("Error: '" + th.toString().trim() + "\r\n\r\n" + getGoodStackTrace(th, 0));
            }
            return null;
        }
    }
    public static String AppendAllBytesFlex(LogPathInfo[] LPIs, byte[] dataB, byte[] prefix, byte[] appendix, String FAILEDmethod)
    {       
        String AppendAllBytesError = null;
        String AppendAllBytesErrors = null;
        for (LogPathInfo lpi : LPIs) 
        {
            if (lpi.readyToWrite())
            {
                AppendAllBytesError = AppendAllBytes(lpi.fullFilePath(), dataB, prefix, appendix);
                if (AppendAllBytesError == null)
                {
                    if (AppendAllBytesErrors != null)
                        AppendAllBytesErrors += "path: " + lpi.fullFilePath() + "\r\n" + "success." + hr;
                    break;
                }
                else
                {
                    lpi.setLastError(AppendAllBytesError);
                    AppendAllBytesErrors = nz(AppendAllBytesErrors) + "path: " + lpi.fullFilePath() + "\r\n" + "error: " + AppendAllBytesError + hr;
                }
            }
            else
            {
                AppendAllBytesErrors = nz(AppendAllBytesErrors) + "path: " + lpi.fullFilePath() + "\r\n" + "skipping due its not ready (" + lpi.millisecToReady() + "ms to ready)." + hr;
            }
        }
        if (AppendAllBytesError != null)
        {
            System.out.println(Common.NowToString() + "    CommonLib.Common.AppendAllBytesFlex() error: log write failed: " + AppendAllBytesError + hr() + "Source message: " + new String(dataB, StandardCharsets.UTF_8));
            AppendAllBytesError = AppendAllBytes(FAILEDLOGPATH(LPIs), dataB, prefix, appendix);
            WriteLog(false, FAILEDLOGPATH(LPIs), FAILEDmethod, "Prev record error: " + AppendAllBytesErrors, null);
        }
        else if (AppendAllBytesErrors != null)
            WriteLog(false, FAILEDLOGPATH(LPIs), FAILEDmethod, "Log error: " + AppendAllBytesErrors, null);
        
        return AppendAllBytesError;
    }
    private final static long AppendAllBytes_timeout_sec = 10;
    private final static long AppendAllBytes_timeout_trys = 4;
    public static String AppendAllBytes(String filepath, byte[] data, byte[] prefix, byte[] appendix)
    {
        long ts = System.currentTimeMillis();
        if (data == null || data.length == 0)
            return null;
        String lastError = null;
        for (int n = 0; true; n++)
        {
            try {
                Path p = Paths.get(filepath);
                try (FileChannel fc = FileChannel.open(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
                {
                    java.nio.channels.FileLock fclock = fc.tryLock();
                    if (fclock == null)
                        throw new IOException("lock could not be acquired because another program holds an overlapping lock");
                    try {
                        if (fc.size() > 0)
                            fc.position(fc.size() - (appendix != null && appendix.length > 0 ? appendix.length : 0));
                        else
                            if (prefix != null && prefix.length > 0)
                                fc.write(ByteBuffer.wrap(prefix));
                        fc.write(ByteBuffer.wrap(data));
                        if (appendix != null && appendix.length > 0)
                            fc.write(ByteBuffer.wrap(appendix));
                        fc.force(true);
                    } finally {
                        fclock.release();
                    }
                }
                return null;
            } catch (Throwable ex) {
                String errorCreateDir = null;
                if (ex instanceof NoSuchFileException || ex instanceof FileNotFoundException)
                {
                    if (!Files.isDirectory(Paths.get(filepath).getParent()))
                    {
                        try {
                            Files.createDirectories(Paths.get(filepath).getParent());
                            continue;
                        } catch (IOException ex1) {
                            errorCreateDir = ex1.toString();
                        }
                    }
                }
                lastError = "AppendAllBytes: " + ex.toString().trim() + hr + getGoodStackTrace(ex, 0);
                if (errorCreateDir != null)
                    lastError += hr + "Error creating folder '" + Paths.get(filepath).getParent() + "': " + errorCreateDir;
                if (
                    ((ts + (AppendAllBytes_timeout_sec * 1000)) < System.currentTimeMillis() && n > 0) //timeout, but at least one retry occured
                    || (n+1) >= AppendAllBytes_timeout_trys // AppendAllBytes_timeout_trys unsuccessful tries
                    || !(ex instanceof IOException) 
                    || ex instanceof NoSuchFileException || ex instanceof FileNotFoundException
                )
                {
                    lastError += hr + "break (after " + (System.currentTimeMillis() - ts) + "ms and " + (n+1) + " trys):"
                        + (    ((ts + (AppendAllBytes_timeout_sec * 1000)) < System.currentTimeMillis() && n > 0)     ? " timeout." : "")
                        + (    (n+1) >= AppendAllBytes_timeout_trys                                                   ? " trys." : "")
                        + (    !(ex instanceof IOException)                                                           ? " is not IOException." : "")
                        + (    ex instanceof NoSuchFileException || ex instanceof FileNotFoundException               ? " is NoSuchFileException." : "")
                    ;
                    break;
                }
                try { Thread.sleep(100); } catch (InterruptedException iex) { 
                    lastError += hr + "break (after " + (System.currentTimeMillis() - ts) + "ms): sleep Interrupted.";
                    break; 
                }
            }
        }
        return lastError;
    }
    static String FAILEDLOGPATH(LogPathInfo[] LPIs)
    {
        return FAILEDLOGPATH(ArrayToString(LPIs, (lpi) -> { 
            return lpi.fullFilePath(); 
        }, "___" ));
    }
    static String FAILEDLOGPATH(String orig_filepath)
    {
        return SlashOnEnd(SlashOnEnd(System.getProperty("user.home")) + "java_CommonLib_FAILEDLOGS") + "FAILED__" + ReplaceBadPathChars(orig_filepath, "_");
    }

    private static String tmpdir = null; 
    public static String getTmpdir()
    {
        if (tmpdir != null) 
            return tmpdir;
        String[] tmps = new String[] { 
            System.getProperty("java.io.tmpdir"),
            System.getenv().get("temp"),
            System.getenv().get("TEMP"),
            System.getenv().get("%TEMP%"),
            System.getenv().get("%temp%"),
            System.getenv().get("tmp"),
            System.getenv().get("TMP"),
            System.getenv().get("%TMP%"),
            System.getenv().get("%tmp%")
        };
        for (String tmp : tmps)
            if (tmp != null && !tmp.equals(""))
                return tmpdir = SlashOnEnd(tmp);
        return tmpdir = SlashOnEnd(SlashOnEnd(System.getProperty("user.home")) + "temptmp");//Failed to get TEMP
    }
    public static class LogInfo
    {
        private final LogPathInfo[] LPIs;
        public final boolean async;
        public LogInfo(boolean async, String logname, boolean dated, LogPathInitInfo[] LPIIs)
        {
            this(async, logname, dated, false, LPIIs);
        }
        LogInfo(boolean async, String logname, boolean dated, boolean inTemp, LogPathInitInfo[] LPIIs)
        {
            if (stringIsNullOrEmpty(logname) || ReplaceBadPathChars(logname, "").equals(""))
                throw new NullPointerException("MUSTNEVERTHROW: Logname required!"); 
            if (LPIIs == null || LPIIs.length == 0)
                throw new NullPointerException("MUSTNEVERTHROW: LPIIs == null || LPIIs.length == 0");
            LPIs = new LogPathInfo[LPIIs.length];
            for (int n = 0; n < LPIIs.length; n++)
            {                
                LPIs[n] = new LogPathInfo(logname, dated, inTemp, LPIIs[n].logPath, LPIIs[n].timeOutOnError_sec);
            }
            this.async = async;
        }
        LogInfo(boolean async, String fullFilePath)
        {
            this(async, fullFilePath, logPathInitInfoTimeOutOnError_sec);
        }
        LogInfo(boolean async, String fullFilePath, int timeOutOnError_sec)
        {
            LPIs = new LogPathInfo[1];
            LPIs[0] = new LogPathInfo(fullFilePath, timeOutOnError_sec);
            this.async = async;
        }        
        public int getLPIs_length()
        {
            return LPIs.length;
        }
        public LogPathInfo getLPI(int n)
        {
            return LPIs[n];
        }
    }
    static final int logPathInitInfoTimeOutOnError_sec = 5;
    public static class LogPathInitInfo
    {
        protected final String logPath;
        protected final int timeOutOnError_sec;
        public LogPathInitInfo(String logPath) { this(logPath, logPathInitInfoTimeOutOnError_sec); }
        public LogPathInitInfo(String logPath, int timeOutOnError_sec)
        {
            if (timeOutOnError_sec < 0 || timeOutOnError_sec > 1800)
                throw new Error("MUSTNEVERTHROW: TimeOutOnError_sec must be between 0 and 1800!");
            this.logPath = logPath; 
            this.timeOutOnError_sec = timeOutOnError_sec;
        }
        
        @Override
        public boolean equals(Object thatO)
        {
            if (thatO == null || !(thatO instanceof LogPathInitInfo)) return false;
            LogPathInitInfo that = (LogPathInitInfo)thatO;
            return 
                (
                    (this.logPath == null && that.logPath == null)
                    ||
                    (this.logPath != null && this.logPath.equals(that.logPath))
                )
                && this.timeOutOnError_sec == that.timeOutOnError_sec;
        }        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + Objects.hashCode(this.logPath);
            hash = 13 * hash + this.timeOutOnError_sec;
            return hash;
        }
    }    
    public static class LogPathInfo
    {
        private final String logname;
        private final boolean dated;
        private final boolean inTemp;
        private final String logPath;
        private final String forcedFullFilePath;        
        public final String fullFilePath()
        {
            if (!stringIsNullOrEmpty(forcedFullFilePath))
                return forcedFullFilePath;
            String f = logname;

            if (dated)
                f += NowToString("yyyyMMdd");        

            if (inTemp)
            {
                String temp = SlashOnEnd(getTmpdir());
                if (!stringIsNullOrEmpty(logPath) && !ReplaceBadPathChars(logPath, "").equals("")) {
                    temp = SlashOnEnd(temp + ReplaceBadPathChars(logPath, ""));
                }
                f = temp + f + ".log.xml";
            }
            else
            {   
                f = SlashOnEnd(logPath) + f + ".log.xml";
            }  
            return f;
        }
        public final int timeOutOnError_sec;
        LogPathInfo(String logname, boolean dated, boolean inTemp, String logPath, int timeOutOnError_sec)
        {
            this.logname = logname;
            this.dated = dated;
            this.inTemp = inTemp;
            this.logPath = logPath;
            this.timeOutOnError_sec = timeOutOnError_sec;
            this.forcedFullFilePath = null;
        }
        LogPathInfo(String forcedFullFilePath, int timeOutOnError_sec)
        {
            this.logname = null;
            this.dated = false;
            this.inTemp = false;
            this.logPath = null;
            this.timeOutOnError_sec = timeOutOnError_sec;
            this.forcedFullFilePath = forcedFullFilePath;
        }
        private long lastErrorTS = 0;
        private String lastError;
        public void setLastError(String v)
        {
            lastErrorTS = System.currentTimeMillis();
            lastError = nz(v, "<null>");
        }
        public boolean readyToWrite()
        {
            return millisecToReady() == 0;
        }
        public long millisecToReady()
        {
            long result = (lastErrorTS == 0 ? 0 : (timeOutOnError_sec * 1000) - (System.currentTimeMillis() - lastErrorTS));            
            return (result < 0 ? 0 : result);
        }
        
        public static String toExString(LogPathInfo[] a)
        {
            return ArrayToString(a, (lpi) -> { 
                return "[" + lpi.fullFilePath() + (lpi.lastErrorTS > 0 ? ", last error (" + (System.currentTimeMillis() - lpi.lastErrorTS) + "ms ago): " + lpi.lastError : "" ) + "]"; 
            }, ", " );
        }
    }
    public static final String hr = "\r\n________________________________\r\n";
    public static String hr()
    {
        return br() + "________________________________" + br();
    }
    public static Log debugLog;
    public static class Log
    {
        //final boolean Async; final String Logname; final boolean Dated; final boolean InTemp; final LogPathInitInfo[] LPIs;
        final LogInfo LI;
        public Log(boolean Async, String Logname, boolean Dated, String LogPath)
        {
            this(Async, Logname, Dated, new LogPathInitInfo[] { new LogPathInitInfo(LogPath)});
        }
        public Log(boolean Async, String Logname, boolean Dated, LogPathInitInfo[] LPIIs)
        {
            this.LI = new LogInfo(Async, Logname, Dated, LPIIs);
//            this.Async = Async; 
//            this.Logname = Objects.requireNonNull(Logname, "MUSTNEVERTHROW: Logname is null!"); 
//            this.Dated = Dated; 
//            this.InTemp = false; 
//            this.LPIs = Objects.requireNonNull(LPIs, "MUSTNEVERTHROW: LPIs is null!");
        }
        public Log(LogInfo LI)
        {
            this.LI = LI;
        }

        public String write(String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags)
        {
            return write(Method, Msg, StringSimpleEntryArrayList2Array(AddTags), false);
        }
        public String write(String Method, String Msg, ArrayList<AbstractMap.SimpleEntry<String, String>> AddTags, boolean AsyncWait)
        {
            return write(Method, Msg, StringSimpleEntryArrayList2Array(AddTags), AsyncWait);
        }        
        public String write(String Msg)
        {
            return write(getCurrentSTEMethodName(1), Msg, (Object[][])null);
        }
        public String writeSync(String Msg)
        {
            return write(getCurrentSTEMethodName(1), Msg, true);
        }
        public String write(String Msg, Object[][] AddTags)
        {
            return write(getCurrentSTEMethodName(1), Msg, AddTags);
        }
        public String write(String Method, String Msg)
        {
            String[][] dummy = null;
            return write(Method, Msg, dummy, false);
        }
        public String write(String Method, String Msg, boolean AsyncWait)
        {
            String[][] dummy = null;
            return write(Method, Msg, dummy, AsyncWait);
        }
        public String write(String Method, String Msg, String[][] AddTags)
        {
            return write(Method, Msg, AddTags, false);
        }
        public String write(String Method, String Msg, Object[][] AddTags)
        {
            return write(Method, Msg, arrayTransform(AddTags, String[][].class, a -> arrayTransform(a, String[].class, e -> Common.toString(e))), false);
        }

        public String write(Throwable exception, StackTraceElement callSTE)
        {
            return write(exception, callSTE, "" , null   , false);
        }
        public String write(Throwable exception, StackTraceElement callSTE, boolean AsyncWait)
        {
            return write(exception, callSTE, "" , null   , AsyncWait);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String[][] AddTags)
        {
            return write(exception, callSTE, "" , AddTags, false);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String[][] AddTags, boolean AsyncWait)
        {
            return write(exception, callSTE, "" , AddTags, AsyncWait);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String Msg)
        {
            return write(exception, callSTE, Msg, null   , false);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String Msg, boolean AsyncWait)
        {
            return write(exception, callSTE, Msg, null   , AsyncWait);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String Msg, String[][] AddTags)
        {
            return write(exception, callSTE, Msg, AddTags, false);
        }
        public String write(Throwable exception, StackTraceElement callSTE, String Msg, String[][] AddTags, boolean AsyncWait)
        {
            String[][] a_t = null;
            try
            {
                a_t = new String[][]{new String[]{"exception", nz(exception.toString())}, new String[]{"stackTrace", getGoodStackTrace(exception, callSTE)}};
            }
            catch (Throwable th) { 
                try
                {
                    a_t = new String[][]{new String[]{"exceptionMessage", nz(exception.getMessage())}, new String[]{"write_ERROR", nz(th.getMessage())}};
                }
                catch (Throwable dummy) { }
            }
            try
            {
                a_t = ConcatArray(a_t, AddTags);
            }
            catch (Throwable dummy) { }
            return write(callSTE.toString(), Msg, a_t, AsyncWait);
        }
        public String write(String Method, String Msg, String[][] AddTags, boolean AsyncWait)
        {
            try
            {
                WriteLog(LI, Method, Msg, AddTags, AsyncWait);
                return null;
            }
            catch (Throwable th)
            {
                String thstack = "----";
                try { thstack = getGoodStackTrace(th, 0); } catch (Throwable dummy) { }
                try
                {
                    try
                    {
                        System.err.println("CommonLib.Common.Log.write() FatalError: " + th.toString() 
                            + hr 
                            + thstack
                            + hr 
                            + "failed log LPIs: " + LogPathInfo.toExString(LI.LPIs)
                            + hr 
                            + "Msg: " + Msg
                            + hr 
                            + "AddTags: " + (AddTags == null ? "null" : ArrayToString(AddTags, (a) -> { return String.join(":", a); }, ", ") ) 
                            + hr 
                        );
                        //String[] z = null;
                        //String[] x = z.clone();
                        //z.length;
                    }
                    catch (Throwable dummy) {
                        try
                        {
                            System.err.println("CommonLib.Common.Log.write() FatalError!");
                        }
                        catch (Throwable dummydummy) { }
                    }
                }
                catch (Throwable dummy) { }
                try
                {
                    return th.toString() + hr + getGoodStackTrace(th, 0);
                }
                catch (Throwable dummy) {
                    return "CommonLib.Common.Log.write() FatalError!!!";
                }

            }
        }
    }
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    @FunctionalInterface
    public static interface Action {
        void call();
    }
    @FunctionalInterface
    public static interface Action1<Targ1> {
        void call(Targ1 arg1);
    }
    @FunctionalInterface
    public static interface Action2<Targ1,Targ2> {
        void call(Targ1 arg1, Targ2 arg2);
    }
    @FunctionalInterface
    public static interface Func<Tres> {
        Tres call();
    }
    @FunctionalInterface
    public static interface Func1<Targ1, Tres> {
        Tres call(Targ1 arg1);
    }
    @FunctionalInterface
    public static interface Func2<Targ1,Targ2, Tres> {
        Tres call(Targ1 arg1, Targ2 arg2);
    }
    @FunctionalInterface
    public static interface ActionTHROWS {
        void call() throws Exception;
    }
    @FunctionalInterface
    public static interface Action1THROWS<Targ1> {
        void call(Targ1 arg1) throws Exception;
    }
    @FunctionalInterface
    public static interface Action2THROWS<Targ1,Targ2> {
        void call(Targ1 arg1, Targ2 arg2) throws Exception;
    }
    @FunctionalInterface
    public static interface FuncTHROWS<Tres> {
        Tres call() throws Exception;
    }
    @FunctionalInterface
    public static interface Func1THROWS<Targ1, Tres> {
        Tres call(Targ1 arg1) throws Exception;
    }
    @FunctionalInterface
    public static interface Func2THROWS<Targ1,Targ2, Tres> {
        Tres call(Targ1 arg1, Targ2 arg2) throws Exception;
    }
    @FunctionalInterface
    public static interface Action1THROWSSPECIFIC<Targ1, E1 extends Exception> {
        void call(Targ1 arg1) throws E1;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    
  
    
    public static String getGoodStackTrace(Throwable ex, int stackLevelsFromCatch)
    {
        return getGoodStackTrace(ex, stackLevelsFromCatch, false, null);
    }
    public static String getGoodStackTrace(Throwable ex, int stackLevelsFromCatch, boolean forceFullStack)
    {
        return getGoodStackTrace(ex, stackLevelsFromCatch, forceFullStack, null);
    }
    static String getGoodStackTrace(Throwable ex, int stackLevelsFromCatch, boolean forceFullStack, Object dummy)
    {
        StackTraceElement currentSTE = Thread.currentThread().getStackTrace()[stackLevelsFromCatch + 3];//-1-getStackTrace()-- -2-getGoodStackTrace(v,b,o)-- -3-getGoodStackTrace(v) or getGoodStackTrace(v,b)--
        return getGoodStackTrace(ex, currentSTE, forceFullStack);
    }
    
    public static String getGoodStackTrace(Throwable ex, StackTraceElement catchSTE)
    {
        return getGoodStackTrace(ex, catchSTE, false);
    }
    public static String getGoodStackTrace(Throwable ex, StackTraceElement catchSTE, boolean forceFullStack)
    {
        String stack = "";
        for (StackTraceElement ste : ex.getStackTrace()) 
        {
            stack += ste.toString() + "\r\n";
            if (!forceFullStack 
                && catchSTE != null 
                && ste.getClassName().equals(catchSTE.getClassName()) 
                && ste.getMethodName().equals(catchSTE.getMethodName())
            )
            {
                stack += "...";
                break;
            }     
        }
        return stack;
    }
    public static StackTraceElement[] getGoodStackTraceSTEA(Throwable ex, StackTraceElement catchSTE)
    {
        StackTraceElement[] result = null;
        StackTraceElement[] st = ex.getStackTrace();
        if (st != null)
        {
            int n;
            for (n = 0; n < st.length; n++) 
                if (st[n].getClassName().equals(catchSTE.getClassName()) && st[n].getMethodName().equals(catchSTE.getMethodName()))
                    break;
            result = Arrays.copyOf(st, n + 1);
        }
        return result;
    }
    public static StackTraceElement[] getStackTraceSTEA(int topStackLevel)
    {
        StackTraceElement[] result = null;
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        if (st != null)
        {
            result = Arrays.copyOfRange(st, topStackLevel + 2, st.length - 1);
        }
        return result;
    }
    public static String getStackTrace(int topStackLevel)
    {
        String result = "";
        StackTraceElement[] currentST = Thread.currentThread().getStackTrace();
        if (currentST != null)
        {
            for (int n = topStackLevel + 2; n < currentST.length; n++)// -1-getStackTrace()-- -2-getStackTrace(i)
            {
                StackTraceElement ste = currentST[n];
                result += ste.toString() + "\r\n";            
            }
        }
        return result;
    }
    public static StackTraceElement getCurrentSTE()
    {
        return getCurrentSTE(1);
    }
    public static StackTraceElement getCurrentSTE(int stackLevelsFromCall)
    {
        try
        {
            StackTraceElement[] currentST = Thread.currentThread().getStackTrace();
            if (currentST != null)
            {
                if (currentST.length < (stackLevelsFromCall + 2 + 1))
                    return new StackTraceElement("error","error1", "getStackTrace_length_is_less_than_three", 0);
                
                return currentST[stackLevelsFromCall + 2];//-1-getStackTrace()-- -2-getCurrentStackLevel(i)
            }
            else
                return new StackTraceElement("error","error2", "getStackTrace_is_null", 0);
        }
        catch (Throwable th)
        {
            return new StackTraceElement("error","error3", th.getMessage(), 0);
        }
    }    
    public static String getCurrentSTEMethodName(int stackLevelsFromCall)
    {
        StackTraceElement ste = getCurrentSTE(stackLevelsFromCall + 1);
        return ste.getClassName() + "." + ste.getMethodName();
    }
    
    
    public static RuntimeException rethrow(Throwable th) {
        Throwable result;
        if (th instanceof Error)
            result = new Error(th);
        else
            result = new RuntimeException(th);
        result.setStackTrace(ConcatArray(getGoodStackTraceSTEA(th, getCurrentSTE(1)), getStackTraceSTEA(1)));
        if (th instanceof Error)
            throw (Error)result;
        else
            throw (RuntimeException)result;
    }
    public static RuntimeException getExc(Throwable th) {
        RuntimeException result = new RuntimeException(th);
        result.setStackTrace(ConcatArray(getGoodStackTraceSTEA(th, getCurrentSTE(1)), getStackTraceSTEA(1)));
        return result;
    }
    
    public static String getAllStackTraces()
    {
        String result = NowToString();
        Map<Thread, StackTraceElement[]> trdstes = Thread.getAllStackTraces();
        for (Thread th : trdstes.keySet())
        {
            if (   !"Reference Handler".equals(th.getName()) 
                && !"DestroyJavaVM".equals(th.getName()) 
                && !"Finalizer".equals(th.getName()) 
                && !"Signal Dispatcher".equals(th.getName()) 
                && !"Attach Listener".equals(th.getName()) 
            )
            {
                if (result != null)
                    result += "\r\n" + "\r\n";
                result = (result == null ? "" : result) + "Thread '" + th.getName() +  "':" + "\r\n" + ArrayToString(trdstes.get(th), (e) -> { return e.toString(); } , "\r\n");            
            }
        }
        return result;
    }
    
    
    
        
    
    
    public static String throwableToString(Throwable ex, StackTraceElement catchSTE)
    {
        String s;
        return ex.toString() + br() + (!stringIsNullOrWhiteSpace(s = getGoodStackTrace(ex, catchSTE)) ? ("stackTrace: " + s) : ("fullStackTrace: " + getGoodStackTrace(ex, null)));
    }  
    
    
        
    public static class ArtificialError extends Error {
        public ArtificialError(String Message) {
            super(Message);
        }
    }
    public static class ArtificialException extends Exception {
        public ArtificialException(String Message) {
            super(Message);
        }
    }
    public static class ArtificialRuntimeException extends RuntimeException {
        public ArtificialRuntimeException(String Message) {
            super(Message);
        }
    }
    public static class MustNeverHappenException extends RuntimeException {
        public MustNeverHappenException() {
            super();
        }
        public MustNeverHappenException(String Message) {
            super(Message);
        }
    }
    public static Object MustNeverHappen() throws MustNeverHappenException
    {
        throw new MustNeverHappenException();
    }

    
    
    
    ////////////////////////////////////////////////////////////
    
    
    
    
    
    public static byte[] UnGZip(byte[] bytesIn) 
    {
        try 
        {
            byte[] result;
            try (
                ByteArrayInputStream memstream = new ByteArrayInputStream(bytesIn); 
                GZIPInputStream gzin = new GZIPInputStream(memstream); 
                ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ) 
            {
                final int buffSize = 8192;
                byte[] buffer = new byte[buffSize];
                int size;
                while ((size = gzin.read(buffer, 0, buffSize)) != -1) 
                {
                    baos.write(buffer, 0, size);
                }   
                result = baos.toByteArray();
            }
            return result;
        } 
        catch (Exception ex) 
        {
            throw new RuntimeException(ex);
        }
    }
    public static byte[] GZip(byte[] bytesIn) 
    {
        try 
        {
            byte[] result;
            try (
                ByteArrayInputStream memstream = new ByteArrayInputStream(bytesIn);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzipOS = new GZIPOutputStream(baos);
                BufferedOutputStream out = new BufferedOutputStream(gzipOS)            
            ) 
            {
                byte[] buffer = new byte[8192];
                int len;
                while((len=memstream.read(buffer)) != -1)
                {
                    out.write(buffer, 0, len);
                }
                out.close();
                result = baos.toByteArray();
            }
            return result;
        } 
        catch (Exception ex) 
        {
            throw new RuntimeException(ex);
        }         
    }
    
    
    
    
    
    
    
    
    
    //////////////////////////////////////
    
    
    
    
    public static String nz(String value)
    {
        return nz(value, "");
    }    
    public static String nz(String value, String valueIfNull)
    {
        if (value == null)
            return valueIfNull;
        else
            return value;
    }
    public static boolean stringIsNullOrEmpty(String value)
    {
        return value == null || value.equals("");
    }
    public static boolean stringIsNullOrWhiteSpace(String value)
    {
        if (stringIsNullOrEmpty(value)) return true;
        char c;
        for (int n = 0, l = value.length(); n < l; n++)
            if ((c = value.charAt(n)) != ' ' && c  != '\n' && c  != '\r' && c  != '\t')
                return false;
        return true;
    }
    public static boolean stringContainsAnyOfChars(String value, char[] ofChars)
    {
        int len = value.length();
        for(int n = 0; n < len; n++)
            for(char c: ofChars)
                if (value.charAt(n) == c)
                    return true;
        return false;
    }

    
    /////////////////////////////////////
    
    
    
    public static void println(String x)
    {
//        if (System.console() != null)
//            System.console().writer().println(x);
//        else
            System.out.println(x);
    }
    
    
    
    ////////////////////////////////////
    
    static volatile Boolean isDebug;
    public static boolean IsDebug()
    {
        if (isDebug == null)
        {
            String javaargs = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().toLowerCase();
            //if (1==1) throw new Error("javaargs=" + javaargs);
            isDebug = javaargs.contains("-agentlib:jdwp") 
                    || javaargs.contains("-xdebug") 
                    || (javaargs.contains("-xrunjdwp") && javaargs.contains("javadebug"))
                    || javaargs.contains("-DasIstNetBeans=ja".toLowerCase())                    
            ;
        }
        return isDebug;
    }
    public static void setIsDebug(Boolean v)
    {
        isDebug = v;
    }
    static String jarRelativeOrAbsolutePath;
    public static String getJarRelativeOrAbsolutePath(String ExecutingJarFileName)
    {
        if (jarRelativeOrAbsolutePath == null)
        {
            jarRelativeOrAbsolutePath = getJarRelativeOrAbsolutePath(ExecutingJarFileName, false);
        }
        return jarRelativeOrAbsolutePath;        
    }
    static String getJarRelativeOrAbsolutePath(String ExecutingJarFileName, boolean absolute)
    {
        String result = "";
        if (ExecutingJarFileName.contains("\\") || ExecutingJarFileName.contains("/"))
            throw new Error("MUSTNEVERTHROW: remove slash from ExecutingJarFileName!");
        if (!(ExecutingJarFileName.endsWith(".jar") || ExecutingJarFileName.endsWith(".ear")))
            throw new Error("MUSTNEVERTHROW: ExecutingJarFileName MUST be .jar or .ear!");
        String jcp = System.getProperty("java.class.path");
        if (jcp != null && !jcp.toLowerCase().contains(ExecutingJarFileName.toLowerCase()) && !IsDebug())
            throw new Error("Java executable must be named '" + ExecutingJarFileName + "'!");
        if (System.getProperty("sun.java.command") != null)
        {
            String sjc = System.getProperty("sun.java.command");
            if (sjc.toLowerCase().endsWith(".jar") || sjc.toLowerCase().endsWith(".ear"))
            {
                int lastSlash = sjc.lastIndexOf(getFileSeparator());
                if (lastSlash > 0)
                {
                    result = sjc.substring(0, 1 + lastSlash);
                }
                if (absolute)
                    result = Paths.get(result).toAbsolutePath().toString();
            }            
        }
        if (stringIsNullOrEmpty(result) && jcp != null && !IsDebug())
        {
            String[] a = jcp.split("[" + getPathSeparator() + "]");
            for(String jcp1 : a)
                if (jcp1.toLowerCase().contains(ExecutingJarFileName.toLowerCase()))
                {
                    String s_ = jcp1.toLowerCase().replace(ExecutingJarFileName.toLowerCase(), "ZZZZZZZZ");
                    if (s_.toLowerCase().contains(".jar") || s_.toLowerCase().contains(".ear"))
                        throw new Error("MUSTNEVERTHROW: unrecognized java.class.path: " + jcp);
                    
                    int lastSlash = jcp1.lastIndexOf(getFileSeparator());
                    if (lastSlash > 0)
                    {
                        result = jcp1.substring(0, 1 + lastSlash);
                    }
                    if (absolute)
                        result = Paths.get(result).toAbsolutePath().toString();
                }                
        }
        if (stringIsNullOrEmpty(result) && absolute)
            result = System.getProperty("user.dir");
        
        return SlashOnEnd(result);
    }    
    static String jarAbsolutePath;
    public static String getJarAbsolutePath(String ExecutingJarFileName)
    {
        if (jarAbsolutePath == null)
        {
            jarAbsolutePath = getJarRelativeOrAbsolutePath(ExecutingJarFileName, true);
        }
        return jarAbsolutePath;
    }
    
    
    
    
    
    
    
    
    
    //////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    
    
    
    public static void MessageBoxErrorAndThrowError(String Message, String MessageBoxCaption)
    {
        JOptionPane.showMessageDialog(null, Message, MessageBoxCaption, JOptionPane.ERROR_MESSAGE);
        throw new Error(Message);
    }


    
    public static byte[] ToUtf8(String value)
    {
        return value.getBytes(StandardCharsets.UTF_8);
    }
    public static String FromUtf8(byte[] value)
    {
        return new String(value, StandardCharsets.UTF_8);
    }
    public static String repeatString(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }
    
    
    
    //////////////////////////////////////
    
    
    
    
    
    public static String ifIsDirectoryReturnNull(String pathS, LinkOption... options)
    {
        try {
            Path path = Paths.get(pathS);
            if (Files.readAttributes(path, BasicFileAttributes.class, options).isDirectory())
                return null;
            else
                return "Specified path is not directory!";
        } catch (IOException ioe) {
            if (ioe instanceof AccessDeniedException)
                return "Access denied: " + ioe.getMessage();
            else
            if (ioe instanceof NoSuchFileException || ioe instanceof FileNotFoundException)
                return "Path not found!";
            else
                return ioe.toString();
        } catch (InvalidPathException ipe) {
            return "Path '" + pathS + "' invalid: " + ipe.getMessage();
        }
    }

  
    public static String writeTest(String dir, long minFreeSpaceMB)
    {
        String iidrn = ifIsDirectoryReturnNull(dir);
        if (iidrn != null)
            return iidrn;
        String testFile = Common.SlashOnEnd(dir) + "writeTest" + Long.toString(System.currentTimeMillis()) + "_" + ManagementFactory.getRuntimeMXBean().getName() + "." + Thread.currentThread().getId();
        for(int n = 0; true; n++)
            try {
                Files.write(Paths.get(testFile), "qwerty".getBytes());
                break;
            } catch (IOException ex) {
                if (n >= 5)
                    return "Access for file create denied: " + ex.toString();
                else
                    Common.sleep(500);
            }
        Common.sleep(100);
        String m = "";
        for(int n = 0; true; n++)
            try {
                Files.delete(Paths.get(testFile));
                break;
            } catch (IOException ex) {
                if (n >= 5)
                    return "Access for file detete denied: " + ex.toString() + "(" + m + ")";
                else
                    Common.sleep(500);
                m += "; n=" + n + ": " + ex.toString();
            }
        File dirF = new File(dir);
        if (dirF.getUsableSpace() < minFreeSpaceMB * 1024 * 1024)
            return "Infufficient free disk space - lass than " + minFreeSpaceMB + " MiB free (" + Math.round(dirF.getUsableSpace()/(1024*1024)) + " MiB free left)";
        return null;
    }

    
    
    
    
    //////////////////////////////////////
    
    
    
    final static SimpleDateFormat DateSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public static String DateToString(java.util.Date value)
    {
        String result = DateSdf.format(value);
        result = result.replace(" 00:00:00", "");
        return result;
    }
    public static String DateToString(java.util.Date value, String format)
    {
        return new java.text.SimpleDateFormat(format).format(value);
    }
    public static java.util.Date Now()
    {
        return new java.util.Date();
    }
    public static String NowToString()
    {
        return DateToString(Now());
    }
    public static String NowToString(String format)
    {
        return DateToString(Now(), format);
    }

    
    
    
    
    
    //////////////////////////////////////
    
    
    
    public static <T> T[] ConcatArray(T[] original1, T[] original2) 
    {
        if (original1 == null && original2 == null)
            return null;
        if (original1 == null) 
            return Arrays.copyOf(original2, original2.length);
        if (original2 == null) 
            return Arrays.copyOf(original1, original1.length);        
        T[] result = Arrays.copyOf(original1, original1.length + original2.length);
        System.arraycopy(original2, 0, result, original1.length, original2.length);
        return result;
    }
    public static byte[] ConcatArray(byte[] original1, byte[] original2) 
    {
        if (original1 == null && original2 == null)
            return null;
        if (original1 == null) 
            return Arrays.copyOf(original2, original2.length);
        if (original2 == null) 
            return Arrays.copyOf(original1, original1.length);        
        byte[] result = Arrays.copyOf(original1, original1.length + original2.length);
        System.arraycopy(original2, 0, result, original1.length, original2.length);
        return result;
    }
    public static Object[] ConcatArraysOrObjectsToArray(Object original1, Object original2)
    {
        if (original1 == null && original2 == null)
            return null;
        Object[] a1 = original1 == null ? null : (original1 instanceof Object[] ? (Object[])original1 : new Object[] { original1 } );
        Object[] a2 = original2 == null ? null : (original2 instanceof Object[] ? (Object[])original2 : new Object[] { original2 } );
        return ConcatArray(a1, a2);
    } 
    public static char[] arrayAppend(char[] src, char value)
    {
        char[] result = Arrays.copyOf(src, src.length + 1);
        result[src.length] = value;
        return result;
    }
    @SuppressWarnings("unchecked")
    private static <T> String ArrayToString(Object[] a, Func2<T, Integer, String> TtoString, String separ, Class<T> dummyComponentType)
    {
        if (a == null) return null;
        String result = null;
        for (int n = 0; n < a.length; n++)
        {
            Object ae = a[n];
            result = (result == null ? "" : (result + separ)) + TtoString.call((T)ae, n);
        }
        return result;
    }
    public static <T> String ArrayToString(T[] a, Func1<T, String> TtoString, String separ)
    {
        return ArrayToString(a, (ae, n) -> TtoString.call(ae), separ, (Class<T>)null);
    }
    public static <T> String ArrayToString(T[] a, Func2<T, Integer, String> TtoString, String separ)
    {
        return ArrayToString(a, TtoString, separ, (Class<T>)null);
    }
    public static String ArrayToString(long[] a, Func1<Long, String> TtoString, String separ)
    {
        return ArrayToString(Array_long_to_Long(a), (ae, n) -> TtoString.call(ae), separ, (Class<Long>)null);
    }
    public static String ArrayToString(int[] a, Func1<Integer, String> TtoString, String separ)
    {
        return ArrayToString(Array_int_to_Integer(a), (ae, n) -> TtoString.call(ae), separ, (Class<Integer>)null);
    }
    
    public static <T> boolean ArrayContains(T[] a, T search) {
        return ArrayContains(a, search, Objects::equals);
    }
    public static <T, S> boolean ArrayContains(T[] a, S search, Func2<S, T, Boolean> comparator)
    {
        return ArrayContains(a, (t) -> comparator.call(search, t));
    }
    public static <T> boolean ArrayContains(T[] a, Func1<T, Boolean> comparator) 
    {
        if (a == null || a.length == 0 || comparator == null) 
            return false;
        for (T e : a)
            if (comparator.call(e)) 
                return true;
        return false;
    }
    public static <T, S> T[] ArrayWhere(T[] a, S search, Func2<S, T, Boolean> selector) 
    {
        return ArrayWhere(a, (t) -> selector.call(search, t));
    }
    public static <T, S> T[] ArrayWhere(T[] a, Func1<T, Boolean> selector) 
    {
        if (a == null || selector == null) 
            return null;
        if (a.length == 0 )
            return a;
        T[] result = Arrays.copyOf(a, a.length);
        int resultLength=0;
        for (T e : a)
            if (selector.call(e)) 
            {
                result[resultLength] = e;
                resultLength++;
            }
        return Arrays.copyOf(result, resultLength);
    }
    public static <T, S> boolean collectionContains(Collection<T> a, S search, Func2<S, T, Boolean> comparator) 
    {
        return collectionContains(a, (t) -> comparator.call(search, t));
    }
    public static <T> boolean collectionContains(Collection<T> a, Func1<T, Boolean> comparator) 
    {
        if (a == null || a.isEmpty() || comparator == null) 
            return false;
        for (T e : a)
            if (comparator.call(e)) 
                return true;
        return false;
    }
    public static boolean ArrayContainsArrayAt(char[] container, int at, char[] content)
    {
        if (at > (container.length - content.length))
            return false;        
        for (int n = 0; n < content.length; n++)
            if (container[at + n] != content[n])
                return false;
        return true;
    }
    public static boolean ArrayContainsArray(char[] container, char[] content)
    {
        if (container.length < content.length)
            return false;        
        for (int n = 0; n <= container.length - content.length; n++)
            if (ArrayContainsArrayAt(container, n, content))
                return true;
        return false;
    }
    public static <T> T[] collectionToArray(Collection<T> col, Class<T[]> cls)
    {
        return Arrays.copyOf(col.toArray(), col.size(), cls);
    }    
    public static <T> List<T> arrayToList(T[] a)
    {
        if (a == null)
            return null;
        ArrayList<T> result = new ArrayList<>();
        int n = 0;
        for (T e : a)
        {
            result.add(e);  
            n++;
        }
        return result;
    }   
    @SuppressWarnings("unchecked")
    public static <T,U> T[] arrayCopy(U[] original, Integer newLength, Class<T> componentType) {
        if (newLength == null)
            newLength = original.length;
        T[] copy = (T[]) Array.newInstance(componentType, newLength);
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }
    public static <T,C> T[] collectionToArray(Collection<C> col, Class<T[]> cls, Func1<C, T> converter)
    {
        T[] result = genericArrayCreation(col.size(), cls);
        int n = 0;
        for(C elm : col)
        {
            result[n] = converter.call(elm);
            n++;
        }
        return result;
    }
    public static <T> String collectionToString(Collection<T> col, Func1<T, String> TtoString, String separ)
    {
        return ArrayToString(col.toArray(), (ae, n) -> TtoString.call(ae), separ, (Class<T>)null);
    }
    public static <T> String collectionToString(Collection<T> col, Func2<T, Integer, String> TtoString, String separ)
    {
        return ArrayToString(col.toArray(), TtoString, separ, (Class<T>)null);
    }
    
            
    @SuppressWarnings("unchecked")
    public static <T> T[] genericArrayCreation(int length, Class<T[]> cls)
    {
        return (T[]) Array.newInstance(cls.getComponentType(), length);
    }   
    @SuppressWarnings("unchecked")
    public static <T> T[] genericArrayCreationC(int length, Class<T> componentCls)
    {
        return (T[]) Array.newInstance(componentCls, length);
    }     
    
    public static <Ts, Td> Td[] arrayTransform(Ts[] src, Class<Td[]> dCls, Func1<Ts, Td> elmTransformator)
    {
        if (src == null)
            return null;
        Td[] dst = genericArrayCreation(src.length, dCls);
        for (int n = 0; n < src.length; n++)
            dst[n] = elmTransformator.call(src[n]);
        return dst;
    }
    public static <Ts, Td> List<Td> arrayTransformToList(Ts[] src, Func1<Ts, Td> elmTransformator)
    {
        return collectionTransform(arrayToList(src), elmTransformator);
    }
    public static <Ts, Td> List<Td> collectionTransform(Collection<Ts> src, Func1<Ts, Td> elmTransformator)
    {
        if (src == null)
            return null;
        ArrayList<Td> dst = new ArrayList<>();
        for (Ts e : src)
            dst.add(elmTransformator.call(e));
        return dst;
    }
    public static Long[] Array_long_to_Long(long[] src)
    {
        if (src == null)
            return null;
        Long[] dst = new Long[src.length];
        for (int n = 0; n < src.length; n++)
            dst[n] = src[n];
        return dst;
    }
    public static Integer[] Array_int_to_Integer(int[] src)
    {
        if (src == null)
            return null;
        Integer[] dst = new Integer[src.length];
        for (int n = 0; n < src.length; n++)
            dst[n] = src[n];
        return dst;
    }
    
    public static int[] ArrayAdd(int[] original, int add)
    {
        int[] copy = new int[original == null ? 1 : original.length + 1]; copy[copy.length - 1] = add; if (original != null) System.arraycopy(original, 0, copy, 0, original.length); return copy;
    }
    public static long[] ArrayAdd(long[] original, long add)
    {
        long[] copy = new long[original == null ? 1 : original.length + 1]; copy[copy.length - 1] = add; if (original != null) System.arraycopy(original, 0, copy, 0, original.length); return copy;
    }
    public static <T> T[] ArrayAdd(T[] original, T add, Class<T> componentCls)
    {
        T[] copy = genericArrayCreationC(original == null ? 1 : original.length + 1, componentCls); copy[copy.length - 1] = add; if (original != null) System.arraycopy(original, 0, copy, 0, original.length); return copy;
    }
    
    
    public static String toString(Object o)
    {
        if (o == null)
            return "";
        return o.toString();
    }
   

    
    //////////////////////////////////////
    
    public static class TimeoutHandle{ 
        public void clear() { cleared = true; } private volatile boolean cleared; 
        public boolean isExited() { return isExited; } private volatile boolean isExited;         
    }
    public static TimeoutHandle setTimeout(Action handler, int timeout)
    {
        return setTimeout(handler, timeout, false);
    }
    public static TimeoutHandle setTimeout(Action handler, int timeout, boolean isDaemon)
    {
        return setTimeout(handler, timeout, false, null);
    }
    public static TimeoutHandle setTimeout(Action handler, int timeout, boolean isDaemon, String threadName)
    {
        TimeoutHandle result = new TimeoutHandle();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                if (timeout > 0)
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                if (!result.cleared)
                    handler.call();
                }
                finally
                {
                    result.isExited = true;
                }                
            }
        });
        th.setDaemon(isDaemon);
        if (threadName != null)
           th.setName(threadName); 
        th.start();
        return result;
    }
    
    
    
    
    //////////////////////////////////////
    
    
    
    
    public static class JobThread <T>
    {
        private final String JobThreadNameSuffix;
        public JobThread(String JobThreadNameSuffix)
        {
            this.JobThreadNameSuffix = JobThreadNameSuffix;
        }
        static volatile int cnt = 0;
        Thread thd;
        private volatile boolean isThdExited; 
        private volatile boolean isGoOnRun;
        final private Object isGoOnRunLOCK = new Object();
        private volatile Action1THROWS<T> goProc;
        private volatile T goProcArg;
        private volatile ActionTHROWS goProcNoArg;
        private volatile Action2<Throwable, T> onFail;
        public boolean go(ActionTHROWS goProc, Action2<Throwable, T> onFail)
        {
            return go(null, null, goProc, onFail);
        }
        public boolean go(Action1THROWS<T> goProc, T goProcArg, Action2<Throwable, T> onFail)
        {
            return go(goProc, goProcArg, null, onFail);
        }
        private boolean go(Action1THROWS<T> goProc, T goProcArg, ActionTHROWS goProcNoArg, Action2<Throwable, T> onFail)
        {
            if (isGoOnRun)
                return false;
            else
            {
                synchronized (isGoOnRunLOCK)
                {
                    if (isGoOnRun)
                        return false;
                    this.goProc = goProc;
                    this.goProcArg = goProcArg;
                    this.goProcNoArg = goProcNoArg;
                    this.onFail = onFail;
                    isGoOnRun = true;
                }                
                if (thd == null || isThdExited)
                {
                    thd = new Thread(this::loop);
                    cnt++;
                    thd.setName("JobThread" + cnt + (JobThreadNameSuffix == null ? "" : " " + JobThreadNameSuffix)); 
                    isThdExited = false;
                    thd.start();
                }
                return true;
            }
        }
        private volatile boolean finishAndStop = false; 
        private volatile int cyclesFromCall;
        private void loop()
        {
            try
            {
                while(true)
                {
                    if (isGoOnRun)
                    {
                        try
                        {
                            try
                            {
                                cyclesFromCall=0;
                                if (goProc != null)
                                    goProc.call(goProcArg);
                                else
                                    goProcNoArg.call();
                            }
                            catch(Exception ex)
                            {
                                if (onFail != null)
                                    try { onFail.call(ex, goProcArg); } catch(Exception dummy) { }
                            } catch (Throwable th) {
                                if (onFail != null)
                                    try { onFail.call(th, goProcArg); } catch(Exception dummy) { }
                                throw th;
                            }                        
                        }
                        finally
                        {
                            isGoOnRun = false;
                        }
                    }                    
                    if (finishAndStop)
                    {
                        synchronized (isGoOnRunLOCK)
                        {
                            if (!isGoOnRun)
                            {
                                isThdExited = true;
                                return;
                            }
                        }
                    }   
                        
                    sleep((cyclesFromCall < 200 ? 5 : (cyclesFromCall < 450 ? 40 : 200)));
                    cyclesFromCall++;
                }
            }
            finally
            {
                isThdExited = true;
            }            
        }
    }
    public static class JobThreadPool <T>
    {
        final JobThread<T>[] jobThreads;
        final String poolName;
        @SuppressWarnings("unchecked")
        public JobThreadPool(int maxThreads, String poolName)
        {
            jobThreads = new JobThread[maxThreads];
            this.poolName = poolName;
        }
        public void go(ActionTHROWS goProc)
        {
            go(null, null, goProc, null);
        }
        public void go(ActionTHROWS goProc, Action2<Throwable, T> onFail)
        {
            go(null, null, goProc, onFail);
        }
        public void go(Action1THROWS<T> goProc, T goProcArg)
        {
            go(goProc, goProcArg, null, null);
        }
        public void go(Action1THROWS<T> goProc, T goProcArg, Action2<Throwable, T> onFail)
        {
            go(goProc, goProcArg, null, onFail);
        }
        private synchronized void go(Action1THROWS<T> goProc, T goProcArg, ActionTHROWS goProcNoArg, Action2<Throwable, T> onFail)
        {
            int cyclesFromCall = 0;
            while (true)
            {
                for(int n = 0; n < jobThreads.length; n++)
                {
                    if (jobThreads[n] == null)
                        jobThreads[n] = new JobThread<>("of " + poolName + " pool");
                    
                    if (finishAndStop)
                        jobThreads[n].finishAndStop = true;
                    
                    if (jobThreads[n].go(goProc, goProcArg, goProcNoArg, onFail))
                        return;
                }
                sleep((cyclesFromCall < 200 ? 5 : (cyclesFromCall < 450 ? 40 : 200)));
                cyclesFromCall++;
            }
        }
        public synchronized boolean isGoOnRun()
        {
            for (JobThread<T> jobThread : jobThreads) {
                if (jobThread != null && jobThread.isGoOnRun) {
                    return true;
                }
            }
            return false;
        }
        private volatile boolean finishAndStop = false; 
        public void finishAndStop()
        {
            finishAndStop = true;
            for (JobThread<T> jobThread : jobThreads) {
                if (jobThread != null) {
                    jobThread.finishAndStop = true;
                }
            }
        }
    }

    
    
    
    
    
    
    
    //////////////////////////////////////
    
    
    public static void sleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    

    
    //////////////////////////////////////
    @SuppressWarnings("unchecked")
    public static <T>T DirectCast(Object ovalue, Class<T> targetClass)//<class of TTT> asd
    {
        if (ovalue == null) return null;
        if (!targetClass.isInstance(ovalue))
            throw new ClassCastException(ovalue.getClass().getCanonicalName() + " cannot be cast to " + targetClass.getCanonicalName() + ". value: " + ovalue.toString());
        try
        {
            return (T)ovalue;
        }
        catch(Exception ex)
        {
            throw new ClassCastException(ex.getMessage() + ". value: " + ovalue.toString());
        }
    }
       
    /////////////////////////////////////////
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static String getResourceAsUTF8(Class cls, String pth)
    {
        try 
        {
            InputStream in = cls.getResourceAsStream(pth);
            if (in == null)
                throw new NullPointerException("no resource with name '" + pth + "' is found");
            byte[] b = new byte[in.available()]; 
            int pos = 0, rd;
            while (in.available() > 0)
            {
                rd = in.read(b, pos, in.available());
                if (rd == -1)
                    throw new RuntimeException("read returned -1 but available > 0");
                pos += rd;
            }
            return new String(b, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    //////////////////////////////////////
    @SuppressWarnings("unchecked")
    public static <Tsuper, Tctorarg1> Tsuper getAnyInstance(String javaClass, Class<Tsuper> c_Tsuper,  Tctorarg1 ctorarg1) 
    throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class c = Class.forName(javaClass);
        if (!c_Tsuper.isAssignableFrom(c))
            throw new RuntimeException(javaClass + " not a child of " + c_Tsuper.getSimpleName() + "!");
        Objects.requireNonNull(ctorarg1, "ctorarg1 constructor argument must be != null");
        Class<Tsuper> c_T = (Class<Tsuper>)c; 
        Constructor<Tsuper> con;
        con = c_T.getConstructor(ctorarg1.getClass());
        try {
            return con.newInstance(ctorarg1);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetException(ex.getTargetException(), ex.toString() + "; targetException: " + Common.throwableToString(ex.getTargetException(), null));
        }
    }
    
    public static class ClassValuePair<V> { public final Class<V> c_V; public final V v; public ClassValuePair(Class<V> c_V, V v) { this.c_V = c_V; this.v = v; Objects.requireNonNull(c_V, "(c_V) class must not be null"); } }
    
    @SuppressWarnings("unchecked")
    public static <Tsuper, Tctorarg1, Tctorarg2> Tsuper getAnyInstance(String javaClass, Class<Tsuper> c_Tsuper, ClassValuePair<Tctorarg1> ctorarg1, ClassValuePair<Tctorarg2> ctorarg2) 
    throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Class c = Class.forName(javaClass);
        if (!c_Tsuper.isAssignableFrom(c))
            throw new RuntimeException(javaClass + " not a child of " + c_Tsuper.getSimpleName() + "!");
        Class<Tsuper> c_T = (Class<Tsuper>)c; 
        Constructor<Tsuper> con;
        if (ctorarg2 != null)
            con = c_T.getConstructor(ctorarg1.c_V, ctorarg2.c_V);
        else
            con = c_T.getConstructor(ctorarg1.c_V);
        try {            
            if (ctorarg2 != null)
                return con.newInstance(ctorarg1.v, ctorarg2.v);
            else
                return con.newInstance(ctorarg1.v);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetException(ex.getTargetException(), ex.toString() + "; targetException: " + Common.throwableToString(ex.getTargetException(), null));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T, Tctorarg1> T getAnyInstance(Class<T> c_T, ClassValuePair<Tctorarg1> ctorarg1) 
    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
    {        
        Constructor<T> con;
        con = c_T.getConstructor(ctorarg1.c_V);
        try {
            return con.newInstance(ctorarg1.v);
        } catch (InvocationTargetException ex) {
            throw new InvocationTargetException(ex.getTargetException(), ex.toString() + "; targetException: " + Common.throwableToString(ex.getTargetException(), null));
        }
    }
    
   
    
    
    public static class AsyncInputStreamReader 
    {
        public final ByteArrayOutputStream baos;        
        public String streamError;
        final InputStream stream; final Common.Func<Boolean> isToStop;
        public AsyncInputStreamReader(InputStream stream, Common.Func<Boolean> isToStop)
        {
            this.stream = stream;
            this.isToStop = isToStop;
            baos = new ByteArrayOutputStream();
            Common.setTimeout(() -> {
                byte[] b = new byte[10240];
                while (true && !this.isToStop.call())
                {                    
                    try {
                        if (this.stream.available() > 0)
                        {
                            int read = this.stream.read(b);
                            if (read > 0)
                                baos.write(Arrays.copyOf(b, read));
                        }
                    } catch (IOException ex) {
                        streamError = ex.toString();
                        return;
                    }
                    Common.sleep(500);
                }    
            }, 0, true, "AsyncInputStreamReader thread");
        }
        
    }
///////////////////
    public static String getAllFieldValues(Object o, String prefix)
    {
        HashMap<Object, String> was = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        getAllFieldValues(o, null, sb, was, prefix);
        return sb.toString();
    }    
    static void getAllFieldValues(Object o, Class o_cls, StringBuilder sb, HashMap<Object, String> was, String prefix)
    {
        String br = "\r\n";
        Class c; String cn;
        if (o_cls == null && o == null)
            sb.append(prefix).append(" == null").append(br);
        else if (o_cls == null && (o instanceof String || o instanceof Integer || o instanceof Long || o instanceof LocalDateTime || o instanceof Integer))
            sb.append(prefix).append(" == ").append(o.toString()).append(br);
        else if (o_cls == null && (o instanceof Connection || o instanceof URLClassLoader || o instanceof java.lang.ref.Reference || ((c = o.getClass()) != null && (cn = c.getCanonicalName()) != null && (cn.contains("DBmethodsLib.DataTable")))))
            sb.append(prefix).append(" is not need (").append(o.getClass().getCanonicalName()).append(")").append(br);
        else if (o_cls == null && was.containsKey(o))
            sb.append(prefix).append(" was: ").append(was.get(o)).append(br);
        else
        {     
            was.put(o, prefix);
            if (o_cls == null)
                o_cls = o.getClass();
            if (o_cls.isArray())
            {
                if (o_cls.isPrimitive())
                {
                    sb.append(prefix).append(" == ");
                    if (o_cls == boolean[].class) 
                        sb.append(Arrays.toString((boolean[])o));
                    else if (o_cls == byte[].class) 
                        sb.append(Arrays.toString((byte[])o));
                    else if (o_cls == char[].class) 
                        sb.append(Arrays.toString((char[])o));
                    else if(o_cls == double[].class) 
                        sb.append(Arrays.toString((double[])o));
                    else if(o_cls == float[].class) 
                        sb.append(Arrays.toString((float[])o));
                    else if (o_cls == int[].class) 
                        sb.append(Arrays.toString((int[])o));
                    else if (o_cls == long[].class) 
                        sb.append(Arrays.toString((long[])o));
                    else if (o_cls == short[].class) 
                        sb.append(Arrays.toString((short[])o));
                    sb.append(br);
                }
                else
                {
                    try
                    {
                        Object[] a = (Object[])o;
                        for(int n = 0; n < a.length; n++)
                            getAllFieldValues(a[n], null, sb, was, prefix + "[" + n + "]");
                    }
                    catch(Exception ex)
                    {
                        sb.append(prefix).append(" cast to Object[] err: ").append(ex.getMessage()).append(br);
                    }
                }
            }
            else
            {
                Field[] fields = o_cls.getDeclaredFields();            
                for(Field f : fields)
                {            
                    Class t = f.getType();
                    f.setAccessible(true);
                    try {
                        if (t.isPrimitive())
                        {
                            sb.append(prefix).append(".").append(f.getName()).append(" == ");
                            if (t == boolean.class) 
                                sb.append(f.getBoolean(o));
                            else if (t == byte.class) 
                                sb.append(f.getByte(o));
                            else if (t == char.class) 
                                sb.append(f.getChar(o));
                            else if(t == double.class) 
                                sb.append(f.getDouble(o));
                            else if (t == float.class) 
                                sb.append(f.getFloat(o));
                            else if (t == int.class) 
                                sb.append(f.getInt(o));
                            else if (t == long.class) 
                                sb.append(f.getLong(o));
                            else if (t == short.class) 
                                sb.append(f.getShort(o));
                            sb.append(br);
                        }
                        else
                        {
                            getAllFieldValues(f.get(o), null, sb, was, prefix + "." + f.getName());
                        }
                    } 
                    catch (IllegalArgumentException | IllegalAccessException ex) 
                    {
                        sb.append(prefix).append(".").append(f.getName()).append(" error: ").append(ex.getMessage()).append(br);
                    }
                }            
                if (o_cls.getSuperclass() != null)
                    getAllFieldValues(o, o_cls.getSuperclass(), sb, was, prefix + "{" + o_cls.getSuperclass().getName() + "}");
            }
            
        }
    }
    /////////////
    public static class Container<V> { public V value; public Container() { } public Container(V value) { this.value = value; } }
    
    ////////////////
    
    public static XMLGregorianCalendar localDateTimeToXml(LocalDateTime value)
    {        
        try 
        {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                    BigInteger.valueOf(value.getYear()),
                    value.getMonthValue(), 
                    value.getDayOfMonth(), 
                    value.getHour(), 
                    value.getMinute(), 
                    value.getSecond(), 
                    null,
                    DatatypeConstants.FIELD_UNDEFINED
            );
        } 
        catch (DatatypeConfigurationException ex) 
        {
            throw new RuntimeException(ex);
        }
    }
    
    
    //////////    
    
    
    public static String xmlSerialize(Object value)
    {
        if (value == null) return null;
        String rootElementName = null;
        Class cls = value.getClass();
        while (rootElementName == null)
        {
            if (stringIsNullOrEmpty(cls.getSimpleName()))
                cls = cls.getSuperclass();
            else
                rootElementName = cls.getSimpleName();
            if (cls == null)
                throw new UnsupportedOperationException("'" + value.getClass().getCanonicalName() + "' class does not have SimpleName. Use xmlSerialize(String, Object) method instead");
        }
        return xmlSerialize(rootElementName, value);
    }
    @SuppressWarnings("unchecked")
    public static <T> String xmlSerialize(String rootElementName, T value)
    {
        if (value == null) return null;
        return xmlSerialize(rootElementName, value, (Class<T>)value.getClass());
    }
    public static <T> String xmlSerialize(String rootElementName, T value, Class<T> cls_T)
    {
        if (value == null) return null;
        StringWriter sw = new StringWriter();
        try 
        {
            JAXBElement<T> jaxbElement = new JAXBElement<>(new QName(rootElementName), cls_T, value);
            JAXBContext c = JAXBContext.newInstance(cls_T);
            Marshaller m = c.createMarshaller();
            m.marshal(jaxbElement, sw);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
        return sw.toString();
    }
    public static <T> String xmlSerialize(T value, Class<T> cls_T)
    {
        if (value == null) return null;
        if (stringIsNullOrEmpty(cls_T.getSimpleName()))
            throw new UnsupportedOperationException("'" + cls_T.getCanonicalName() + "' class does not have SimpleName. Use xmlSerialize(String, T, Class<? extends T>) method instead");
        return xmlSerialize(cls_T.getSimpleName(), value, cls_T);
    }
    public static <T> byte[] xmlSerializeAndGZip(T value, Class<T> cls_T)
    {
        String s = xmlSerialize(value, cls_T);
        return GZip(s.getBytes(StandardCharsets.UTF_8));
    }
    
    //////////////
    
    public static <E extends Enum<E>> String getXmlEnumValue(E e)
    {
        try {
            return e.getClass().getField(e.name()).getAnnotation(XmlEnumValue.class).value();
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    public static <E extends Enum<E>> E getByXmlEnumValue(String xmlEnumValue, Class<E> enumClass)
    {
        E[] ees = enumClass.getEnumConstants();
        E e = null;
        for (E ee : ees)
            if (getXmlEnumValue(ee).equals(xmlEnumValue))
            {
                if (e != null)
                    throw new RuntimeException("'" + enumClass.getCanonicalName()  + "' enum contains more than one element with XmlEnumValue == " + xmlEnumValue + "!");
                e = ee;
            }
        if (e != null)
            return e;            
        throw new RuntimeException("'" + enumClass.getCanonicalName()  + "' enum does not contain element with XmlEnumValue == " + xmlEnumValue + "!");
    }
    
    

    public static boolean isMethodOverriden(Class<?> cls, String method) 
    {
        try {
            return cls.getMethod(method).getDeclaringClass() == cls;
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static long lapsed_sec(long from_System_currentTimeMillis)
    {
        return (System.currentTimeMillis() - from_System_currentTimeMillis) / 1000;
    }
    
    public static LocalDateTime dateToLocalDateTime(java.util.Date v)
    {
        return (new java.util.Date(v.getTime())).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}


