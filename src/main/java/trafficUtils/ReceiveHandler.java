package trafficUtils;

import packetUtils.Packet;
import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class ReceiveHandler {

    private final String directory;
    private final String fileName;
    private File writingFile;
    private Map<Integer, Packet> packetQueue;
    private int lastWritten;
    private Path writingPath;
    private boolean active;

    public ReceiveHandler(String directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
        active = false;
    }

    public void init() {
        File file = new File(directory);
        if (file.exists() && file.isDirectory()) {
            writingFile = new File(directory+fileName);
            writingPath = Paths.get(writingFile.getAbsolutePath());
            try {
                Files.createFile(writingPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            packetQueue = new HashMap<>();
            lastWritten = 2;
            active = true;
        } else {
            Logger.log("What you tryin'?");
        }
    }

    public void newPacket(Packet packet) {
        if (packet.getNumber() > lastWritten) {
            packetQueue.put(packet.getNumber(), packet);
        }
        checkQueue();
    }

    private void checkQueue() {
        Logger.log("packetQueue: " + packetQueue + " ");
        Logger.log("nextPacket: " + (lastWritten + 1));
        if (packetQueue.containsKey(lastWritten + 1)) {
            Logger.log("hoi");
            try {
                Files.write(writingPath,packetQueue.get(lastWritten + 1).getData(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastWritten++;
            packetQueue.remove(lastWritten);
            checkQueue();
        }
    }


    public boolean isActive() {
        return active;
    }

    public void endOfFile(Packet packet) {

    }
}
