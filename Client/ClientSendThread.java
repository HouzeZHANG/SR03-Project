package Client;

import java.io.*;
import java.util.*;
import java.util.logging.*;


public class ClientSendThread extends Thread {
	private final OutputStream outputStream;
	private Boolean closed = false;
	public ClientSendThread(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
    public void send(String str) throws IOException {
        this.outputStream.write(str.getBytes());
    }
	public void exit() {
		// Attendre que le serveur ferme la connexion
		try {
			Thread.sleep(20);
		} catch (InterruptedException ex) { 
            Logger.getLogger(ClientSendThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Ferme la connexion
		try {
			this.outputStream.close();
		} catch (IOException ex) { 
            Logger.getLogger(ClientSendThread.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

	public void run() {
        while (!this.closed) {
            // Lire le message de l'utilisateur
            Scanner sc = new Scanner(System.in);
            String msg = sc.nextLine();
//            sc.close();

            // Si le message est vide, continuer
            if (Objects.equals(msg, "")) {
                continue;
            }

            try {
                this.send(msg);
                System.out.println();
                if (msg.equals("exit")) {
                    this.closed = true;
                }
            } catch (IOException ex) {
                Logger.getLogger(ClientSendThread.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Erreur: Default envoie");
                break;
            }
        }
        exit();
    }
}
