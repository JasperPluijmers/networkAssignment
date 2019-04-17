package fileserver.utils.trafficUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * A Sender can send datagrampackets over a datagramsocket.
 */
public class Sender {
    public DatagramSocket datagramSocket;

    public Sender(DatagramSocket sendingSocket) {
        this.datagramSocket = sendingSocket;
    }

    public void send(DatagramPacket datagramPacket) {
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
