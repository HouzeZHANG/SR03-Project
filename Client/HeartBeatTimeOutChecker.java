package Client;

import java.util.Date;

public class HeartBeatTimeOutChecker extends Thread{
    private final long heartBeatTimeOut;
    private final long checkInterval;
    private Date lastHeartBeat;
    private void exit() {
        System.exit(0);
    }

    @Override
    public String toString() {
        return "HeartBeatReceiverThread{" +
                "heartBeatInterval=" + heartBeatTimeOut +
                ", checkInterval=" + checkInterval +
                ", lastHeartBeat=" + lastHeartBeat +
                '}';
    }

    public HeartBeatTimeOutChecker(long heartBeatTimeOut, long checkInterval, Date lastHeartBeat) {
        this.heartBeatTimeOut = heartBeatTimeOut;
        this.checkInterval = checkInterval;
        this.lastHeartBeat = lastHeartBeat;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 比较上一个ACK的时间和当前时间，如果超过10秒，说明服务器挂了
            Date now = new Date();
            synchronized (this.lastHeartBeat) {
                if (now.getTime() - lastHeartBeat.getTime() > heartBeatTimeOut) {
                    System.out.println("Server is down");
                    exit();
                    break;
                }
            }
        }
    }
}
