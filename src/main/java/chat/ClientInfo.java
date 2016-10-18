package chat;

import lombok.Data;

import java.net.Socket;

/**
 * Created by Beata on 2016-10-19.
 */
@Data
public class ClientInfo {

    private Socket socket;

    private Long secret; // shared secret between client and server

    private String encryption;

}
