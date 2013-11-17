import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static List<Peer> peerList = new LinkedList<Peer>();
    static List<RFCInfo> rfcInfoList = new LinkedList<RFCInfo>();

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(7734);
        while(true) {
            new RFCServer(listener.accept()).start();
            printPeers();
            printRFCs();
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
                parseMessages();
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

        private void parseMessages() {
            try {
                String line;
                while((line = reader.readLine()) != null) {
                    String method = line.split(" ")[0];
                    System.out.println(method);
                    if(method.equals("ADD"))
                        parseAdd(line);
//                    else if(method.equals("LOOKUP"))
//                        parseLookup(line);
//                    else
//                        parseList(line);
                }


            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        private void parseAdd(String line) throws IOException {
            int number = Integer.parseInt(line.split(" ")[2]);
            String host = reader.readLine().split(" ")[1];
            reader.readLine();
            String title = reader.readLine().split(" ")[1];
            rfcInfoList.add(new RFCInfo(number, title, host));
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

    private static class RFCInfo {
        int number;
        String title;
        String hostname;

        private RFCInfo(int number, String title, String hostname) {
            this.number = number;
            this.title = title;
            this.hostname = hostname;
        }
    }

    private static void printPeers() {
        System.out.println("Peers");
        for(Peer peer: peerList) {
            System.out.println("hostname = " + peer.hostname + " port = " + peer.port);
        }
    }

    private static void printRFCs() {
        System.out.println("RFCs");
        for(RFCInfo rfc: rfcInfoList) {
            System.out.println("number = " + rfc.number + " title = " + rfc.title + " hostname = " + rfc.hostname);
        }
    }

}
