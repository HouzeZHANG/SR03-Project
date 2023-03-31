package Server;

import EnumLib.Ack;
import EnumLib.BasicMsg;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;


public class SocketThread extends Thread {
	// create mapping between socket and client name O(1)
	private final Hashtable<SocketThread, String> socketThreadToID;
	// used to check if the client name is already taken O(1)
	private final HashSet<String> nameSet;
	private final Socket clientSocket;
	private String clientName;
	private final InputStream inputStream;
	private final OutputStream outputStream;

	public SocketThread(Socket clientSocket,
						Hashtable<SocketThread, String> socketThreadToID,
						HashSet<String> nameSet) throws IOException {
		this.clientSocket = clientSocket;
		this.socketThreadToID = socketThreadToID;
		this.clientName = "";
		this.inputStream = clientSocket.getInputStream();
		this.outputStream = clientSocket.getOutputStream();
		this.nameSet = nameSet;
	}

	public OutputStream getOutputStream() {
		return this.outputStream;
	}

	// send message to the client, used by the server, by socketThread
	 public void send(SocketThread destination, String str) throws IOException {
		synchronized (destination.outputStream) {
			destination.outputStream.write(str.getBytes());
			destination.outputStream.flush();
		}
	}

	/**
	 * before assigne the usernam, exit directly without remove the name from the nameSet
	 * after assigne the username, exit will remove the name from the nameSet
	 */
	public void exit() {
		try {
			// mise à jour de la liste des clients
			socketThreadToID.remove(this);

			if (Objects.equals(this.clientName, "")){
				this.clientName = "Anonyme user";
			}
			else{
				// mise à jour de la liste des noms
				this.nameSet.remove(this.clientName);
			}

			System.out.println("[Serveur] " + this.clientName +
					" se déconnecte, "+ socketThreadToID.size() +
					" clients en ligne.");

			for (SocketThread clientSMR : socketThreadToID.keySet()) {
				// broadcast to all clients that a client has left
				if (clientSMR != this) {
					try {
						this.send(clientSMR, this.clientName.trim() + " a quitté la conversation");
					} catch (IOException ex) {
						Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}

			// Fermer la connexion
			this.inputStream.close();
			this.outputStream.close();
			this.clientSocket.close();
		} catch (IOException ex) {
			Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
		} 
	}

	private void broadcast(String msg, String clientName) throws IOException, ClassNotFoundException {
		synchronized (this) {
			for (SocketThread clientSMR : socketThreadToID.keySet()) {
				if (!clientSMR.clientName.equals(this.clientName)) {
					String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					this.send(clientSMR, "[" + currentTime + "]" + "[" + clientName.trim() + "] " + msg);
				}
			}
			System.out.println("[Serveur] " + this.clientName.trim() + " a envoyé un message");
		}
	}

	private void unicast(String msg, String clientName) throws IOException, ClassNotFoundException {
		String dest = msg.substring(msg.indexOf("@")+1, msg.indexOf(" "));
		String msg_dest = msg.substring(msg.indexOf(" ")+1);
		synchronized (this) {
			boolean ok = false;
			for (SocketThread clientSMR : socketThreadToID.keySet()) {
				System.out.println("de"+clientSMR.clientName+"bug");
				if (clientSMR.clientName.trim().equals(dest)) {
					String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					this.send(clientSMR, "[" + currentTime + "]" +
							"[" + clientName.trim() +
							"(msg privé)] " + msg_dest);
					System.out.println("[Serveur] " + this.clientName.trim() + " a envoyé un message privé à " + dest);
					ok = true;
				}
			}
			if(!ok){
				this.send(this, "[Serveur] Utilisateur " + dest + " n'existe pas");
			}
		}
	}

	private boolean pseduoValide(String pseudo) { return pseudo.indexOf('@') == -1 && pseudo.indexOf('!') == -1; }

	synchronized private String readMessage() throws IOException {
		byte[] b = new byte[200];
		int len = this.inputStream.read(b);
		return new String(b, 0, len);
	}

	private void setUserName() throws IOException, InterruptedException {
		while(true){
			this.send(this, "[Serveur] Entrez votre pseudo :");
			System.out.println("[Serveur] Waiting for client to enter a username");

			while(this.inputStream.available() <= 0){
				Thread.sleep(100);
			}
			// Lire le pseudo
			this.clientName = this.readMessage().trim();
			System.out.println("[Serveur] Client name is " + this.clientName);

			// Vérifier si le client veut quitter
			if (Objects.equals(this.clientName, BasicMsg.EXIT.toString())) {
				this.exit();
				return;
			}

			// Vérifier si le pseudo est valide
			if (pseduoValide(this.clientName)) {
				// O(1) pour vérifier si le pseudo est déjà pris
				if (!this.nameSet.contains(this.clientName)) {
					// O(1) pour ajouter le pseudo à la hashmap
					this.socketThreadToID.put(this, this.clientName);
					// O(1) pour ajouter le pseudo à la liste des pseudos
					this.nameSet.add(this.clientName);
					return;
				}
				else {
					this.send(this, "[Serveur] Votre pseudo a déja été utilisé. Veuillez réessayer : ");
				}
			}
			else {
				this.send(this, "[Serveur] Le format de pseudo n'est pas valide. Veuillez réessayer : ");
			}
		}
	}

	@Override
	public void run() {
		try {
			// Assurer qu'il n'y a qu'un seul thread qui utilise cet objet
			setUserName();

			// Afficher sur le serveur
			System.out.println("[Serveur] Pseudo de nouveau client : " + this.clientName.trim());

			// Afficher sur le client
			this.send(this, "[Serveur] Vous(pseudo: " + this.clientName.trim()
					+ ") avez rejoint la conversation.\n" +
					"[Serveur] Tapez " + BasicMsg.EXIT + " pour se déconnecter.\n");
			this.send(this, "-----------------------------------------------------");

			// Annoncer aux autres clients
			for (SocketThread clientSMR : socketThreadToID.keySet()) {
				if (clientSMR != this) {
					this.send(clientSMR, "[Serveur] " + this.clientName.trim() + " a rejoint la conversation.");
				}
			}

			// Commencer la conversation
			while (true) {
				String msg = this.readMessage();
				if (msg.startsWith(String.valueOf(BasicMsg.EXIT))) {
					// Confirme la terminaison de la session en envoyant un ack
					this.send(this, Ack.CLIENT_EXIT.toString());
					exit();
				} else if (msg.startsWith("@")){
					unicast(msg, this.clientName);
				} else {
					broadcast(msg, this.clientName);
				}
			}
		} catch (IOException e) {
			if (!this.clientSocket.isClosed()) { exit(); }
			System.out.println("[Serveur] La session de " + this.clientName.trim() + " a terminé.");
		} catch (ClassNotFoundException e) {
			Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
