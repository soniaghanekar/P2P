import java.io.*;
import java.net.*;

public class Peer {

    private String hostname;
    private UploadServer uploadServer;

    public static void main(String[] args) {
        try {
            Peer peer = new Peer();
            peer.uploadServer.start();
            Socket socket = new Socket("localhost", 7734);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(peer.hostname);
            writer.println(Integer.toString(peer.uploadServer.port));
            System.out.println("Sent Upload port= " + peer.uploadServer.port);

            socket.close();
        } catch (IOException e) {
            System.out.println("Server not found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Peer() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
            this.uploadServer = new UploadServer();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static class UploadServer extends Thread {
        ServerSocket uploadSocket;
        int port;
        UploadServer() {
            try {
                uploadSocket = new ServerSocket(0);
                port = uploadSocket.getLocalPort();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Socket accept = uploadSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}

