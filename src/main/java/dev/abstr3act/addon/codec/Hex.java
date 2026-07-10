package dev.abstr3act.addon.codec;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Hex implements org.apache.commons.codec.BinaryEncoder, org.apache.commons.codec.BinaryDecoder {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private final Charset charset;

    public Hex() {
        this.charset = DEFAULT_CHARSET;
    }

    public Hex(Charset charset) {
        this.charset = charset;
    }

    public Hex(String charsetName) {
        this(Charset.forName(charsetName));
    }

    public static byte[] decodeHex(char[] data) throws org.apache.commons.codec.DecoderException {
        byte[] out = new byte[data.length >> 1];
        decodeHex(data, out, 0);
        return out;
    }

    public static int decodeHex(char[] data, byte[] out, int outOffset) throws org.apache.commons.codec.DecoderException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new org.apache.commons.codec.DecoderException("Odd number of characters.");
        } else {
            int outLen = len >> 1;
            if (out.length - outOffset < outLen) {
                throw new org.apache.commons.codec.DecoderException("Output array is not large enough to accommodate decoded data.");
            } else {
                int i = outOffset;

                for (int j = 0; j < len; i++) {
                    int f = toDigit(data[j], j) << 4;
                    f |= toDigit(data[++j], j);
                    j++;
                    out[i] = (byte) (f & 0xFF);
                }

                return outLen;
            }
        }
    }

    public static byte[] decodeHex(String data) throws org.apache.commons.codec.DecoderException {
        return decodeHex(data.toCharArray());
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int dataLength = data.length;
        char[] out = new char[dataLength << 1];
        encodeHex(data, 0, dataLength, toDigits, out, 0);
        return out;
    }

    public static char[] encodeHex(byte[] data, int dataOffset, int dataLen, boolean toLowerCase) {
        char[] out = new char[dataLen << 1];
        encodeHex(data, dataOffset, dataLen, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER, out, 0);
        return out;
    }

    public static void encodeHex(byte[] data, int dataOffset, int dataLen, boolean toLowerCase, char[] out, int outOffset) {
        encodeHex(data, dataOffset, dataLen, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER, out, outOffset);
    }

    private static void encodeHex(byte[] data, int dataOffset, int dataLen, char[] toDigits, char[] out, int outOffset) {
        int i = dataOffset;

        for (int j = outOffset; i < dataOffset + dataLen; i++) {
            out[j++] = toDigits[(240 & data[i]) >>> 4];
            out[j++] = toDigits[15 & data[i]];
        }
    }

    public static char[] encodeHex(ByteBuffer data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(ByteBuffer data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(ByteBuffer byteBuffer, char[] toDigits) {
        return encodeHex(toByteArray(byteBuffer), toDigits);
    }

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    public static String encodeHexString(byte[] data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    public static String encodeHexString(ByteBuffer data) {
        return new String(encodeHex(data));
    }

    public static String encodeHexString(ByteBuffer data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    private static byte[] toByteArray(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();
        if (byteBuffer.hasArray()) {
            byte[] byteArray = byteBuffer.array();
            if (remaining == byteArray.length) {
                byteBuffer.position(remaining);
                return byteArray;
            }
        }

        byte[] byteArray = new byte[remaining];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    protected static int toDigit(char ch, int index) throws org.apache.commons.codec.DecoderException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new org.apache.commons.codec.DecoderException("Illegal hexadecimal character " + ch + " at index " + index);
        } else {
            return digit;
        }
    }

    public byte[] decode(byte[] array) throws org.apache.commons.codec.DecoderException {
        return decodeHex(new String(array, this.getCharset()).toCharArray());
    }

    public byte[] decode(ByteBuffer buffer) throws org.apache.commons.codec.DecoderException {
        return decodeHex(new String(toByteArray(buffer), this.getCharset()).toCharArray());
    }

    public Object decode(Object object) throws org.apache.commons.codec.DecoderException {
        if (object instanceof String) {
            return this.decode(((String) object).toCharArray());
        } else if (object instanceof byte[]) {
            return this.decode((byte[]) object);
        } else if (object instanceof ByteBuffer) {
            return this.decode((ByteBuffer) object);
        } else {
            try {
                return decodeHex((char[]) object);
            } catch (ClassCastException var3) {
                throw new org.apache.commons.codec.DecoderException(var3.getMessage(), var3);
            }
        }
    }

    public byte[] encode(byte[] array) {
        return encodeHexString(array).getBytes(this.getCharset());
    }

    public byte[] encode(ByteBuffer array) {
        return encodeHexString(array).getBytes(this.getCharset());
    }

    public Object encode(Object object) throws org.apache.commons.codec.EncoderException {
        byte[] byteArray;
        if (object instanceof String) {
            byteArray = ((String) object).getBytes(this.getCharset());
        } else if (object instanceof ByteBuffer) {
            byteArray = toByteArray((ByteBuffer) object);
        } else {
            try {
                byteArray = (byte[]) object;
            } catch (ClassCastException var4) {
                throw new org.apache.commons.codec.EncoderException(var4.getMessage(), var4);
            }
        }

        return encodeHex(byteArray);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getCharsetName() {
        return this.charset.name();
    }

    @Override
    public String toString() {
        return super.toString() + "[charsetName=" + this.charset + "]";
    }
}
