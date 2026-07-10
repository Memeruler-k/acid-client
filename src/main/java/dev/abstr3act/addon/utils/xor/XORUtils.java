package dev.abstr3act.addon.utils.xor;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class XORUtils {
    public static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        deflater.end();
        return outputStream.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            inflater.end();
            return outputStream.toByteArray();
        } catch (Exception var5) {
            return new byte[32];
        }
    }

    public static byte[] xorEncryptDecrypt(byte[] data, String key) {
        byte[] output = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            output[i] = (byte) (data[i] ^ key.charAt(i % key.length()));
        }

        return output;
    }

    public static byte[] encoding(String data, String key) {
        byte[] d = data.getBytes(StandardCharsets.UTF_8);
        byte[] compressedData = compress(d);
        return xorEncryptDecrypt(compressedData, key);
    }

    public static byte[] decoding(byte[] data, String key) {
        byte[] decryptedData = xorEncryptDecrypt(data, key);
        return decompress(decryptedData);
    }

    public static String removePrefix(String d) {
        return d.substring(1);
    }

    public static String removeNonAscii(String d) {
        return d.replace("\u200c", "").replaceAll("[^\\x00-\\x7F]", "");
    }
}
