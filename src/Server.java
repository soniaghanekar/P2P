import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static List<Peer> peerList = new LinkedList<Peer>();
    static List<RFCInfo> rfcInfoList = new LinkedList<RFCInfo>();

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Server usage: Server #port");
            System.exit(-1);
        }

        ServerSocket listener = new ServerSocket(Integer.parseInt(args[0]));
        while(true) {
            new RFCServer(listener.accept()).start();
        }
    }

    private static class RFCServer extends Thread {

        private Socket socket;
        BufferedReader reader;
        PrintWriter writer;

        private RFCServer(Socket socket) {
            this.socket = socket;
            try {
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.writer = new PrintWriter(socket.getOutputStream(), true);
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
                    System.out.println("\nReceived request from peer");
                    System.out.println(line);
                    String method = line.split(" ")[0];
                    if(method.equals("ADD"))
                        parseAdd(line);
                    else if(method.equals("LOOKUP"))
                        parseLookup(line);
                    else if(method.equals("LIST"))
                        parseList();
                    else
                        writer.println("P2P-CI/1.0 400 BAD REQUEST");
                }

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        private void parseList() throws IOException {
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());

            writer.println(rfcInfoList.size());
            writer.println("P2P-CI/1.0 200 OK");
            for(RFCInfo rfc: rfcInfoList) {
                writer.println("RFC " + rfc.number + " " + rfc.title + " " + rfc.hostname + " " +
                        rfc.port);
            }
        }

        private void parseLookup(String line) throws IOException {
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
            int rfcNo = Integer.parseInt(line.split(" ")[2]);
            List<Peer> peers = searchPeersHavingRfc(rfcNo);
            String title = getTitleForRfc(rfcNo);
            sendResponse(peers, rfcNo, title);
        }

        private String getTitleForRfc(int rfcNo) {
            for(RFCInfo rfc: rfcInfoList)
                if(rfc.number == rfcNo)
                    return rfc.title;
            return null;
        }

        private void sendResponse(List<Peer> peers, int rfcNo, String title) {
            writer.println(peers.size());
            if(peers.size() == 0)
                writer.println("P2P-CI/1.0 404 NOT FOUND");
            else {
                writer.println("P2P-CI/1.0 200 OK");
                for(Peer peer: peers) {
                    writer.println("RFC " + rfcNo + " " + title + " " + peer.hostname + " " + peer.port);
                }
            }
        }

        private List<Peer> searchPeersHavingRfc(int rfcNo) {
            List<Peer> peersHavingRfc = new ArrayList<Peer>();
            for(RFCInfo rfc: rfcInfoList) {
                if(rfc.number == rfcNo)
                    peersHavingRfc.add(new Peer(rfc.hostname, rfc.port));
            }
            return peersHavingRfc;
        }

        private void parseAdd(String line) throws IOException {
            int number = Integer.parseInt(line.split(" ")[2]);
            String s = reader.readLine();
            System.out.println(s);
            String host = s.split(" ")[1];
            s = reader.readLine();
            System.out.println(s);
            int port = Integer.parseInt(s.split(" ")[1]);
            s = reader.readLine();
            System.out.println(s);
            String title = s.split(":")[1].trim();
            rfcInfoList.add(new RFCInfo(number, title, host, port));

            ArrayList<Peer> peers = new ArrayList<Peer>();
            peers.add(new Peer(host, port));
            sendResponse(peers, number, title);
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
        int port;

        private RFCInfo(int number, String title, String hostname, int port) {
            this.number = number;
            this.title = title;
            this.hostname = hostname;
            this.port = port;
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
