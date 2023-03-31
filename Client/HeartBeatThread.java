package Client;

import EnumLib.Ack;

import java.io.IOException;

public class HeartBeatThread extends Thread{
    private int heartBeatInterval;
    private boolean isRunning;
    private final ClientSendThread clientSendThread;

    public HeartBeatThread(int heartBeatInterval, ClientSendThread clientSendThread) {
        this.heartBeatInterval = heartBeatInterval;
        this.isRunning = true;
        this.clientSendThread = clientSendThread;
    }

    public void setHeartBeatInterval(int heartBeatInterval) {
        this.heartBeatInterval = heartBeatInterval;
    }

    public void stopHeartBeat() {
        this.isRunning = false;
    }

    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(heartBeatInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                this.clientSendThread.send(Ack.HEART_BEAT.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
