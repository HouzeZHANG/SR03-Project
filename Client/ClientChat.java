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

            // create heart beat time, shared by all threads
            Date lastHeartBeatTime = new Date();

            // create send and receive threads
            ClientSendThread sendThread = new ClientSendThread(clientSocket.getOutputStream());
		    ClientReceiveThread receiveThread = new ClientReceiveThread(clientSocket, lastHeartBeatTime);

            // create heart beat threads
            HeartBeatSenderThread heartBeatSenderThread = new HeartBeatSenderThread(1000, clientSocket);
            HeartBeatTimeOutChecker heartBeatTimeOutChecker = new HeartBeatTimeOutChecker(5000,
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
            heartBeatTimeOutChecker.start();
        } catch (Exception e) {
			System.out.println("Client.ClientChat Error: " + e);
		}
    }
}
