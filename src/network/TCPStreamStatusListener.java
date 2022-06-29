package network;

/**
 * Listener notifies when a tcp stream is set up
 */
public interface TCPStreamStatusListener {

    /**
     * This method is called when the connection is established
     * @param tcpStream the TCPStream from where to get the in- and output stream
     */
    void streamCreated(TCPStream tcpStream);

    /**
     * This method is called when the connection couldn't be established.
     * Most likely, when the time to wait for a client to connect is over.
     */
    void streamCreationFailed();
}
