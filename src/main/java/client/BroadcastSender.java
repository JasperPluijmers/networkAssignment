package client;

import trafficUtils.Sender;

import java.net.SocketException;

public class BroadcastSender extends Sender {

    public BroadcastSender(int port) {
        super(port);
        try {
            super.datagramSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
