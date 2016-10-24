package server;

import encryption.Encryption;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.List;
import java.util.Random;

/**
 * Created by Beata Kalis on 2016-10-19.
 * class represents a channel between server and client
 */
class ServerThread extends Thread {
    private final Socket socket;
    private final List<ClientInfo> clientsList;
    private final ClientInfo clientInfo = new ClientInfo();
    private final BigInteger p = new BigInteger("23"); // prime number
    private final BigInteger g = new BigInteger("5"); // primitive root modulo n
    private final BigInteger b; // server local secret, random value
    private BigInteger S; // shared secret
    private BigInteger B; // value send to client
    private BigInteger A; // value received from client
    private boolean canSendB = false;
    private boolean canCalculateS = false;
    private boolean canSendMessage = false;
    private PrintWriter out;

    /**
     *
     * @param s client socket
     * @param clientsList list of all clients; it is necessary to send a message to all connected clients
     */
    ServerThread(Socket s, List<ClientInfo> clientsList) {
        this.socket = s;
        this.clientsList = clientsList;
        this.clientInfo.setSocket(s);

        String serverLocalSecret = "" + new Random().nextInt(1000);
        this.b = new BigInteger(serverLocalSecret);
    }

    /**
     * method to wait for requests and responses
     */
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));


            while (true) {
                out = new PrintWriter(socket.getOutputStream(), true);

                if (canSendB) {
                    sendB();
                }

                if (canCalculateS) {
                    calculateS();
                }

                String input = in.readLine();
                if (input == null) {
                    break;
                }

                System.out.println("from client: " + input);
                JSONObject object = new JSONObject(input);

                if (object.has("request") && object.getString("request").equals("keys")) {
                    sendKeys();

                } else if (object.has("a")) {
                    A = object.getBigInteger("a");
                    canCalculateS = true; // can calculate value S

                } else if (object.has("encryption")) {
                    setEncryption(object);

                } else if (object.has("msg") & canSendMessage) {
                    sendMessages(object);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * method to send value B to the client
     */
    private void sendB() {
        BigInteger tmp = g.pow(b.intValue());
        B = tmp.mod(p);
        System.out.println("Server B: " + B);
        JSONObject key = new JSONObject();
        key.put("b", B);
        out.println(key);
        canSendB = false; // only once send value B
    }

    /**
     * method to calculate shared secret S
     */
    private void calculateS() {
        // S = (long) (Math.pow(A, b)) % p;
        BigInteger tmp = A.pow(b.intValue());
        S = tmp.mod(p);
        clientInfo.setSecret(S);
        System.out.println("Server S: " + S);
        canCalculateS = false; // only once calculate value S;
    }

    /**
     * method to send keys to the client (p and g)
     */
    private void sendKeys() {
        JSONObject keys = new JSONObject();
        keys.put("p", p);
        keys.put("g", g);
        out.println(keys);
        canSendB = true; // server can send B to client
    }

    /**
     * method to set client encryption
     * @param object received JSON
     */
    private void setEncryption(JSONObject object) {
        clientInfo.setEncryption(object.getString("encryption"));
        clientsList.add(clientInfo);
        canSendMessage = true;
    }

    /**
     * method to get a message, decode it from Base63, decipher it from suitable type of encryption and then
     * cipher it for every client using right way of encryption, encode using Base64 and send as JSON to all clients
     * @param object received JSON
     * @throws IOException
     */
    private void sendMessages(JSONObject object) throws IOException {
        // send received message to all clients
        byte[] decodedBytes = Base64.decode(object.getString("msg"));
        byte[] decryptedBytes;
        String response;
        if (clientInfo.getEncryption().equals("cezar")) {
            decryptedBytes = Encryption.caesarCipher(new String(decodedBytes), S, false);
            response = new String(decryptedBytes);
        } else if (clientInfo.getEncryption().equals("xor")) {
            decryptedBytes = Encryption.xor(new String(decodedBytes), S);
            response = new String(decryptedBytes);
        } else {
            // none
            response = new String(decodedBytes);
        }

        for (int i = 0; i < clientsList.size(); i++) {
            Socket s = clientsList.get(i).getSocket();
            out = new PrintWriter(s.getOutputStream(), true);
            byte[] encryptMsg;
            if (clientsList.get(i).getEncryption().equals("cezar")) {
                encryptMsg = Encryption.caesarCipher(response, clientsList.get(i).getSecret(), true);
            } else if (clientsList.get(i).getEncryption().equals("xor")) {
                encryptMsg = Encryption.xor(response, clientsList.get(i).getSecret());
            } else {
                // none
                encryptMsg = response.getBytes();
            }
            String encodeMsg = Base64.encode(encryptMsg);
            JSONObject o = new JSONObject();
            o.put("msg", encodeMsg);
            o.put("from", object.get("from"));
            out.println(o.toString());
        }
    }


}
