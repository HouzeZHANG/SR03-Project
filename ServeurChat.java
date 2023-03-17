import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Hashtable;

public class ServeurChat {

    private static Hashtable<ServeurMessageReceptor, String> listClient = new Hashtable<ServeurMessageReceptor, String>();
    private static int nb_client = 0;
    private static final int PORT = 10087;
    private static ServerSocket serverSocket;
    public static void main(String[] args) {
        try {
            System.out.println("[Serveur] Attente de connexion depuis le port " + PORT);
            serverSocket = new ServerSocket(PORT);
            while(true) {
                Socket comm = serverSocket.accept();
                if (comm.isConnected()){
                    ServeurMessageReceptor newMessageReceptor = new ServeurMessageReceptor(comm, listClient);
			        listClient.put(newMessageReceptor, "");
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
