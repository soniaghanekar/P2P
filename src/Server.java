import java.io.IOException;
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
        }
    }

}
