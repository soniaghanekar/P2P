import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private Socket socket;

    public void listenSocket() {
        try {
            ServerSocket serverSocket = new ServerSocket(7734);
            socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
