import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientChat {
    public static void main(String[] args) {
        try{
            Socket client = new Socket ("localhost", 10087);
            System.out.println("Connecté : " + client);
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            ClientSendThread sendThread = new ClientSendThread(out);
		    ClientReceiveThread receiveThread = new ClientReceiveThread(client, in);
            sendThread.start();
            receiveThread.start();

        } catch (Exception e) {
			System.out.println("ClientChat Error: " + e);
		}
    }
}
