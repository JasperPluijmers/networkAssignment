package fileserver.server;

import fileserver.utils.packetUtils.Packet;
import fileserver.utils.packetUtils.PacketCreator;
import fileserver.utils.trafficUtils.Listener;
import fileserver.utils.trafficUtils.Sender;
import fileserver.utils.Constants;
import fileserver.utils.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The guard is the main server class. It listens on a socket on port 3651 and will handle the discovery of the server
 * and setting up connections to the clients. It is also tasked with the timeout of connections.
 */
public class Guard extends Listener {
    private String ROOT_DIRECTORY;

    private static ScheduledExecutorService connectionChecker = Executors.newSingleThreadScheduledExecutor();
    private Sender sender;
    private String name = "Server";
    private List<Connection> connections;

    public Guard(int port, String directory) {
        super(port);
        sender = new Sender(super.getSocket());
        connections = new ArrayList<>();
        this.ROOT_DIRECTORY = directory;
        Thread thread = new Thread(this);
        thread.start();
        connectionChecker.scheduleAtFixedRate(this::checkConnections, Constants.CONNECTION_TIMEOUT_IN_MINUTES,
                Constants.CONNECTION_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
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
        connections.add(nextSession);
    }

    private void handleDiscover(DatagramPacket receivedPacket) {
        DatagramPacket discoveredPacket = PacketCreator.discoveredPacket(receivedPacket.getAddress(), receivedPacket.getPort(), name);
        sender.send(discoveredPacket);
    }

    private void checkConnections() {
        for (Connection connection: connections) {
            if (((float) System.currentTimeMillis() - (float) connection.getLastMessage())/60000 > Constants.CONNECTION_TIMEOUT_IN_MINUTES) {
                connection.close("Timeout");
            }
        }
        connections.removeIf(connection -> ((float) System.currentTimeMillis() - (float) connection.getLastMessage())/60000 > Constants.CONNECTION_TIMEOUT_IN_MINUTES);
    }
}
