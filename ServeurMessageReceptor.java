import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServeurMessageReceptor extends Thread {
	private Hashtable<ServeurMessageReceptor, String> clients = new Hashtable<ServeurMessageReceptor, String>();
	private Socket clientSocket;
	private String clientName;
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private Boolean closed = false;

	public ServeurMessageReceptor(Socket clientSocket, Hashtable<ServeurMessageReceptor, String> clients) {
		this.clientSocket = clientSocket;
		this.clients = clients;
		this.clientName = "";
	}

	public void send(ServeurMessageReceptor destination, String str) throws IOException {
		destination.outputStream.write(str.getBytes());
	}

	public void exit() {
		try {
			clients.remove(this);
			System.out.println(this.clientName + " se déconnecte, "+ clients.size() + " clients en ligne.");
			synchronized (this) {
				if (!clients.isEmpty()) {
					for (ServeurMessageReceptor clientSMR : clients.keySet()) {
						if (clientSMR != null && clientSMR != this && clientSMR.clientName != null) {
							try {
								this.send(clientSMR,
										new String(this.clientName.trim() + " a quitté la conversation"));
							} catch (IOException ex) { 
								Logger.getLogger(ServeurMessageReceptor.class.getName()).log(Level.SEVERE, null, ex);
							} 
						}
					}
				}
			}
			// Fermer la connexion
			this.inputStream.close();
			this.outputStream.close();
			this.clientSocket.close();
		} catch (IOException ex) { 
			Logger.getLogger(ServeurMessageReceptor.class.getName()).log(Level.SEVERE, null, ex);
		} 
	}

	private void broadcast(String msg, String clientName) throws IOException, ClassNotFoundException {
		synchronized (this) {
			for (ServeurMessageReceptor clientSMR : clients.keySet()) {
				if (clientSMR != null && clientSMR.clientName != null && clientSMR.clientName != this.clientName) {
					String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
					this.send(clientSMR, new String("["+ currentTime +"]"+"[" + clientName.trim() + "] " + msg));
				}
			}
			System.out.println(this.clientName.trim() + " a envoyé un message");
		}
	}

	@Override
	public void run() {
		try {
			this.inputStream = clientSocket.getInputStream();
			this.outputStream = clientSocket.getOutputStream();
			String clientName;
			// Assurer qu'il n'y a qu'un seul thread qui utilise cet objet
			synchronized (this) {
				this.send(this, new String("Entrer votre pseudo :"));
				InputStream in = this.clientSocket.getInputStream();
				boolean isClientNameInitialized = false;
				while (!isClientNameInitialized) {
					if (in.available() > 0) {
						byte b[] = new byte[200];
						inputStream.read(b);
						clientName= new String(b);
						if (((clientName.indexOf('@') == -1) || (clientName.indexOf('!') == -1)
								|| this.clients.containsValue(clientName)) && !clientName.contains("exit")) {
							if (this.clients.containsValue(clientName)) {
								this.send(this,
										new String("Votre pseudo a déja été utilisé. Veuillez réessayer : "));
								continue;
							} else {
								this.clients.put(this, clientName);
								this.clientName = clientName;
								isClientNameInitialized = true;
							}
						} else {
							this.outputStream.write(("Le format de pseudo n'est pas valide. Veuillez réessayer : ").getBytes());
							this.outputStream.flush();
						}
					}
					else {
						Thread.sleep(10);
					}

				}
			}
			System.out.println("Pseudo de nouveau client : " + this.clientName.trim());
			this.send(this, new String(
					"Vous(pseudo: " + this.clientName.trim() + ") avez rejoint la conversation.\nTapez 'exit' pour se déconnecter.\n"));
			this.send(this,
					new String("-----------------------------------------------------"));

			// Annoncer aux autres clients
			synchronized (this) {
				for (ServeurMessageReceptor clientSMR : clients.keySet()) {
					if (clientSMR != null && clientSMR != this) {
						this.send(clientSMR, new String(this.clientName.trim() + " a rejoint la conversation."));
					}
				}
			}
			// Commencer la conversation
			while (true) {
				byte b[] = new byte[200];
				inputStream.read(b);
				String msg = new String(b);
					if (msg.startsWith("exit") || msg.startsWith("^C%")) {
						this.closed = true;
						break;
					} else {
						broadcast(msg, this.clientName);
					}
			}
			// Confirme la terminaison de la session en envoyant un ack
			if (this.closed) {
				this.send(this, new String("ACKUSEREXIT"));
				exit();
			}
		} catch (IOException e) {
			exit();
			System.out.println("La session de " + this.clientName.trim() + " a terminé.");

		} catch (ClassNotFoundException e) {
			Logger.getLogger(ServeurMessageReceptor.class.getName()).log(Level.SEVERE, null, e);
		} catch (InterruptedException e) {
			Logger.getLogger(ServeurMessageReceptor.class.getName()).log(Level.SEVERE, null, e);
		}
	}

}
