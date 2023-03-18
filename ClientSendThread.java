import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientSendThread extends Thread {
	private Scanner sc = new Scanner(System.in);
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
                synchronized (this) {
                    String msg = sc.nextLine();
                    if (msg != null) {
                        try {
                            this.send(msg);
                            System.out.println("");
                            if (msg.equals("exit")) {
                                this.closed = true;
                                break;
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ClientSendThread.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Erreur: Default envoie");
                            break;
                        }
                    }
                }
            }
            if (this.closed) {
                exit();
            }
    }
}
