import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {

    private String hostname;
    private UploadServer uploadServer;
    private Thread thread;

    BufferedReader reader;
    PrintWriter writer;
    Socket socket;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: Peer server-hostname #portno");
            System.exit(-1);
        }
        Peer peer = new Peer(args[0], Integer.parseInt(args[1]));

        peer.sendPeerInfo(peer);
        peer.sendRFCList();

        Scanner scanner = new Scanner(System.in);
        Boolean shouldContinue = true;
        while (shouldContinue) {
            System.out.println("\nWhat would you like to do?\n1. Lookup and download an RFC \n2. List of all RFCs available in the system" +
                    "\n3. Exit\nEnter your choice");
            switch (Integer.parseInt(scanner.nextLine())) {
                case 1:
                    peer.lookup(scanner);
                    break;

                case 2:
                    peer.listAllRFCs();
                    break;

                case 3:
                    peer.exit();
                    shouldContinue = false;
                    break;

                default:
                    System.out.println("Enter a valid option");

            }
        }
    }

    private void exit() {
        try {
            thread.interrupt();
            this.uploadServer.uploadSocket.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
            for (int i = 0; i < len; i++)
                System.out.println(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void lookup(Scanner scanner) {
        System.out.println("Enter the RFC No. you would like to lookup: ");
        int rfcNo = Integer.parseInt(scanner.nextLine());
        List<PeerInfo> peerList = sendLookup(rfcNo);
        if (peerList != null) {
            System.out.println("Would you want to download the RFC from any of the above peers? (y or n)");
            switch (scanner.nextLine().toLowerCase().charAt(0)) {
                case 'y':
                    System.out.println("Enter hostname of the peer from which you would like to download");
                    String host = scanner.nextLine();
                    PeerInfo peer = getPeerFromHostnameAndRfc(host, rfcNo, peerList);
                    if (peer == null)
                        System.out.println("Hostname " + host + " does not contain RFC No. " + rfcNo);
                    else {
                        getRfcFromHost(peer, rfcNo);
                        File rfc = new File("../RFC/" + rfcNo);
                        sendRFCInfo(rfc);
                    }
                default:
                    break;
            }
        }
        else {
            System.out.println("RFC No. " + rfcNo + " not found on any of the peers");
        }
    }

    private void getRfcFromHost(PeerInfo peer, int rfcNo) {
        try {
            Socket clientSocket = new Socket(peer.hostname, peer.port);
            PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            clientWriter.println("GET RFC " + rfcNo + " P2P-CI/1.0");
            clientWriter.println("Host: " + peer.hostname);
            clientWriter.println("OS: " + System.getProperty("os.name"));

            byte[] bytearray = new byte[1024];
            InputStream is = clientSocket.getInputStream();
            FileOutputStream fos = new FileOutputStream("../RFC/" + rfcNo);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int count;
            while ((count = is.read(bytearray, 0, bytearray.length)) > 0)
                bos.write(bytearray, 0, count);
            bos.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private PeerInfo getPeerFromHostnameAndRfc(String hostname, int rfcNo, List<PeerInfo> peerList) {
        for (PeerInfo peer : peerList)
            if (peer.hostname.equals(hostname) && peer.rfcNo == rfcNo)
                return peer;
        return null;
    }

    private List<PeerInfo> sendLookup(int rfcNo) {
        writer.println("LOOKUP RFC " + rfcNo + " P2P-CI/1.0");
        writer.println("Host: " + hostname);
        writer.println("Port: " + uploadServer.port);
        return readLookupResponse(rfcNo);
    }

    private List<PeerInfo> readLookupResponse(int rfcNo) {
        try {
            System.out.println("\nReceived response from server");
            int len = Integer.parseInt(reader.readLine());
            System.out.println(reader.readLine());
            if (len != 0) {
                List<PeerInfo> peersWithRfc = new ArrayList<PeerInfo>();
                for (int i = 0; i < len; i++) {
                    String line = reader.readLine();
                    System.out.println(line);
                    String[] s = line.split(" ");
                    peersWithRfc.add(new PeerInfo(s[s.length - 2], Integer.parseInt(s[s.length - 1]), rfcNo));
                }
                return peersWithRfc;
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private void sendRFCList() {
        File[] rfcs = new File("../RFC").listFiles();
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
        for (int i = 0; i < noOfLines; i++)
            System.out.println(reader.readLine());
    }

    private void sendPeerInfo(Peer peer) {
        writer.println(peer.hostname);
        writer.println(Integer.toString(peer.uploadServer.port));
    }

    private void connectToServer(String hostname, int port) throws IOException {
        this.socket = new Socket(hostname, port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public Peer(String serverHostname, int port) {
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
            this.uploadServer = new UploadServer();
            thread = new Thread(uploadServer);
            thread.start();
            connectToServer(serverHostname, port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class UploadServer implements Runnable {
        ServerSocket uploadSocket;
        int port;

        UploadServer() {
            try {
                uploadSocket = new ServerSocket(65411);
                port = uploadSocket.getLocalPort();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    new PeerRFCServer(uploadSocket.accept()).start();
                }
            } catch (IOException e) {
                System.out.println("Closing the upload server socket");
            }
        }

        private class PeerRFCServer extends Thread {
            private Socket serverSocket;
            private PrintWriter serverWriter;
            private BufferedReader serverReader;

            public PeerRFCServer(Socket socket) {
                this.serverSocket = socket;
                try {
                    this.serverWriter = new PrintWriter(serverSocket.getOutputStream(), true);
                    this.serverReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public void run() {
                try {
                    String request = serverReader.readLine();
                    System.out.println(request);
                    String[] split = request.split(" ");
                    if (!(split[0]).equals("GET"))
                        serverWriter.println("P2P-CI/1.0 400 BAD REQUEST");
                    else {
                        File rfc = new File("../RFC/" + split[2]);
                        byte[] bytearray = new byte[(int) rfc.length()];
                        FileInputStream fin = new FileInputStream(rfc);
                        BufferedInputStream bin = new BufferedInputStream(fin);
                        bin.read(bytearray, 0, bytearray.length);
                        OutputStream os = serverSocket.getOutputStream();
                        os.write(bytearray, 0, bytearray.length);
                    }
                } catch (FileNotFoundException f) {
                    serverWriter.println("P2P-CI/1.0 404 NOT FOUND");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
    }

    private class PeerInfo {
        String hostname;
        int port;
        int rfcNo;

        private PeerInfo(String hostname, int port, int rfcNo) {
            this.hostname = hostname;
            this.port = port;
            this.rfcNo = rfcNo;
        }
    }
}

