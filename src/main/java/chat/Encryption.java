package chat;

/**
 * Created by Beata on 2016-10-19.
 */
public class Encryption {

    private Encryption() {
        // utility class
    }

    public static byte[] caesarCipher(String message, long key, boolean encode) {
        int ch;
        char[] array = message.toCharArray();
        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            ch = array[i];
            byte shiftedByte;
            if (encode) {
                long shift = (ch + key);
                if (shift > 127) {
                    shift -= 127;
                }
                shiftedByte = (byte) shift;

            } else {
                long shift = (ch - key);
                if (shift < 0) {
                    shift += 127;
                }
                shiftedByte = (byte) shift;
            }

            result[i] = shiftedByte;
        }
        return result;
    }

    public static String xor(String msg, long secret){
        byte b = (byte) secret;
        StringBuilder encrypted = new StringBuilder();
        for(byte c : msg.getBytes()){
            encrypted.append((char)(c ^ b & 0xFF));
        }
        return encrypted.toString();
    }
}
