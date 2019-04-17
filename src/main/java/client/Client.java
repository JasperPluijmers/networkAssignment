package client;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import packetUtils.requestCreator;
import trafficUtils.Listener;
import trafficUtils.ReceiveHandler;
import trafficUtils.Sender;
import trafficUtils.Remote;
import tui.screens.DownloadScreen;
import tui.screens.FilesScreen;
import tui.screens.MenuScreen;
import tui.Tui;
import tui.screens.ServerOverviewScreen;
import utils.Logger;

import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private Tui tui;

    public Client() {
        super();
        sender = new Sender(super.getSocket());
        discoveredRemotes = new HashSet<>();

        tui = new Tui(this);
        Thread tuiThread = new Thread(tui);
        tuiThread.start();

        Thread clientThread = new Thread(this);
        clientThread.start();
        tui.update(new MenuScreen(this::discover,this::setDirectory, directory));
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
    }

    public void handlePackage(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        switch (packet.getOption()) {
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
        }
    }

    private void filePacketHandler(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Logger.log(new String(packet.getData()));
        tui.update(new FilesScreen(connectedRemote, new String(packet.getData()), this::askDownload, this::askFiles));
    }

    private void errorHandler(DatagramPacket datagramPacket) {
        Logger.log("Error: " + new String(new Packet(datagramPacket).getData()));
    }

    private void handleBeginOfFile(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        String fileName = new String(packet.getData()).split("\\+")[0];
        String size = new String(packet.getData()).split("\\+")[1];
        receiveHandler = new ReceiveHandler(directory, fileName, size, tui, new DownloadScreen(fileName, this::askFiles, this::pauseDownload));
        receiveHandler.init();
    }

    private void sendAcknowledge(int number) {
        sender.send(PacketCreator.ackPacket(connectedRemote.getAddress(), connectedRemote.getPort(),number, connectionId));
    }

    private void handleAccept(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        connectedRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        connectionId = packet.getId();
        askFiles("/");
        Logger.log("Connection " + connectionId + " with: " + connectedRemote.getAddress() + ":" + connectedRemote.getPort());
    }

    private void handleDiscovered(DatagramPacket datagramPacket) {
        Packet packet = new Packet(datagramPacket);
        Remote newRemote = new Remote(new String(packet.getData()), datagramPacket.getAddress(), datagramPacket.getPort());
        if (!discoveredRemotes.contains(newRemote)) {
            discoveredRemotes.add(newRemote);
            tui.update(new ServerOverviewScreen(this::setup, discoveredRemotes));
        }

        Logger.log("discovered servers: " + discoveredRemotes);
    }

    private void setup(Remote remote) {
        sender.send(PacketCreator.setupPacket(remote.getAddress(), remote.getPort()));
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

    private void setDirectory(String directory) {
        if (Files.exists(Paths.get(directory))) {
            this.directory = directory;
            tui.update(new MenuScreen(this::discover,this::setDirectory, this.directory));
        } else {
            Logger.log("Directory does not exist");
            tui.update(new MenuScreen(this::discover,this::setDirectory, this.directory));
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
