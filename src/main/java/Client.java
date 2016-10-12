import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Beata on 2016-10-12.
 */
public class Client extends JFrame {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JLabel labelMessage;
    private JTextField textFieldMessage;
    private JTextArea textArea1;
    private JPanel panel;

    Client(String host, int port) {
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initComponents();
    }

    private void initComponents() {
     //   panel.setLayout(null);
    }

    public static void main(String[] args) {
        new Client(args[0], Integer.parseInt(args[1]));

    }
}
