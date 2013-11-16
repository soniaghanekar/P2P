import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static List<Peer> peerList = new LinkedList<Peer>();

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(7734);
        while(true) {
            new RFCServer(listener.accept()).start();
            printPeers();
        }
    }

    private static class RFCServer extends Thread {

        private Socket socket;
        BufferedReader reader;

        private RFCServer(Socket socket) {
            this.socket = socket;
            try {
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("Hi.. This is a new connection for a peer");

            try {
                registerPeer();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void registerPeer() throws IOException {
            String peerHostName = reader.readLine();
            int peerUploadPort = Integer.parseInt(reader.readLine());
            peerList.add(new Peer(peerHostName, peerUploadPort));
        }

    }
    private static class Peer {

        String hostname;
        int port;
        private Peer(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

    }

    private static void printPeers() {
        for(Peer peer: peerList) {
            System.out.println("hostname = " + peer.hostname + " port = " + peer.port);
        }
    }

}
