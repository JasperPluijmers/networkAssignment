package server;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import packetUtils.Request;
import trafficUtils.SendHandler;
import trafficUtils.Session;
import utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ServerSession extends Session {


    private final String directory;
    private SendHandler sendHandler;

    public ServerSession(InetAddress address, int destinationPort, String directory) {
        super(address, destinationPort);
        this.directory = directory;
        sendHandler = new SendHandler(id, this);
    }

    @Override
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
        Logger.log("hoi");
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
