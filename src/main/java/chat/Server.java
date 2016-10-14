package chat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beata Kalis on 2016-10-12.
 */
public class Server {

    private ServerSocket server;
    /**
     * list of clients (socket and secret value S)
     */
    private final Map<Socket, Long> clientsList = new HashMap<Socket, Long>();


    private Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server socket created on port " + port);
        } catch (IOException e) {
            System.out.println("Could not listen on port " + port);
            e.printStackTrace();
        }

        listen();
    }

    private void listen() {
        while (true) {
            try {
                Socket client = server.accept();
                System.out.println("Client connected");
                new ServerThread(client, clientsList).start();
            } catch (IOException ex) {
                System.out.println("I/O error" + ex);
            }
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        new Server(port);
    }

    private static class ServerThread extends Thread {
        private final Socket socket;
        private final Map<Socket, Long> clientsList;
        private final long p = 23; // prime number
        private final long g = 5; // primitive root modulo n
        private final long b = 6; // server local secret
        private long S; // shared secret
        private long B; // value send to client
        private long A; // value received from client

        ServerThread(Socket s, Map<Socket, Long> clientsList) {
            this.socket = s;
            this.clientsList = clientsList;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                boolean canSendB = false;
                boolean canCalculateS = false;

                while (true) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    if (canSendB) {
                        B = (long) (Math.pow(g, b)) % p;
                        System.out.println("Server B: " + B); // 8 expected
                        JSONObject key = new JSONObject();
                        key.put("B", B);
                        out.println(key);
                        canSendB = false; // only once send value B
                    }

                    if (canCalculateS) {
                        S = (long) (Math.pow(A, b)) % p;
                        clientsList.put(socket, S);
                        System.out.println("Server S: " + S); // 2 expected
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
                        // cezar, xor or none

                    } else if (object.has("msg")) {
                        // send received message to all clients
                        for (Socket s : clientsList.keySet()) {
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
}
