import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {

    private String hostname;
    private UploadServer uploadServer;
    BufferedReader reader;
    PrintWriter writer;
    Socket socket;

    public static void main(String[] args) {
        Peer peer = new Peer();
        try {

            peer.sendPeerInfo(peer);
            peer.sendRFCList();

            Scanner scanner = new Scanner(System.in);
            Boolean shouldContinue = true;
            while (shouldContinue) {
                System.out.println("\nWhat would you like to do?\n1. Lookup an RFC \n2. List of all RFCs available in the system\n" +
                        "3. Download an RFC\n4.Exit\nEnter your choice");
                switch (Integer.parseInt(scanner.nextLine())) {
                    case 1:
                        peer.lookup(scanner);
                        break;

                    case 2:
                        peer.listAllRFCs();
                        break;

                    case 3:
                        downloadRFC(scanner);
                        break;

                    case 4:
                        shouldContinue = false;
                        return;

                    default:
                        System.out.println("Enter a valid option");

                }
            }

        } finally {
            try {
                peer.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listAllRFCs() {
        writer.println("LIST ALL P2P-CI/1.0");
        writer.println("Host: " + hostname);
        writer.println("Port: " + uploadServer.port);

        try {
            System.out.println("\nReceived response from server");

            int len = Integer.parseInt(reader.readLine());
            System.out.println(reader.readLine());
            for(int i=0; i<len; i++)
                System.out.println(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void lookup(Scanner scanner) {
        System.out.println("Enter the RFC No. you would like to lookup: ");
        int rfcNo = Integer.parseInt(scanner.nextLine());
        sendLookup(rfcNo);
    }

    private void sendLookup(int rfcNo) {
        writer.println("LOOKUP RFC " + rfcNo + " P2P-CI/1.0");
        writer.println("Host: " + hostname);
        writer.println("Port: " + uploadServer.port);
        readLookupResponse();
    }

    private void readLookupResponse() {
        try {
            System.out.println("\nReceived response from server");
            int len = Integer.parseInt(reader.readLine());
            System.out.println(reader.readLine());
            if(len != 0) {
                List<PeerInfo> peersWithRfc = new ArrayList<PeerInfo>();
                for(int i=0; i<len; i++) {
                    String line = reader.readLine();
                    System.out.println(line);
                    String[] s = line.split(" ");
                    peersWithRfc.add(new PeerInfo(s[s.length-2], Integer.parseInt(s[s.length-1])));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static void downloadRFC(Scanner scanner) {
        System.out.println("Enter the RFC number you would like to download");
        int rfcNo = Integer.parseInt(scanner.nextLine());
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
        try {
            String title = new BufferedReader(new FileReader(rfc)).readLine().split(":")[1].trim();
            writer.println("ADD RFC " + name + " P2P-CI/1.0");
            writer.println("Host: " + hostname);
            writer.println("Port: " + uploadServer.port);
            writer.println("Title: " + title);
            reader.readLine();
            System.out.println("\nReceived response from server");
            printResponse(2);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void printResponse(int noOfLines) throws IOException {
        for(int i=0; i<noOfLines; i++)
            System.out.println(reader.readLine());
    }

    private void sendPeerInfo(Peer peer) {
        writer.println(peer.hostname);
        writer.println(Integer.toString(peer.uploadServer.port));
    }

    private void connectToServer() throws IOException {
        this.socket = new Socket("localhost", 7734);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
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

    private class PeerInfo {
        String hostname;
        int port;

        private PeerInfo(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }
    }
}

