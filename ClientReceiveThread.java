import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientReceiveThread extends Thread {

	private final InputStream inputStream;
	private Socket clientSocket;
	private String msg;
	private Boolean closed = false;

	public ClientReceiveThread(Socket sc, InputStream inputStream) {
		this.inputStream = inputStream;
		this.clientSocket = sc;
	}

	public void exit() {
		// Attendre que le serveur ferme la connexion
		try {
			Thread.sleep(20);
		} catch (InterruptedException ex) { 
            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
		// Fermer la connexion
		try {
			this.inputStream.close();
			this.clientSocket.close();
		} catch (IOException ex) { 
            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
	}

	public void run() {

		try {
			while (!this.closed) {
				try {
                    byte[] b = new byte[200];
                    this.inputStream.read(b);
                    this.msg = new String(b);
					if (this.msg != "") {
						synchronized (this) {
							// Apres avoir recu la confirmation du serveur, quitter le boucle et terminer le programme
							if (this.msg.startsWith("ACKUSEREXIT")) {
								System.out.println("Vous avez quitté la conversation. Merci pour votre utilisation !");
								this.closed = true;
								break;
							} else {
								System.out.println(this.msg);
								this.msg = null;
							}
						}
					}
				} catch (IOException ex) {
					exit();
					System.out.println("Erreur: Message invalide");
					break;
				}
			}
			if (this.closed) {
				exit();
			}
		} catch (Exception ex) { 
            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erreur: Déconnexion serveur");
        } 
	}
}
