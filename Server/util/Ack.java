package Server.util;

public class Ack {
    private String ack;

    Ack(String ack) {
        this.ack = ack;
    }

    @Override
    public String toString() {
        return this.ack;
    }
}
