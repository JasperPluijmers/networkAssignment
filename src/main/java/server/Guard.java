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
    private static final String ROOT_DIRECTORY = "/home/jasper.pluijmers/nws";

    private Sender sender;
    private String name = "testServer";

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
        Packet packet = new Packet(receivedPacket);

        switch (packet.getOption()) {
            case Discover:
                handleDiscover(receivedPacket);
                break;
            case Setup:
                handleSetup(receivedPacket);
                break;
        }
        Logger.logPacket(packet);
    }

    private void handleSetup(DatagramPacket receivedPacket) {
        Logger.log("Found a setup packet from: " + receivedPacket.getSocketAddress());
        InetAddress address = receivedPacket.getAddress();
        int destinationPort = receivedPacket.getPort();
        ServerSession nextSession = new ServerSession(address, destinationPort, ROOT_DIRECTORY);
        nextSession.init();
    }

    private void handleDiscover(DatagramPacket receivedPacket) {
        DatagramPacket discoveredPacket = PacketCreator.discoveredPacket(receivedPacket.getAddress(), receivedPacket.getPort(), name);
        sender.send(discoveredPacket);
    }
}
