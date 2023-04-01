package Client;

import EnumLib.BasicMsg;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HeartBeatSenderThread extends Thread{
    private int heartBeatInterval;
    private boolean isRunning;
    private final OutputStream outputStream;

    public HeartBeatSenderThread(int heartBeatInterval, Socket clientSocket) throws IOException {
        this.heartBeatInterval = heartBeatInterval;
        this.isRunning = true;
        this.outputStream = clientSocket.getOutputStream();
    }

    synchronized public void send(String str) throws IOException {
        this.outputStream.write(str.getBytes());
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
                this.send(BasicMsg.HEART_BEAT.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
