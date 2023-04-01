package Client;

import EnumLib.BasicMsg;

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
            Socket clientSocket = new Socket (args[0], Integer.parseInt(args[1]));
            System.out.println("Connecté : " + clientSocket +
                    " " + clientSocket.getInetAddress() +
                    " " + clientSocket.getPort());
            ClientSendThread sendThread = new ClientSendThread(clientSocket.getOutputStream());
		    ClientReceiveThread receiveThread = new ClientReceiveThread(clientSocket);

            // handler called on Control-C pressed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    sendThread.send(BasicMsg.EXIT.toString());
                    System.out.println("Client.ClientChat: Client exited");
                } catch (IOException e) {
                    System.out.println("Client.ClientChat Error: " + e);
                }
            }));

            sendThread.start();
            receiveThread.start();
        } catch (Exception e) {
			System.out.println("Client.ClientChat Error: " + e);
		}
    }
}
