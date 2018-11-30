/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CommonLib.Crypto;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;


/**
 *
 * 
 */
public class CryptoExCommon {
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String hex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHex(String enc) {
        byte[] r = new byte[enc.length() / 2];
        char[] hexChars = enc.toCharArray();
        for (int n = 0; n < r.length; n++) {
            int iv = Integer.parseInt(new String(new char[]{ hexChars[n * 2], hexChars[n * 2 + 1]}), 16);
            r[n] = (byte)(iv > 0x7f ? iv | 0xffffff00 : iv);
        }
        return r;
    }

    public static final hashing md5 = new hashing("MD5");
    public static final hashing sha1 = new hashing("SHA1");
    
    public static class crc32
    {
        public static long hashLong(byte[] inp)
        {
            CRC32 crc32 = new CRC32();
            crc32.update(inp);
            return crc32.getValue();
        }
        public static long hashLong(String inp) 
        {
            byte[] inpB;
            try {
                inpB = inp.getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
            return hashLong(inpB);
        }
        
        public static int long2intSafe(long inp)
        {
            long l = inp & 0xffffffffL;
            if (l > 0x7fffffffL)
                return (int)(l | 0xffffffff00000000L);
            else
                return (int)l;
        }     
        public static int hashInt(byte[] inp) { return long2intSafe(hashLong(inp)); }
        public static int hashInt(String inp) { return long2intSafe(hashLong(inp)); }
    }

}
