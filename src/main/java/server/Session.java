package server;

import packetUtils.PacketCreator;
import trafficUtils.Listener;
import trafficUtils.Sender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class Session extends Listener {

    private Sender sender;
    private InetAddress destinationAddress;
    private int destinationPort;
    private byte id;

    public Session(InetAddress address, int destinationPort) {
        super();
        this.destinationAddress = address;
        this.destinationPort = destinationPort;
        this.sender = new Sender(super.getSocket());
        this.id =(byte) new Random().nextInt();
    }

    public void init() {
        Thread listening = new Thread(this);
        listening.start();
        sender.send(PacketCreator.acceptPacket(destinationAddress, destinationPort, id));
    }

    @Override
    public void handlePackage(DatagramPacket receivedPacket) {

    }
}
