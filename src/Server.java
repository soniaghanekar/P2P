import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(7734);
        while(true) {
           new RFCServer(listener.accept()).start();
        }
    }

    private static class RFCServer extends Thread {
        private Socket socket;

        private RFCServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Hi.. This is a new connection for a peer");

            try {
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);

                long peerUploadPort =  Long.parseLong(reader.readLine());
//                String s = reader.readLine();
                System.out.println("Peer port = " + peerUploadPort);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

}
