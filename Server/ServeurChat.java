package Server;

import EnumLib.BasicMsg;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;


/**
 * Serveur de chat
 *
 */
public class ServeurChat {
    // hashtable est thread-safe, peut etre utilisee par plusieurs socketsThreads
    private static final Hashtable<SocketThread, String> socketThreadToID = new Hashtable<>();
    private static int PORT;
    private static ServerSocket serverSocket;

    @Override
    public String toString() {
        return "ServeurChat{" +
                "socketThreadToID=" + socketThreadToID +
                ", PORT=" + PORT +
                ", serverSocket=" + serverSocket +
                '}';
    }

    /**
     * @param args port du serveur à utiliser
     */
    public static void main(String[] args) {
        try {
            // creation du socket serveur
            PORT = Integer.parseInt(args[0]);
            System.out.println("[Serveur] Attente de connexion depuis le port " + PORT);
            serverSocket = new ServerSocket(PORT);

            // create a thread to check if a client is disconnected
            Hashtable<SocketThread, Date> threadToLastHeartBeat = new Hashtable<SocketThread, Date>();
            HeartBeatTimeOutChecker heartBeatTimeOutChecker = new HeartBeatTimeOutChecker(threadToLastHeartBeat,
                    5000, 2000);
            heartBeatTimeOutChecker.start();

            // handler called on Control-C pressed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Serveur] ServeurChat exited");
                for (SocketThread socketThread : socketThreadToID.keySet()) {
                    try {
                        socketThread.send(socketThread, BasicMsg.EXIT.toString());
                    } catch (IOException e) {
                        System.out.println("[Serveur] ServeurChat Error: " + e);
                    }
                }
            }));

            // boucle infinie pour continuer à accepter les demandes entrantes
            while(true) {
                Socket comm = serverSocket.accept();
                if (comm.isConnected()){
                    // creation d'un nouveau thread
                    SocketThread socketThread = new SocketThread(comm, socketThreadToID, threadToLastHeartBeat);
                    // stocke le message receptor dans le tableau
			        socketThreadToID.put(socketThread, "");
                    // lance le thread
			        socketThread.start();
                    System.out.println("[Serveur] Connexion établie avec un client, " +
                            socketThreadToID.size() + " clients en ligne.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ServeurChat.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
