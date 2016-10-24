package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beata Kalis on 2016-10-12.
 * class represents Server
 */
public class Server {

    private ServerSocket server;
    private final List<ClientInfo> clientsList = new ArrayList<ClientInfo>();

    /**
     *
     * @param port number of port on which server will be listening
     */
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

    /**
     * method to accepts client connections and start new thread for every client
     */
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

}
