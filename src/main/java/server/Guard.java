package server;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import trafficUtils.Listener;
import trafficUtils.Sender;
import utils.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Guard extends Listener {
    private static final int LISTENING_PORT = 3651;
    private static final int SENDING_PORT = 3652;
    private Sender sender;

    public Guard(int port) {
        super(port);
        sender = new Sender(SENDING_PORT);
    }

    public Guard(DatagramSocket datagramSocket) {
        super(datagramSocket);
        sender = new Sender(datagramSocket);
    }
    public static void main(String[] args) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(LISTENING_PORT);
            Listener listener = new Guard(datagramSocket);
            Thread thread = new Thread(listener);
            thread.start();
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handlePackage(DatagramPacket receivedPacket) {
        Packet packet = new Packet(receivedPacket.getData());

        switch (packet.getOption()) {
            case Discover:
                handleDiscover(receivedPacket);
                Logger.log("hoi");
                break;
            case Setup:
                handleSetup(receivedPacket);
                break;
        }

        Logger.log("packetType: " + packet.getOption());
        Logger.log("id: " + packet.getId());
        Logger.log("number: " + packet.getNumber());
        Logger.log("data:" + new String(packet.getData()));
    }

    private void handleSetup(DatagramPacket receivedPacket) {
        InetAddress address = receivedPacket.getAddress();
        int destinationPort = receivedPacket.getPort();
        Session nextSession = new Session(address, destinationPort);
        nextSession.init();
    }

    private void handleDiscover(DatagramPacket receivedPacket) {
        DatagramPacket discoveredPacket = PacketCreator.discoveredPacket(receivedPacket.getAddress(), receivedPacket.getPort());
        sender.send(discoveredPacket);
    }
}
