import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientReceiveThread extends Thread {

	private final InputStream inputStream;
	private Socket client;
	private String msg;
	private Boolean closed = false;

	public ClientReceiveThread(Socket client, InputStream inputStream) {
		this.inputStream = inputStream;
		this.client = client;
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
			this.client.close();
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
							// Quitter la boucle si le socket a fermé, sinon afficher le message sur la console
							if (this.msg.equals("Vous avez quitté la conversation")) {
								System.out.println("Merci pour votre utilisation !");
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
					System.out.println("Erreur message");
					break;
				}
			}
			if (this.closed) {
				exit();
			}
		} catch (Exception ex) { 
            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Déconnexion serveur");
        } 
	}
}
