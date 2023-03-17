import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;

public class ServeurChat {

    private static Hashtable<ServeurMessageReceptor, String> clients = new Hashtable<ServeurMessageReceptor, String>();
    private static int nb_client = 0;
    private static final int PORT = 10087;
    private static ServerSocket serverSocket;
    public static void main(String[] args) {
        try {
            System.out.println("[Serveur] Attente de connexion depuis le port " + PORT);
            serverSocket = new ServerSocket(PORT);

            // boucle infinie pour continuer à accepter les demandes entrantes
            while(true) {
                Socket comm = serverSocket.accept();
                if (comm.isConnected()){
                    // creation d'un nouveau thread
                    ServeurMessageReceptor newMessageReceptor = new ServeurMessageReceptor(comm, clients);
                    // stocke le message receptor dans le tableau
			        clients.put(newMessageReceptor, "");
                    // lance le thread
			        newMessageReceptor.start();
                    nb_client++;
                    System.out.println("[Serveur] Connecxion établie avec un client, " + nb_client + " clients en ligne.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ServeurChat.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
