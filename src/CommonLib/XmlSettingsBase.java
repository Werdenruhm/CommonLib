package CommonLib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * 
 * @param <E>
 */
public class XmlSettingsBase<E extends Enum<E>> {
    final Properties props = new Properties();
    final String fileName;
    final boolean autoStore;
    final String[] requiredProperties;
    public XmlSettingsBase(String ExecutingJarWarEarFileName, String settingsFileName, boolean ConfigFileIsRequiredForStart, String[] RequiredProperties)
    {
        String fileName_ = settingsFileName;
        String roap;
        try {
            roap = Common.getJarRelativeOrAbsolutePath(ExecutingJarWarEarFileName);
        } catch (Throwable ex) {
            fileName = fileName_;
            writeTmpSysProps();
            throw ex;
        }

        if (roap.length() > 0)
            fileName_ = roap + fileName_;
        fileName = fileName_;
        
        autoStore = true;
        requiredProperties = RequiredProperties;
        try {
            loadFromXML();
        } catch (Exception ex) {
            if (ConfigFileIsRequiredForStart)
                throw ex;
        }
    }
    public XmlSettingsBase(String ExecutingJarWarEarFileName, String settingsFileName, boolean ConfigFileIsRequiredForStart, Class<E> requiredPropertiesEnum)
    {
        this(ExecutingJarWarEarFileName, settingsFileName, ConfigFileIsRequiredForStart, Common.arrayTransform(requiredPropertiesEnum.getEnumConstants(), String[].class, ee -> ee.toString()));
    }
    public String getProperty(String key) {
        return props.getProperty(key);
    }
    public String getProperty(E key) {
        return props.getProperty(key.toString());
    }
    public synchronized void storeToXML()
    {
        try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
            props.storeToXML(fos, "");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
    public synchronized void setProperty(String key, String value) {
        props.setProperty(key, value);
        if (autoStore)
            storeToXML();
    }
    public final synchronized void loadFromXML()
    {
        try (FileInputStream fis = new FileInputStream(new File(fileName))) {
            props.loadFromXML(fis);
        } catch (IOException ex) {
            String tmpFileName = Common.getTmpdir() + (fileName.contains(Common.getFileSeparator()) ? fileName.substring(1 + fileName.lastIndexOf(Common.getFileSeparator())) : fileName);
            try (FileOutputStream fos = new FileOutputStream(new File(tmpFileName))) {
                Properties props2 = new Properties();
                if (requiredProperties != null && requiredProperties.length > 0)
                {
                    for (String prp : requiredProperties)
                    {
                        props2.setProperty(prp, "value of " + prp);
                    }
                }
                props2.storeToXML(fos, "");
            } catch (IOException ex2) { tmpFileName = null; }
            writeTmpSysProps();
            throw new Common.ArtificialError("Error reading properties from  (" + fileName + "): " + ex.getMessage() + "\r\n"
                + "An example properties file created in '" + tmpFileName + "'.");
        }
        if (requiredProperties != null && requiredProperties.length > 0)
        {
            String errmsg = null;
            for (String prp : requiredProperties)
            {
                if (getProperty(prp) == null)
                {
                    if (errmsg == null)
                        errmsg = "A properties file (" + fileName + ") does not have required properties: ";
                    else
                        errmsg += ", ";
                    errmsg += "[" + prp + "]";
                }
            }
            if (errmsg != null)
            {
                errmsg += "! Add it into a properties file and restart.";
                throw new Common.ArtificialError(errmsg);
            }
        }
    }
    final void writeTmpSysProps()
    {
        String tmpFileName = "";
        try {
            tmpFileName = Common.getTmpdir() + (fileName.contains(Common.getFileSeparator()) ? fileName.substring(1 + fileName.lastIndexOf(Common.getFileSeparator())) : fileName);
            Properties prprts = System.getProperties();
            String prprtsS = "";
            for (String k : prprts.stringPropertyNames())
                prprtsS += k + "=[" + prprts.getProperty(k) + "]\r\n";
            
            prprtsS += "\r\n\r\n" + "InputArguments:" + "\r\n";
            for (String k : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments())
                prprtsS += k + "\r\n";
            
            Files.write(Paths.get(tmpFileName + "_SystemProperties_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".txt"), prprtsS.getBytes("utf-8"));
        } catch (Exception exx) { throw new Error("writeTmpSysProps Error(tmpFileName = " + tmpFileName + "):" + exx.getMessage());}
    }
}
