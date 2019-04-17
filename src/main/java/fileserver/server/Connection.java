package fileserver.server;

import fileserver.packetUtils.Packet;
import fileserver.packetUtils.PacketCreator;
import fileserver.packetUtils.PacketOption;
import fileserver.packetUtils.Request;
import fileserver.trafficUtils.*;
import fileserver.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

public class Connection extends Listener {

    protected Sender sender;
    protected byte id;

    private Remote connectedRemote;
    private ReceiveHandler receiveHandler;
    private final String directory;
    private SendHandler sendHandler;
    private long lastMessage;

    public Connection(InetAddress address, int destinationPort, String directory) {
        super();
        this.sender = new Sender(super.getSocket());
        connectedRemote = new Remote("fileserver/client", address, destinationPort);
        this.id =(byte) new Random().nextInt();
        this.directory = directory;
        Thread listening = new Thread(this);
        listening.start();
        sender.send(PacketCreator.acceptPacket(connectedRemote.getAddress(), connectedRemote.getPort(), id));
    }

    @Override
    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        updateLastMessage();
        if (packet.getOption() != PacketOption.Data) {
            Logger.logPacket(packet);

        }
        switch (packet.getOption()) {
            case Request:
                handleRequest(datagramPacket);
                break;
            case Acknowledge:
                 if (sendHandler.isActive()) {
                         sendHandler.acknowledge(packet);
                 }
                 break;
            case BeginOfFile:
                handleBeginOfFile(datagramPacket);
                sendAcknowledge(packet.getNumber());
                break;
            case Data:
                if (receiveHandler.isActive()) {
                    receiveHandler.newPacket(packet);
                    sendAcknowledge(packet.getNumber());
                }
                break;
            case EndOfFile:
                if (receiveHandler.isActive()) {
                    receiveHandler.endOfFile(packet);
                    sendAcknowledge(packet.getNumber());
                }
                break;
        }
    }

    private void handleRequest(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);

        String request = new String(packet.getData());
        if (request.split("\\|").length == 2) {
            String path = request.split("\\|")[1];
            String command = request.split("\\|")[0];
            switch (Request.valueOf(command)) {
                case files:
                    handleFileRequest(path);
                    break;
                case down:
                    handleDownloadRequest(path);
                    break;
            }
        }
    }

    private void handleBeginOfFile(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        String path = new String(packet.getData()).split("\\|")[0];
        String size = new String(packet.getData()).split("\\|")[1];
        receiveHandler = new ReceiveHandler(directory, path, size);
        receiveHandler.init();
    }

    private void sendAcknowledge(int number) {
        sender.send(PacketCreator.ackPacket(connectedRemote.getAddress(), connectedRemote.getPort(),number, id));
    }

    private void handleDownloadRequest(String path) {
        sendHandler = new SendHandler(id, connectedRemote.getAddress(), connectedRemote.getPort(), sender);
        File file = new File(directory + path);
            try {
                sendHandler.init(file);
            } catch (FileNotFoundException e) {
                sendErrorMessage("File: " + directory + path + " does not exist.");
            }
    }

    public void close(String message) {
        sender.send(PacketCreator.closePacket(message, connectedRemote.getAddress(), connectedRemote.getPort(), id));
        if (receiveHandler != null && receiveHandler.isActive()) {
            receiveHandler.close();
        }
        if (sendHandler != null && sendHandler.isActive()) {
            sendHandler.close();
        }
        super.close();
    }

    private void sendErrorMessage(String message) {
        sender.send(PacketCreator.errorPacket(message, connectedRemote.getAddress(), connectedRemote.getPort(), id));
    }
    private void handleFileRequest(String path) {
        sender.send(PacketCreator.filesPacket(directory + path.substring(1), path, connectedRemote.getAddress(), connectedRemote.getPort(), id));
    }

    private void updateLastMessage() {
        lastMessage = System.currentTimeMillis();
    }

    public long getLastMessage() {
        return lastMessage;
    }
}
