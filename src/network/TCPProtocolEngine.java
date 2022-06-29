package network;

import maumau.player.Player;

import java.io.InputStream;
import java.io.OutputStream;

public interface TCPProtocolEngine extends Player {

    void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener listener);

    void handleConnection(InputStream is, OutputStream os);
}
