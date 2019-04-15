package trafficUtils;

import utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Sender {
    public DatagramSocket datagramSocket;

    public Sender(DatagramSocket sendingSocket) {
        this.datagramSocket = sendingSocket;
    }

    public void send(DatagramPacket datagramPacket) {
        Logger.log("sending message");
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
