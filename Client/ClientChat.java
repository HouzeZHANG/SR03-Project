package Client;

import java.io.IOException;
import java.net.Socket;


/**
 * ClientChat class
 * @version 1.1
 */
public class ClientChat {
    /**
     * Main method
     * @param args: port number used by the server
     */
    public static void main(String[] args) {
        try{
            System.out.println(args[0]);
            Socket clientSocket = new Socket ("localhost", Integer.parseInt(args[0]));
            System.out.println("Connecté : " + clientSocket +
                    " " + clientSocket.getInetAddress() +
                    " " + clientSocket.getPort());
            ClientSendThread sendThread = new ClientSendThread(clientSocket.getOutputStream());
		    ClientReceiveThread receiveThread = new ClientReceiveThread(clientSocket);

            // handler called on Control-C pressed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    sendThread.send("exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));

            sendThread.start();
            receiveThread.start();
        } catch (Exception e) {
			System.out.println("Client.ClientChat Error: " + e);
		}
    }
}
