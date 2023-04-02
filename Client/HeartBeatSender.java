package Client;

import EnumLib.BasicMsg;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HeartBeatSender extends Thread{
    private int heartBeatInterval;
    private boolean isRunning;
    private final OutputStream outputStream;

    public HeartBeatSender(int heartBeatInterval, Socket clientSocket) throws IOException {
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

    public void run() {
        System.out.println("HeartBeatSenderThread: " + "Start");
        while (isRunning) {
            try {
                Thread.sleep(heartBeatInterval);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            try {
                this.send(BasicMsg.HEART_BEAT.toString());
//                System.out.println("HeartBeatSenderThread: " + BasicMsg.HEART_BEAT.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
