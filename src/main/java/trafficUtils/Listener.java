package trafficUtils;

import packetUtils.Packet;
import utils.Constants;
import utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Listener implements Runnable {
    private static final byte[] buffer = new byte[Constants.MAXIMUM_DATA_SIZE + Constants.HEADER_SIZE];

    private DatagramSocket listeningSocket;

    public Listener() {
        try {
            listeningSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public Listener(DatagramSocket listeningSocket) {
        this.listeningSocket = listeningSocket;
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                listeningSocket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handlePackage(receivedPacket);
        }
    }

    public abstract void handlePackage(DatagramPacket receivedPacket);

    public DatagramSocket getSocket() {
        return this.listeningSocket;
    }
}
