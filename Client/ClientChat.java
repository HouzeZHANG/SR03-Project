package Client;

import EnumLib.BasicMsg;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;


/**
 * ClientChat class
 * @version 1.1
 */
public class ClientChat {
    static private Date lastHeartBeatTime;
    /**
     * Main method
     * @param args: port number used by the server
     */
    public static void main(String[] args) {
        try{
            // create socket
            Socket clientSocket = new Socket (args[0], Integer.parseInt(args[1]));
            System.out.println("Connecté : " + clientSocket +
                    " " + clientSocket.getInetAddress() +
                    " " + clientSocket.getPort());

            // create send and receive threads
            ClientSendThread sendThread = new ClientSendThread(clientSocket.getOutputStream());
		    ClientReceiveThread receiveThread = new ClientReceiveThread(clientSocket);

            // create heart beat threads
            HeartBeatSenderThread heartBeatSenderThread = new HeartBeatSenderThread(1000, clientSocket);
            HeartBeatReceiverThread heartBeatReceiverThread = new HeartBeatReceiverThread(5000,
                    1000, lastHeartBeatTime);

            // handler called on Control-C pressed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    sendThread.send(BasicMsg.EXIT.toString());
                    System.out.println("Client.ClientChat: Client exited");
                } catch (IOException e) {
                    System.out.println("Client.ClientChat Error: " + e);
                }
            }));

            // start send and receive threads
            sendThread.start();
            receiveThread.start();

            // start heart beat threads
            heartBeatSenderThread.start();
            heartBeatReceiverThread.start();
        } catch (Exception e) {
			System.out.println("Client.ClientChat Error: " + e);
		}
    }
}
