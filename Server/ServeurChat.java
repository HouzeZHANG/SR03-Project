package Server;

import java.net.ServerSocket;
import java.net.Socket;
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
    private static final Hashtable<SocketThread, String> socketThreadToID = new Hashtable<SocketThread, String>();
    private static final HashSet<String> clientNames = new HashSet<String>();
    private static int PORT;
    private static ServerSocket serverSocket;

    /**
     * @param args port du serveur à utiliser
     */
    public static void main(String[] args) {
        try {
            PORT = Integer.parseInt(args[0]);
            System.out.println("[Serveur] Attente de connexion depuis le port " + PORT);
            serverSocket = new ServerSocket(PORT);

            // boucle infinie pour continuer à accepter les demandes entrantes
            while(true) {
                Socket comm = serverSocket.accept();
                if (comm.isConnected()){
                    // creation d'un nouveau thread
                    SocketThread socketThread = new SocketThread(comm, socketThreadToID, clientNames);
                    // stocke le message receptor dans le tableau
			        socketThreadToID.put(socketThread, "");
                    // lance le thread
			        socketThread.start();
                    System.out.println("[Serveur] Connexion établie avec un client, " + socketThreadToID.size() + " clients en ligne.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ServeurChat.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
