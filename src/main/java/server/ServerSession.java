package server;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import packetUtils.Request;
import trafficUtils.Listener;
import trafficUtils.SendHandler;
import trafficUtils.Sender;
import utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class ServerSession extends Listener {

    protected Sender sender;
    protected InetAddress destinationAddress;
    protected int destinationPort;
    protected byte id;

    private final String directory;
    private SendHandler sendHandler;

    public ServerSession(InetAddress address, int destinationPort, String directory) {
        super();
        this.destinationAddress = address;
        this.destinationPort = destinationPort;
        this.sender = new Sender(super.getSocket());
        this.id =(byte) new Random().nextInt();
        this.directory = directory;
        sendHandler = new SendHandler(id, destinationAddress, destinationPort, sender);
    }

    public void init() {
        Thread listening = new Thread(this);
        listening.start();
        sender.send(PacketCreator.acceptPacket(destinationAddress, destinationPort, id));
    }

    @Override
    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Logger.logPacket(packet);

        switch (packet.getOption()) {
            case Request:
                handleRequest(datagramPacket);
                break;
            case Acknowledge:
                 if (sendHandler.isActive()) {
                     try {
                         sendHandler.acknowledge(packet);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
        }
    }

    private void handleRequest(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);

        String request = new String(packet.getData());
        String path = request.split(" ")[1];
        String command = request.split(" ")[0];
        switch (Request.valueOf(command)) {
            case files:
                handleFileRequest(path);
                break;
            case down:
                handleDownloadRequest(path);
                break;
            case up:
                handleUploadRequest(path);
                break;
        }
    }

    private void handleUploadRequest(String path) {

    }

    private void handleDownloadRequest(String path) {
        File file = new File(directory+path);
        if (file.exists() && file.isFile()) {
            try {
                sendHandler.init(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFileRequest(String path) {
        sender.send(PacketCreator.filesPacket(directory+path, destinationAddress, destinationPort, id));
    }

    public InetAddress getDestinationAddress() {
        return this.destinationAddress;
    }

    public int getDestinationPort() {
        return this.destinationPort;
    }
}
