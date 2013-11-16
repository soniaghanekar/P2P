import java.io.IOException;
import java.net.Socket;

public class Peer {

    private int id;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 7734);
        } catch (IOException e) {
            System.out.println("Server not found");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
