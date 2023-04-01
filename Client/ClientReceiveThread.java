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

	// last time the client received a heart beat ack, initialized when the client is created
	Date lastHeartBeatTime = new Date();

	public ClientReceiveThread(Socket clientSocket) throws IOException {
		this.inputStream = clientSocket.getInputStream();
		this.clientSocket = clientSocket;
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
		int len = this.inputStream.read(b);
		return new String(b, 0, len);
	}

	private boolean heartBeatAckTimeout(Date latestHeartBeatAck) {
		return latestHeartBeatAck.getTime() - lastHeartBeatTime.getTime() > 10000;
	}

	public void run() {
		String msg;
		while (!this.closed) {
			// Si le client n'a pas recu de heartBeatAck du serveur pendant 10 secondes,
			// quitter le boucle et terminer le programme
			if (heartBeatAckTimeout(new Date())) {
				System.out.println("Heart beat timeout, exiting...");
				this.closed = true;
				break;
			}

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
						this.lastHeartBeatTime = new Date();
					} else {
						// afficher le message recu
						System.out.println(msg);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.toString());
				if (!this.clientSocket.isClosed()) { exit(); }
			}
		}
		if (!this.clientSocket.isClosed()) { exit(); }
	}
}
