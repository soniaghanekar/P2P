import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Peer {

    private int id;

    public static void main(String[] args) {
        try {
            UploadServer uploadServer = new UploadServer();
            uploadServer.start();
            Socket socket = new Socket("localhost", 7734);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(Integer.toString(uploadServer.port));
            System.out.println("Sent Upload port= " + uploadServer.port);

            socket.close();
        } catch (IOException e) {
            System.out.println("Server not found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

