package client;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import packetUtils.requestCreator;
import trafficUtils.Listener;
import trafficUtils.ReceiveHandler;
import trafficUtils.Sender;
import trafficUtils.Remote;
import utils.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.sleep;

public class Client extends Listener {

    private String directory = "/home/jasper.pluijmers/downloadFolder/";
    private Sender sender;
    private Set<Remote> discoveredRemotes;
    private Remote connectedRemote;
    private byte connectionId;
    private ReceiveHandler receiveHandler;

    public Client(DatagramSocket datagramSocket) {
        super(datagramSocket);
        sender = new Sender(datagramSocket);
        discoveredRemotes = new HashSet<>();
    }

    public static void main(String[] args) throws Exception {
        DatagramSocket datagramSocket = new DatagramSocket();
        Client client = new Client(datagramSocket);
        Thread thread = new Thread(client);
        thread.start();
        client.discover();
    }

    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Logger.logPacket(packet);
        if (packet.getNumber() != 0) {
            sendAcknowledge(packet.getNumber());
        }
        switch (packet.getOption()) {
            case Discovered:
                handleDiscovered(datagramPacket);
                break;
            case Accept:
                handleAccept(datagramPacket);
                break;
            case BeginOfFile:
                handleBeginOfFile(datagramPacket);
                break;
            case Data:
                if (receiveHandler.isActive()) {
                    receiveHandler.newPacket(packet);
                }
                break;
            case EndOfFile:
                if (receiveHandler.isActive()) {
                    receiveHandler.endOfFile(packet);
                }
        }
    }

    private void handleBeginOfFile(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        String fileName = new String(packet.getData()).split("\\+")[0];
        receiveHandler = new ReceiveHandler(directory, fileName);
        receiveHandler.init();
    }

    private void sendAcknowledge(int number) {
        sender.send(PacketCreator.ackPacket(connectedRemote.getAddress(), connectedRemote.getPort(),number, connectionId));
    }

    private void handleAccept(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        connectedRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        connectionId = packet.getId();
        Logger.log("in sessie " + connectionId + " met: " + connectedRemote.getAddress() + ":" + connectedRemote.getPort());
        askDownload("/kud.mp4");
    }
    private void handleDiscovered(DatagramPacket datagramPacket) {
        sender.send(PacketCreator.setupPacket(datagramPacket.getAddress(), datagramPacket.getPort()));
        Packet packet = new Packet(datagramPacket);
        Remote newRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        discoveredRemotes.add(newRemote);
        Logger.log("discovered servers: " + discoveredRemotes);
    }

    private void discover() {
        sender.send(PacketCreator.discoverPacket());
    }

   private void askFiles(String path) {
        sender.send(PacketCreator.requestPacket(requestCreator.filesRequest(path), connectedRemote.getAddress(), connectedRemote.getPort(), connectionId));
    }

    private void askDownload(String path) {
        sender.send(PacketCreator.requestPacket(requestCreator.downloadRequest(path), connectedRemote.getAddress(), connectedRemote.getPort(), connectionId));
    }
}
