import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beata Kalis on 2016-10-12.
 */
public class Server {

    private ServerSocket server;
    private List<Socket> clientsList = new ArrayList<Socket>(); // list of clients to send them messages

    Server(int port) {
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
                clientsList.add(client); // add client to the list
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
        private Socket socket;
        private List<Socket> clientsList;

        ServerThread(Socket s, List<Socket> clientsList) {
            this.socket = s;
            this.clientsList = clientsList;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                // send message to client
                //  out.println("Hello ;)");

                // Get messages from the client, line by line
                while (true) {
                    String input = in.readLine();

                    if (input == null) {
                        break;
                    }

                    System.out.println("from client: " + input);
                    JSONObject object = new JSONObject(input);

                    if (object.has("request")) {

                    } else if (object.has("a")) {
                        Integer i = (Integer) object.get("a");
                        System.out.println("a: " + i);

                    } else if (object.has("encryption")) {

                    } else if (object.has("msg")) {
                        // send received message to all clients
                        for (Socket s : clientsList) {
                            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                            out.println((String) object.get("msg"));
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
