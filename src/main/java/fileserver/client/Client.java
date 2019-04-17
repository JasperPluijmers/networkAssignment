package fileserver.client;

import fileserver.utils.packetUtils.Packet;
import fileserver.utils.packetUtils.PacketCreator;
import fileserver.utils.packetUtils.RequestCreator;
import fileserver.utils.trafficUtils.*;
import fileserver.tui.screens.*;
import fileserver.tui.Tui;
import fileserver.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * The Client class is the main class of the  client. It controls the state of the client and couples the
 * different handlers. It is a listener, which is bound to a random available socket. The different methods
 * are used to communicate with the server. The user uses the tui to control these methods.
 */
public class Client extends Listener {

    private String directory = "";
    private Sender sender;
    private Set<Remote> discoveredRemotes;
    private Remote connectedRemote;
    private byte connectionId;
    private ReceiveHandler receiveHandler;
    private Tui tui;
    private SendHandler sendHandler;

    public Client(String directory) {
        super();
        sender = new Sender(super.getSocket());
        discoveredRemotes = new HashSet<>();
        this.directory = directory;
        tui = new Tui(this);
        Thread tuiThread = new Thread(tui);
        tuiThread.start();

        Thread clientThread = new Thread(this);
        clientThread.start();
        tui.update(new MenuScreen(this::discover, this::setDirectory, directory));
    }

    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        switch (packet.getOption()) {
            case Acknowledge:
                if (sendHandler.isActive()) {
                    sendHandler.acknowledge(packet);
                }
                break;
            case Discovered:
                handleDiscovered(datagramPacket);
                break;
            case Accept:
                handleAccept(datagramPacket);
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
            case Error:
                errorHandler(datagramPacket);
                break;
            case Files:
                filePacketHandler(datagramPacket);
                break;
            case Close:
                closeHandler();
                break;
            }
        }

    private void closeHandler() {
        if (receiveHandler != null && receiveHandler.isActive()) {
            receiveHandler.close();
        }
        if (sendHandler != null && sendHandler.isActive()) {
            sendHandler.close();
        }
        discoveredRemotes = new HashSet<>();
        tui.update(new MenuScreen(this::discover, this::setDirectory, directory));
        Logger.log("The fileserver.server closed the connection");
    }


    private void filePacketHandler(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Logger.log(new String(packet.getData()));
        tui.update(new FilesScreen(connectedRemote, new String(packet.getData()), this::askDownload, this::askFiles, this::startUpload));
    }

    private void errorHandler(DatagramPacket datagramPacket) {
        Logger.log("Error: " + new String(new Packet(datagramPacket).getData()));
    }

    private void handleBeginOfFile(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        String fileName = new String(packet.getData()).split("\\|")[0];
        String size = new String(packet.getData()).split("\\|")[1];
        receiveHandler = new ReceiveHandler(directory, fileName, size, tui, new DownloadScreen(fileName, this::askFiles, this::pauseDownload));
        receiveHandler.init();
    }

    private void sendAcknowledge(int number) {
        sender.send(PacketCreator.ackPacket(connectedRemote.getAddress(), connectedRemote.getPort(), number, connectionId));
    }

    private void handleAccept(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        connectedRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        connectionId = packet.getId();
        askFiles("/");
        /*Logger.log("Connection " + connectionId + " with: " + connectedRemote.getAddress() + ":" + connectedRemote.getPort());*/
    }

    private void handleDiscovered(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Remote newRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        if (!discoveredRemotes.contains(newRemote)) {
            discoveredRemotes.add(newRemote);
            tui.update(new ServerOverviewScreen(this::setup, discoveredRemotes));
        }
    }

    private void startUpload(String source, String destination) {
        sendHandler = new SendHandler(connectionId, connectedRemote.getAddress(), connectedRemote.getPort(), sender, tui, new UploadScreen(source, this::askFiles, this::pauseUpload));
        File file = new File(source);
        try {
            sendHandler.init(file, destination);
        } catch (FileNotFoundException e) {
            sendErrorMessage("File: " + source + " does not exist.");
        }
    }

    private void pauseUpload() {
        if (sendHandler.isActive()) {
            sendHandler.pause();
        } else {
            sendHandler.unPause();
        }

    }

    private void sendErrorMessage(String message) {
        sender.send(PacketCreator.errorPacket(message, connectedRemote.getAddress(), connectedRemote.getPort(), connectionId));
    }

    private void setup(Remote remote) {
        sender.send(PacketCreator.setupPacket(remote.getAddress(), remote.getPort()));
    }

    private void discover() {
        sender.send(PacketCreator.discoverPacket());
    }

    private void askFiles(String path) {
        sender.send(PacketCreator.requestPacket(RequestCreator.filesRequest(path), connectedRemote.getAddress(), connectedRemote.getPort(), connectionId));
    }

    private void askDownload(String path) {
        sender.send(PacketCreator.requestPacket(RequestCreator.downloadRequest(path), connectedRemote.getAddress(), connectedRemote.getPort(), connectionId));
    }

    private void setDirectory(String directory) {
        if (Files.exists(Paths.get(directory))) {
            this.directory = directory;
            tui.update(new MenuScreen(this::discover, this::setDirectory, this.directory));
        } else {
            Logger.log("Directory does not exist");
            tui.update(new MenuScreen(this::discover, this::setDirectory, this.directory));
        }
    }

    private void pauseDownload() {
        if (receiveHandler.isActive()) {
            receiveHandler.pause();
        } else {
            receiveHandler.unPause();
        }
    }

}
