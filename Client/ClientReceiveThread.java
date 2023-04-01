package Client;

import EnumLib.Ack;
import EnumLib.BasicMsg;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;


/**
 * Cette classe permet de recevoir les messages du serveur, par heritage de la classe Thread
 * @version 1.0
 */
public class ClientReceiveThread extends Thread {
	private final InputStream inputStream;
	private final Socket clientSocket;
	// thread flag to exit
	private Boolean closed = false;
	private Date lastHeartBeatTime;

	public ClientReceiveThread(Socket clientSocket, Date lastHeartBeatTime) throws IOException {
		this.inputStream = clientSocket.getInputStream();
		this.clientSocket = clientSocket;
		this.lastHeartBeatTime = lastHeartBeatTime;
	}

	public void exit() {
		// Attendre que le serveur ferme la connexion
		try {
			Thread.sleep(20);
		} catch (InterruptedException ex) {
			System.out.println("Erreur: Thread interrompu");
//            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
		// Fermer la connexion
		try {
			this.inputStream.close();
			this.clientSocket.close();
		} catch (IOException ex) {
			System.out.println("Erreur: Fermeture de la connexion");
//            Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
        }

		System.exit(0);
	}

	synchronized private String readMessage() throws IOException, StringIndexOutOfBoundsException {
		byte[] b = new byte[200];
//		System.out.println("Waiting for message...");
		int len = this.inputStream.read(b);
//		System.out.println("Message received");
		return new String(b, 0, len);
	}

	public void run() {
		String msg;
		while (!this.closed) {
			try {
				msg = readMessage();
				if (!msg.equals("")) {
					// Apres avoir recu la confirmation du serveur, quitter le boucle et terminer le programme
					if (msg.startsWith(Ack.CLIENT_EXIT.toString())) {
						System.out.println("Vous avez quitté la conversation. Merci pour votre utilisation !");
						this.closed = true;
						break;
					} else if (msg.startsWith(BasicMsg.EXIT.toString())) {
						// Si le serveur a quitté la conversation, quitter le boucle et terminer le programme
						System.out.println("Le serveur a quitté la conversation. Merci pour votre utilisation !");
						this.closed = true;
						break;
					} else if (msg.startsWith(Ack.HEART_BEAT_ACK.toString())) {
						// update lastHeartBeatTime
						synchronized (this.lastHeartBeatTime) {
							this.lastHeartBeatTime = new Date();
						}
					} else {
						// afficher le message recu
						System.out.println(msg);
					}
				}
				else{
					System.out.println("No message received");
				}
			} catch (Exception ex) {
				System.out.println("Erreur: Reception de message");
				if (!this.clientSocket.isClosed()) { exit(); }
			}
		}
		if (!this.clientSocket.isClosed()) { exit(); }
	}
}
