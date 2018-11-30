package CommonLib.Crypto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Objects;
import javax.crypto.*;
import javax.crypto.spec.*;

public class AES {
    
    private static byte[] safeKey(byte[] key) 
    {
        if (key.length == 16)
            return key;
        else
            return Arrays.copyOf(key, 16);
    }
    
    public static byte[] encrypt(byte[] value, byte[] key)
    {
        try {
            SecretKey k = new SecretKeySpec(safeKey(key), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, k);
            return cipher.doFinal(value);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static byte[] decrypt(byte[] encypted, byte[] key)
    {
        try {
            SecretKey k = new SecretKeySpec(safeKey(key), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
            cipher.init(Cipher.DECRYPT_MODE, k);
            return cipher.doFinal(encypted);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encrypt(String value, String password)
    {
        byte[] encrypted = encrypt(value.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
        return CryptoExCommon.hex(encrypted);
    }


    public static String decrypt(String value, String password)
    {
        byte[] encypted = CryptoExCommon.fromHex(value);
        byte[] decrypted = decrypt(encypted, password.getBytes(StandardCharsets.UTF_8));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
}
