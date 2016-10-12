import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beata on 2016-10-12.
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
                clientsList.add(client);
                System.out.println("Client connected");
                new ServerThread(client);
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
        Socket socket;

        ServerThread(Socket s) {
            this.socket = s;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // send message to client
                // out.println("some message");

                // Get messages from the client, line by line
                while (true) {
                    String input = in.readLine();
                    // loop
                    if (input == null) {
                        break;
                    }
                    out.println(input.toUpperCase());
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
