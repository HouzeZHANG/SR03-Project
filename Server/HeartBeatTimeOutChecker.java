package Server;

import java.util.Date;
import java.util.Hashtable;

public class HeartBeatTimeOutChecker extends Thread{
    private Hashtable<SocketThread, Date> socketToLastHeartBeat;
    private final long serverHeartBeatTimeOut;
    private int checkInterval;

    public HeartBeatTimeOutChecker(Hashtable<SocketThread, Date> socketToLastHeartBeat,
                                   int serverHeartBeatTimeOut, int checkInterval) {
        this.socketToLastHeartBeat = socketToLastHeartBeat;
        this.serverHeartBeatTimeOut = serverHeartBeatTimeOut;
        this.checkInterval = checkInterval;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Date now = new Date();
            for (SocketThread socketThread : this.socketToLastHeartBeat.keySet()) {
                // get lastHeartBeat
                Date lastHeartBeat = this.socketToLastHeartBeat.get(socketThread);
                synchronized (lastHeartBeat) {
                    System.out.println("HeartBeatTimeOutChecker: " + lastHeartBeat.getTime());
                    if (now.getTime() - lastHeartBeat.getTime() > this.serverHeartBeatTimeOut) {
                        System.out.println("Client " + socketThread + " is down");
                        socketThread.exit();
                        this.socketToLastHeartBeat.remove(socketThread);
                    }
                }
            }
        }
    }
}
