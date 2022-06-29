package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPStream implements Runnable {
    private final int port;
    private final boolean isServer;
    private final String playerName;
    private TCPStreamStatusListener listener = null;

    //Server:
    //this socket is the socket of the connected client
    //-----
    //Client:
    //this socket is the client socket
    private Socket socket = null;


    public final int WAIT_LOOP_IN_MILLIS = 30000; // 30 sec - standard wait time

    private TCPServer tcpServer = null;
    private TCPClient tcpClient = null;
    private int waitInMillis = WAIT_LOOP_IN_MILLIS;
    private String remoteEngine = "localhost";





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              constructor                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public TCPStream(int port, boolean asServer, String playerName) {
        this.port = port;
        this.isServer = asServer;
        this.playerName = playerName;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                   methods                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void run() {
        try {
            if(this.isServer) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.getClass().getSimpleName());
                sb.append(": note: this implementation will only accept *one* connection attempt as server");
                System.out.println(sb);

                this.tcpServer = new TCPServer();
                this.socket = this.tcpServer.getConnectedClientSocket();
            } else {
                this.tcpClient = new TCPClient();
                this.socket = this.tcpClient.getSocket();
            }

            if(this.listener != null) {
                this.listener.streamCreated(this);
            }
        } catch (IOException e) {
            this.listener.streamCreationFailed();
        }
    }


    public void checkConnected() throws IOException {
        if(this.socket == null) {
            String s = "no socket yet - should call connect first";
            System.out.println(s);
            throw new IOException(s);
        }
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       setter                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRemoteEngine(String remoteEngine) {
        this.remoteEngine = remoteEngine;
    }

    public void setStreamCreationListener(TCPStreamStatusListener listener) {
        this.listener = listener;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                       getter                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public InputStream getInputStream() throws IOException {
        this.checkConnected();
        return this.socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        this.checkConnected();
        return this.socket.getOutputStream();
    }

    public boolean isServer() {
        return this.isServer;
    }







    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                  private classes                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    class TCPServer {
        private ServerSocket serverSocket;

        public Socket getConnectedClientSocket() throws IOException {
            if(this. serverSocket == null) {
                this.serverSocket = new ServerSocket(port);
            }

            //debug information
            StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append(" (");
            sb.append(playerName);
            sb.append("): ");
            sb.append("connected");
            System.out.println(sb);

            //wait for client to connect
            this.serverSocket.setSoTimeout(waitInMillis);
            Socket connectedSocket = this.serverSocket.accept();

            //debug information
            sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append(" (");
            sb.append(playerName);
            sb.append("): ");
            sb.append("connected");
            System.out.println(sb);

            return connectedSocket;
        }

        public void kill() throws IOException {
            this.serverSocket.close();
        }
    }

    private class TCPClient {
        private boolean killed = false;

        public Socket getSocket() throws IOException {
            while(!this.killed) {
                try {
                    //debug information
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.getClass().getSimpleName());
                    sb.append(" (");
                    sb.append(playerName);
                    sb.append("): ");
                    sb.append("try to connect localhost port ");
                    sb.append(port);
                    System.out.println(sb);

                    return new Socket(TCPStream.this.remoteEngine, port);

                } catch (IOException e) {
                    //debug information
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.getClass().getSimpleName());
                    sb.append(" (");
                    sb.append(playerName);
                    sb.append("): ");
                    sb.append("connection failed / wait and re-try");
                    sb.append(port);
                    System.out.println(sb);

                    try {
                        Thread.sleep(waitInMillis);
                    } catch (InterruptedException ignore) {
                        //ignore
                    }
                }
            }
            throw new IOException("thread was killed before establishing a connection");
        }
    }
}
