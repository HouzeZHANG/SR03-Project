package Client;

import EnumLib.Ack;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Cette classe permet de recevoir les messages du serveur, par heritage de la classe Thread
 * @version 1.0
 */
public class ClientReceiveThread extends Thread {
	private final InputStream inputStream;
	private final Socket clientSocket;
	// thread flag to exit
	private Boolean closed = false;

	public ClientReceiveThread(Socket clientSocket) throws IOException {
		this.inputStream = clientSocket.getInputStream();
		this.clientSocket = clientSocket;
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

	synchronized private String readMessage() throws IOException {
		byte[] b = new byte[200];
		this.inputStream.read(b);
		return new String(b);
	}

	public void run() {
		String msg;
		try {
			while (!this.closed) {
				try {
					msg = readMessage();
					if (!msg.equals("")) {
						// Apres avoir recu la confirmation du serveur, quitter le boucle et terminer le programme
						if (msg.startsWith(Ack.CLIENT_EXIT.toString())) {
							System.out.println("Vous avez quitté la conversation. Merci pour votre utilisation !");
							this.closed = true;
							break;
						} else {
							// afficher le message recu
							System.out.println(msg);
						}
					}
				} catch (IOException ex) {
					System.out.println("Erreur: Message invalide");
					exit();
				}
			}
			exit();
		} catch (Exception ex) {
            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erreur: Déconnexion serveur");
        } 
	}
}
