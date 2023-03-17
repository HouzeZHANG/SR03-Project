import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
			System.out.println(this.clientName + " se déconnecte.");
			clients.remove(this);
			synchronized (this) {
				if (!clients.isEmpty()) {
					for (ServeurMessageReceptor clientSMR : clients.keySet()) {
						if (clientSMR != null && clientSMR != this && clientSMR.clientName != null) {
							try {
								this.send(clientSMR,
										new String("*** " + this.clientName.trim() + " a quitté la conversation ***"));
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
					this.send(clientSMR, new String(clientName + " a dit : " + msg));
				}
			}
			System.out.println("Broadcast message a été envoyé par " + this.clientName.trim());
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
				this.send(this, new String("Entrer votre pseudonyme :"));
				InputStream in = this.clientSocket.getInputStream();
				boolean isClientNameInitialized = false;
				while (!isClientNameInitialized) {
					if (in.available() > 0) {
						byte b[] = new byte[200];
						inputStream.read(b);
						clientName= new String(b);
						if ((clientName.indexOf('@') == -1) || (clientName.indexOf('!') == -1)
								|| this.clients.containsValue(clientName)) {

							if (this.clients.containsValue(clientName)) {
								this.send(this,
										new String("Votre pseudo a été utilisé. Nouveau pseudonyme : "));
								continue;
							} else {
								this.clients.put(this, clientName);
								this.clientName = clientName;
								isClientNameInitialized = true;
							}
						} else {
							this.outputStream.write(("Le pseudo ne devrait pas contenir '@' ou '!'.").getBytes());
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
					this.clientName.trim() + " a rejoint la conversation. Tapez 'exit' pour se déconnecter \n"));
			this.send(this,
					new String("------------------------------------------------------------------------------"));
			// Annoncer aux autres clients
			synchronized (this) {
				for (ServeurMessageReceptor clientSMR : clients.keySet()) {
					if (clientSMR != null && clientSMR != this) {
						this.send(clientSMR, new String(this.clientName.trim() + " a rejoint la conversation"));
					}
				}
			}
			// Commencer la conversation
			while (true) {
				byte b[] = new byte[200];
				inputStream.read(b);
				String msg = new String(b);
					if (msg.startsWith("exit")) {
						this.closed = true;
						break;
					} else {
						broadcast(msg, this.clientName);
					}
			}
			// Terminer la session
			if (this.closed) {
				this.send(this, new String("Vous avez quitté la conversation"));
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
