package client;

import encryption.Encryption;
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
import java.math.BigInteger;
import java.net.Socket;
import java.util.Random;

/**
 * Created by Beata Kalis on 2016-10-12.
 * Class represents client, it is a thread connecting to the server
 */
public class Client extends JFrame implements Runnable {

    private BufferedReader in;
    private PrintWriter out;
    private JTextField inputMessage;
    private JTextArea messagesList;
    private final String clientName;
    private final String encryption;
    private BigInteger p; // prime number
    private BigInteger g; // primitive root modulo n
    private BigInteger B; // value received from server
    private final BigInteger a; // client local secret
    private BigInteger S; // shared secret
    private boolean keys = true;
    private boolean canSendA = false;
    private boolean canCalculateS = false;
    private boolean canSetEncryption = false;
    private BigInteger A; // value send to server


    /**
     * @param host server hostname
     * @param port server host
     * @param name client name
     * @param encryption encryption way
     */
    private Client(String host, int port, String name, String encryption) {
        setTitle(name);
        setBounds(10, 10, 400, 400);

        this.clientName = name;
        this.encryption = encryption;
        String clientLocalSecret = "" + new Random().nextInt(1000);
        this.a = new BigInteger(clientLocalSecret);
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

    /**
     * method to create GUI: input to write a message and field to display conversation
     */
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

    /**
     * method to set listener on input textField (automatic send a message to server when push enter)
     */
    private void setListeners() {
        inputMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = inputMessage.getText();
                sendMessage(msg);
                inputMessage.setText(null);
            }
        });
    }

    /**
     * method check type of encryption, then cypher a message, encode using Base64 and put it in JSON object;
     * then send to the server
     * @param msg message to send to server
     */
    private void sendMessage(String msg) {
        byte[] encryptMsg;
        String encodeMsg;
        byte[] encryptName;
        String encodeName;
        if (encryption.equals("cezar")) {
            encryptMsg = Encryption.caesarCipher(msg, S, true);
        } else if (encryption.equals("xor")) {
            encryptMsg = Encryption.xor(msg, S);
        } else {
            //none
            encryptMsg = msg.getBytes();
        }
        encryptName = clientName.getBytes();
        encodeMsg = Base64.encode(encryptMsg);
        encodeName = Base64.encode(encryptName);

        JSONObject object = new JSONObject();
        object.put("msg", encodeMsg);
        object.put("from", encodeName);
        out.println(object.toString());
    }

    /**
     * client is a thread, it is sending requests and waiting for responses
     */
    public void run() {
        while (true) {
            if (keys) {
                keysRequest();
            }
            if (canSendA) {
                sendA();
            }
            if (canCalculateS) {
                calculateS();
            }

            if (canSetEncryption) {
                setEncryption();
            }
            try {
                String response = in.readLine();
                if (response == null || response.equals("")) {
                    System.exit(0);
                }
                JSONObject object = new JSONObject(response);
                if (object.has("msg")) {
                    displayReceivedMessage(object);
                } else if (object.has("p") && object.has("g")) {
                    setKeys(object);
                } else if (object.has("b")) {
                    setB(object);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * method to send request for keys (p and g)
     */
    private void keysRequest() {
        JSONObject object = new JSONObject();
        object.put("request", "keys");
        out.println(object);
        keys = false;
    }

    /**
     * method to send value A to the server
     */
    private void sendA() {
        BigInteger tmp = g.pow(a.intValue());
        A = tmp.mod(p);
        System.out.println("Client A: " + A);
        JSONObject key = new JSONObject();
        key.put("a", A);
        out.println(key);
        canSendA = false; // send value A only once
    }

    /**
     * method to calculate shared secret S
     */
    private void calculateS() {
        BigInteger tmp = B.pow(a.intValue());
        S = tmp.mod(p);
        System.out.println("Client S: " + S);
        canCalculateS = false; // only once calculate value S;
        canSetEncryption = true;
    }

    /**
     * method to set encryption (none, cezar, xor)
     */
    private void setEncryption() {
        JSONObject object = new JSONObject();
        object.put("encryption", encryption);
        out.println(object);
        canSetEncryption = false;
    }

    /**
     * method to display received messages
     * first it is important to check type of encryption, then decipher a message and decode from Base64
     * @param object received JSON
     */
    private void displayReceivedMessage(JSONObject object) {
        byte[] decodedBytes = Base64.decode(object.getString("msg"));
        byte[] decryptedBytes;
        byte[] decodedBytesName = Base64.decode(object.getString("from"));
        if (encryption.equals("cezar")) {
            decryptedBytes = Encryption.caesarCipher(new String(decodedBytes), S, false);
        } else if (encryption.equals("xor")) {
            decryptedBytes = Encryption.xor(new String(decodedBytes), S);
        } else {
            decryptedBytes = decodedBytes;

        }
        messagesList.append(new String(decodedBytesName) + ": " + new String(decryptedBytes) + "\n");
    }

    /**
     * method to set received keys (p and g)
     * assertion that p > 0 and g > 1
     * @param object received JSON
     */
    private void setKeys(JSONObject object) {
        if(object.getBigInteger("p").compareTo(new BigInteger("0")) == 1 &&
                object.getBigInteger("g").compareTo(new BigInteger("1")) == 1) {
            p = object.getBigInteger("p");
            g = object.getBigInteger("g");
            canSendA = true;
        }
    }

    /**
     * method to set received value B, this value is necessary to calculate secret S
     * @param object received JSON
     */
    private void setB(JSONObject object) {
        B = object.getBigInteger("b");
        System.out.println("B from server: " + B);
        canCalculateS = true; // can calculate value S
    }

    public static void main(String[] args) {
        new Client(args[0], Integer.parseInt(args[1]), args[2], args[3]).run();
    }
}
