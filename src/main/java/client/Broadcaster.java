package client;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import packetUtils.PacketOption;
import trafficUtils.Listener;
import trafficUtils.Sender;
import utils.Logger;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static java.lang.Thread.sleep;

public class Broadcaster extends Listener {

    private Sender sender;

    public Broadcaster(DatagramSocket datagramSocket) {
        super(datagramSocket);
        sender = new Sender(datagramSocket);
    }

    public static void main(String[] args) throws Exception {

        DatagramSocket datagramSocket = new DatagramSocket(5409);
        Broadcaster broadcaster = new Broadcaster(datagramSocket);
        Thread thread = new Thread(broadcaster);
        thread.start();
        broadcaster.broadcast();
    }

    @Override
    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket.getData());

        Logger.log("packetType: " + packet.getOption());
        Logger.log("id: " + packet.getId());
        Logger.log("number: " + packet.getNumber());
        Logger.log("data:" + new String(packet.getData()));
        if (packet.getOption() == PacketOption.Discovered) {
            sender.send(PacketCreator.setupPacket(datagramPacket.getAddress(), datagramPacket.getPort()));
        }
    }

    private void broadcast() throws IOException {
        sender.send(PacketCreator.discoverPacket());
    }
}
