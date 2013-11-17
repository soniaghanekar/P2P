import java.io.*;
import java.net.*;

public class Peer {

    private String hostname;
    private UploadServer uploadServer;
    PrintWriter writer;
    Socket socket;

    public static void main(String[] args) {
        Peer peer = new Peer();
        try {

            peer.sendPeerInfo(peer);
            System.out.println("Sent Upload port= " + peer.uploadServer.port);

            peer.sendRFCList();

        } finally {
            try {
                peer.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendRFCList() {
        File[] rfcs = new File("../RFC's").listFiles();

        for (File rfc : rfcs) {
            if (rfc.isFile())
                sendRFCInfo(rfc);
        }
    }

    private void sendRFCInfo(File rfc) {
        String name = rfc.getName();
        System.out.println(name);
        try {

            String title = new BufferedReader(new FileReader(rfc)).readLine().split(":")[1].trim();
            System.out.println(title);
            writer.println("ADD RFC " + name + " P2P-CI/1.0");
            writer.println("Host: " + hostname);
            writer.println("Port: " + uploadServer.port);
            writer.println("Title: " + title);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void sendPeerInfo(Peer peer) {
        writer.println(peer.hostname);
        writer.println(Integer.toString(peer.uploadServer.port));
    }

    private void connectToServer() throws IOException {
        this.socket = new Socket("localhost", 7734);
        this.writer = new PrintWriter(socket.getOutputStream(), true);

    }

    public Peer() {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
            this.uploadServer = new UploadServer();
            uploadServer.start();
            connectToServer();

        } catch (Exception e) {
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

