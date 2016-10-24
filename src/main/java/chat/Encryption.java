package chat;

import java.math.BigInteger;

/**
 * Created by Beata Kalis on 2016-10-19.
 * utility class contains two encryption methods: caesar Cipher and XOR
 */
class Encryption {

    private Encryption() {
        // utility class
    }

    /**
     *
     * @param message original message which will be ciphered
     * @param secret shared secret S
     * @param encode type of operation (encode or decode) - if true it is encode
     * @return ciphered byte array
     */
    static byte[] caesarCipher(String message, BigInteger secret, boolean encode) {
        long ch;
        byte[] array = message.getBytes();
        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            ch = array[i];
            byte shiftedByte;
            if (encode) {
                long shift = (ch + secret.longValue());
                if (shift > 127) {
                    shift -= 127;
                }
                shiftedByte = (byte) shift;

            } else {
                long shift = (ch - secret.longValue());
                if (shift < 0) {
                    shift += 127;
                }
                shiftedByte = (byte) shift;
            }

            result[i] = shiftedByte;
        }
        return result;
    }

    /**
     * @param message original message which will be ciphered
     * @param secret shared secret S, only the last byte will be used to cipher
     * @return ciphered byte array
     */
    static byte[] xor(String message, BigInteger secret) {
        byte b = secret.byteValue();
        byte[] array = message.getBytes();
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (byte) (array[i] ^ (b & 0xFF));
        }
        return result;
    }
}
