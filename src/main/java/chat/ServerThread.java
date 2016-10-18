package chat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;

/**
 * Created by Beata on 2016-10-19.
 */
public class ServerThread extends Thread {
    private final Socket socket;
    private final List<ClientInfo> clientsList;
    private ClientInfo clientInfo = new ClientInfo();
    private final long p = 23; // prime number
    private final long g = 5; // primitive root modulo n
    private long b; // server local secret
    private long S; // shared secret
    private long B; // value send to client
    private long A; // value received from client

    ServerThread(Socket s, List<ClientInfo> clientsList) {
        this.socket = s;
        this.clientsList = clientsList;
        this.clientInfo.setSocket(s);
        this.b = new Random().nextInt(27);
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            boolean canSendB = false;
            boolean canCalculateS = false;
            boolean canSendMessage = false;

            while (true) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                if (canSendB) {
                    B = (long) (Math.pow(g, b)) % p;
                    System.out.println("Server B: " + B);
                    JSONObject key = new JSONObject();
                    key.put("B", B);
                    out.println(key);
                    canSendB = false; // only once send value B
                }

                if (canCalculateS) {
                    S = (long) (Math.pow(A, b)) % p;
                    clientInfo.setSecret(S);
                    System.out.println("Server S: " + S);
                    canCalculateS = false; // only once calculate value S;
                }

                String input = in.readLine();
                if (input == null) {
                    break;
                }

                System.out.println("from client: " + input);
                JSONObject object = new JSONObject(input);

                if (object.has("request") && object.getString("request").equals("keys")) {
                    JSONObject keys = new JSONObject();
                    keys.put("p", p);
                    keys.put("g", g);
                    out.println(keys);
                    canSendB = true; // server can send B to client

                } else if (object.has("A")) {
                    A = object.getLong("A");
                    System.out.println("A from client: " + A);
                    canCalculateS = true; // can calculate value S

                } else if (object.has("encryption")) {
                    clientInfo.setEncryption(object.getString("encryption"));
                    clientsList.add(clientInfo);
                    canSendMessage = true;

                } else if (object.has("msg") & canSendMessage) {
                    // send received message to all clients
                    for (int i = 0; i < clientsList.size(); i++) {
                        Socket s = clientsList.get(i).getSocket();
                        out = new PrintWriter(s.getOutputStream(), true);
                        out.println(input);
                    }
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
}
