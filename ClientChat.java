import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientChat {
    public static void main(String[] args) {
        try{
            Socket clientSocket = new Socket ("localhost", 10087);
            System.out.println("Connecté : " + clientSocket);
            OutputStream out = clientSocket.getOutputStream();
            InputStream in = clientSocket.getInputStream();

            ClientSendThread sendThread = new ClientSendThread(out);
		    ClientReceiveThread receiveThread = new ClientReceiveThread(clientSocket, in);
            sendThread.start();
            receiveThread.start();

        } catch (Exception e) {
			System.out.println("ClientChat Error: " + e);
		}
    }
}
