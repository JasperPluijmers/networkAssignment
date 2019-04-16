package server;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import trafficUtils.Listener;
import trafficUtils.Sender;
import utils.Constants;
import utils.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Guard extends Listener {
    private static final int LISTENING_PORT = 3651;
    private static final String ROOT_DIRECTORY = "/home/jasper.pluijmers/nws";

    private static ScheduledExecutorService connectionChecker = Executors.newSingleThreadScheduledExecutor();
    private Sender sender;
    private String name = "testServer";
    private List<Connection> connections;

    public Guard(int port) {
        super(port);
        sender = new Sender(super.getSocket());
        connections = new ArrayList<>();
        Thread thread = new Thread(this);
        thread.start();
        connectionChecker.scheduleAtFixedRate(this::checkConnections, Constants.CONNECTION_TIMEOUT_IN_MINUTES, Constants.CONNECTION_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }
    public static void main(String[] args) {
        Guard guard = new Guard(LISTENING_PORT);
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
    }

    private void handleSetup(DatagramPacket receivedPacket) {
        Logger.log("Found a setup packet from: " + receivedPacket.getSocketAddress());
        InetAddress address = receivedPacket.getAddress();
        int destinationPort = receivedPacket.getPort();
        Connection nextSession = new Connection(address, destinationPort, ROOT_DIRECTORY);
        nextSession.init();
        connections.add(nextSession);
    }

    private void handleDiscover(DatagramPacket receivedPacket) {
        DatagramPacket discoveredPacket = PacketCreator.discoveredPacket(receivedPacket.getAddress(), receivedPacket.getPort(), name);
        sender.send(discoveredPacket);
    }

    private void checkConnections() {
        for (Connection connection: connections) {
            if ((System.currentTimeMillis() - connection.getLastMessage())/60000 > Constants.CONNECTION_TIMEOUT_IN_MINUTES) {
                connection.close("Timeout");
            }
        }
        connections.removeIf(connection -> System.currentTimeMillis() - connection.getLastMessage() > Constants.CONNECTION_TIMEOUT_IN_MINUTES);
    }
}
