package chat;

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
import java.util.Random;

/**
 * Created by Beata Kalis on 2016-10-12.
 */
public class Client extends JFrame implements Runnable {

    private BufferedReader in;
    private PrintWriter out;
    private JTextField inputMessage;
    private JTextArea messagesList;
    private final String clientName;
    private long p; // prime number
    private long g; // primitive root modulo n
    private long B; // value received from server
    private final long a; // client local secret, value from <0, 27> when g = 5

    private Client(String host, int port, String name) {
        setTitle(name);
        setBounds(10, 10, 400, 400);

        clientName = name;
        a = new Random().nextInt(27);
        System.out.println("Client local secret a: " + a);

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
                String msg = clientName + ": " + inputMessage.getText();
                String encodeMsg = Base64.encode(msg.getBytes());
                JSONObject object = new JSONObject();
                object.put("msg", encodeMsg);
                out.println(object.toString());
                inputMessage.setText(null);
            }
        });
    }

    public void run() {
        boolean keys = true;
        boolean canSendA = false;
        boolean canCalculateS = false;
        long S; // shared secret
        long A; // value send to server
        while (true) {
            if (keys) {
                JSONObject object = new JSONObject();
                object.put("request", "keys");
                out.println(object);
                keys = false;
            }
            if (canSendA) {
                A = (long) (Math.pow(g, a)) % p; // expected 19
                System.out.println("Client A: " + A);
                JSONObject key = new JSONObject();
                key.put("A", A);
                out.println(key);
                canSendA = false; // send value A only once
            }
            if (canCalculateS) {
                S = (long) (Math.pow(B, a)) % p;
                System.out.println("Client S: " + S); // 2 expected
                canCalculateS = false; // only once calculate value S;
            }
            try {
                String response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
                JSONObject object = new JSONObject(response);
                if (object.has("msg")) {
                    byte[] decodedBytes = Base64.decode(object.getString("msg"));
                    messagesList.append(new String(decodedBytes) + "\n");
                } else if (object.has("p") && object.has("g")) {
                    p = object.getLong("p");
                    g = object.getLong("g");
                    System.out.println("p: " + p + ", g: " + g);
                    canSendA = true;
                } else if (object.has("B")) {
                    B = object.getLong("B");
                    System.out.println("B from server: " + B);
                    canCalculateS = true; // can calculate value S
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Client(args[0], Integer.parseInt(args[1]), args[2]).run();
    }
}
