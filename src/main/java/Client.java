import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Beata Kalis on 2016-10-12.
 */
public class Client extends JFrame implements Runnable {

    private BufferedReader in;
    private PrintWriter out;
    private JTextField inputMessage;
    private JTextArea messagesList;
    private String clientName;

    Client(String host, int port, String name) {
        setTitle(name);
        setBounds(10, 10, 400, 400);

        clientName = name;

        try {
            Socket socket = new Socket(host, port);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initComponents();
        setListeners();

    }

    private void initComponents() {
        JPanel panel = new JPanel();
        inputMessage = new JTextField();
        inputMessage.setPreferredSize(new Dimension(300, 20));
        messagesList = new JTextArea();
        messagesList.setPreferredSize(new Dimension(300, 300));
        panel.add(inputMessage);
        panel.add(messagesList);
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setListeners() {
        inputMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = clientName + ": " +inputMessage.getText();
                String encodeMsg = Base64.encode(msg.getBytes());
                JSONObject object = new JSONObject();
                object.put("msg", encodeMsg);
                out.println(object.toString());
                inputMessage.setText(null);
            }
        });
    }

    public void run() {
        while (true) {
            String response;
            try {
                response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
                JSONObject object = new JSONObject(response);
                byte[] decodedBytes = Base64.decode(object.getString("msg"));
                messagesList.append(new String(decodedBytes) + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client(args[0], Integer.parseInt(args[1]), args[2]).run();
    }
}
