package CommonLib.Crypto;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * 
 */
public class hashing {
    final String algorithm;
    public hashing(String Algorithm)
    {
        algorithm = Algorithm;
    }
    public byte[] hashB(String inp) 
    {
        byte[] inpB;
        try {
            inpB = inp.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        return hashB(inpB);
    }
    public byte[] hashB(byte[] inp)  
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        messageDigest.reset();
        messageDigest.update(inp);
        return messageDigest.digest();
    }
    public String hashHex(String inp) { return CryptoExCommon.hex(hashB(inp)); }
    public String hashHex(byte[] inp) { return CryptoExCommon.hex(hashB(inp)); }
    public String hashB64(String inp) { return Base64.getEncoder().encodeToString(hashB(inp)); }
    public String hashB64(byte[] inp) { return Base64.getEncoder().encodeToString(hashB(inp)); }
    
    
    
    
    
    
    
    
    
    public static class XmlHash 
    {
        public XmlHash(Integer crc32I, String md5S, String sha1S) {
            this.crc32I = crc32I;
            this.md5S = md5S;
            this.sha1S = sha1S;
        }        
        public final Integer crc32I;
        public final String md5S;
        public final String sha1S;
    }
    public static XmlHash getXmlHash(Document doc)
    {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String XmlS = writer.getBuffer().toString();
            String XmlS4hash = XmlS.replaceAll(">[\\s]*[\\r\\n]+[\\s]*<", "><");
            byte[] XmlB4hash = XmlS4hash.toLowerCase().getBytes(StandardCharsets.UTF_8);
            return new XmlHash(
                    CryptoExCommon.crc32.hashInt(XmlB4hash),
                    CryptoExCommon.md5.hashB64(XmlB4hash),
                    CryptoExCommon.sha1.hashB64(XmlB4hash)        
            );
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

}
