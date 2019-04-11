package trafficUtils;

import packetUtils.Packet;
import utils.CheckSum;
import utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class ReceiveHandler {

    private final String directory;
    private final String fileName;
    private Map<Integer, Packet> packetQueue;
    private int lastWritten;
    private Path writingPath;
    private boolean active;
    private long receivedCheckSum;
    private int lastPackage;
    private boolean eofReached;

    public ReceiveHandler(String directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
        active = false;
        eofReached = false;
    }

    public void init() {
        File file = new File(directory);
        if (file.exists() && file.isDirectory()) {
            writingPath = Paths.get(new File(directory + fileName).getAbsolutePath());
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

    private void checkQueue() {/*
        Logger.log("packetQueue: " + packetQueue + " ");
        Logger.log("nextPacket: " + (lastWritten + 1));*/
        if (packetQueue.containsKey(lastWritten + 1)) {
            try {
                Files.write(writingPath,packetQueue.get(lastWritten + 1).getData(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastWritten++;
            packetQueue.remove(lastWritten);
            checkQueue();
        }
        if (eofReached && lastWritten == lastPackage) {
            long calculatedChecksum = 0;
            try {
                calculatedChecksum = CheckSum.getCheckSum(writingPath.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (receivedCheckSum == calculatedChecksum) {
                Logger.log("File: " + fileName + " received correctly!");
            } else {
                Logger.log("Checksum incorrect, received: " + receivedCheckSum + ". Calculated: " + calculatedChecksum);
            }
        }
    }


    public boolean isActive() {
        return active;
    }

    public void endOfFile(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(packet.getData());
        buffer.flip();
        lastPackage = packet.getNumber() - 1;
        receivedCheckSum = buffer.getLong();
        eofReached = true;
        checkQueue();
    }
}
