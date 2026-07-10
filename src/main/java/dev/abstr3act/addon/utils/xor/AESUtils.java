package dev.abstr3act.addon.utils.xor;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtils {
    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static SecretKey createSecretKey(String key) {
        byte[] keyBytes = key.getBytes();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String data, String key) {
        try {
            SecretKey secretKey = createSecretKey(key);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(1, secretKey, ivParams);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            byte[] encryptedWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedWithIv, iv.length, encryptedData.length);
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception var8) {
            return var8.getMessage();
        }
    }

    public static String decrypt(String encryptedData, String key) {
        try {
            SecretKey secretKey = createSecretKey(key);
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            byte[] data = new byte[encryptedWithIv.length - iv.length];
            System.arraycopy(encryptedWithIv, iv.length, data, 0, data.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(2, secretKey, ivParams);
            byte[] decryptedData = cipher.doFinal(data);
            return new String(decryptedData);
        } catch (Exception var9) {
            return var9.getMessage();
        }
    }
}
