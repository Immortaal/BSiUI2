package chat;

import lombok.Data;

import java.math.BigInteger;
import java.net.Socket;

/**
 * Created by Beata Kalis on 2016-10-19.
 * class represents Client and contains necessary information for server
 */
@Data
public class ClientInfo {

    private Socket socket;

    private BigInteger secret; // shared secret between client and server

    private String encryption;

}
