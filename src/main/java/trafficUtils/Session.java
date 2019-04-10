package trafficUtils;

import packetUtils.PacketCreator;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class Session extends Listener {

    protected Sender sender;
    protected InetAddress destinationAddress;
    protected int destinationPort;
    protected byte id;

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
    }

}
