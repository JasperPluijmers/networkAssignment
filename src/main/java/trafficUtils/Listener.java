package trafficUtils;

import packetUtils.Packet;
import sun.awt.datatransfer.DataTransferer;
import utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Listener implements Runnable {
    private static final int LISTENING_PORT = 3651;
    private static final byte[] buffer = new byte[256];

    private DatagramSocket listeningSocket;

    public Listener(int port) {
        try {
            listeningSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

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
            Logger.log("received message");
            handlePackage(receivedPacket);
        }
    }

    public void handlePackage(DatagramPacket receivedPacket) {
        Packet packet = new Packet(receivedPacket.getData());
        Logger.log("packetType: " + packet.getOption());
        Logger.log("id: " + packet.getId());
        Logger.log("number: " + packet.getNumber());
        Logger.log("data:" + new String(packet.getData()));
    }

    public DatagramSocket getSocket() {
        return this.listeningSocket;
    }
}
