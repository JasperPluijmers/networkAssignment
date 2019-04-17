package trafficUtils;

import packetUtils.Packet;
import tui.Tui;
import tui.screens.DownloadScreen;
import utils.CheckSum;
import utils.Logger;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private BufferedOutputStream bufferedOutputStream;
    private long startTime;

    private DownloadScreen screen;
    private Tui tui;
    private float fileSize;
    private float written;
    private boolean hasTui;

    public ReceiveHandler(String directory, String fileName, String fileSize, Tui tui, DownloadScreen downloadScreen) {
        this.directory = directory;
        this.fileName = fileName;
        active = false;
        eofReached = false;
        this.fileSize = Float.valueOf(fileSize);

        hasTui = true;
        this.tui = tui;
        this.screen = downloadScreen;
    }

    public ReceiveHandler(String directory, String relativePath, String fileSize) {
        this.directory = directory + relativePath.substring(0, relativePath.lastIndexOf("/"));
        this.fileName = relativePath.substring(relativePath.lastIndexOf("/"));
        active = false;
        eofReached = false;
        this.fileSize = Float.valueOf(fileSize);
        hasTui = false;
    }

    public void init() {
        startTime = System.currentTimeMillis();
        File writingDirectory = new File(directory);
        if (writingDirectory.exists() && writingDirectory.isDirectory()) {
            writingPath = Paths.get(new File(directory + fileName).getAbsolutePath());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(writingPath.toString(), true);
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                active = true;
            } catch (IOException e) {
                Logger.log("Can't write to file" + writingPath);
            }
            packetQueue = new HashMap<>();
            lastWritten = 2;
        } else {
            Logger.log(directory + " is not a directory.");
        }
    }

    public void newPacket(Packet packet) {
        if (packet.getNumber() > lastWritten) {
            packetQueue.put(packet.getNumber(), packet);
        }
        checkQueue();
    }

    private void checkQueue() {
        if (packetQueue.containsKey(lastWritten + 1)) {
            try {
                byte[] data = packetQueue.get(lastWritten + 1).getData();
                bufferedOutputStream.write(data, 0, data.length);
                written += data.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (lastWritten % 10000 == 0) {
                if (hasTui) {
                    tui.update(screen.update(lastWritten, 100 * written / fileSize));
                } else {
                    Logger.log("Written package: " + lastWritten);
                }
            }
            lastWritten++;
            packetQueue.remove(lastWritten);
            checkQueue();
        }
        if (eofReached && lastWritten == lastPackage) {
            checkCheckSum();
        }
    }

    private void checkCheckSum() {
        try {
            bufferedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long calculatedChecksum = 0;
        try {
            calculatedChecksum = CheckSum.getCheckSum(writingPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (receivedCheckSum == calculatedChecksum) {
            float lapsedTime = ((float)(System.currentTimeMillis() - startTime)) / 1000 ;
            if (hasTui) {
                tui.update(screen.last(true, lapsedTime, new File(writingPath.toString()).length()));
            } else {
                Logger.log("File: " + fileName + " received correctly!");
                Logger.log("Transfer took: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
                Logger.log("Rate: " + new File(writingPath.toString()).length() / lapsedTime + " b/s");
            }
        } else {
            float lapsedTime = ((float)(System.currentTimeMillis() - startTime)) / 1000 ;
            if (hasTui) {
                tui.update(screen.last(false, lapsedTime, new File(writingPath.toString()).length()));
            } else {
            Logger.log("Checksum incorrect, received: " + receivedCheckSum + ". Calculated: " + calculatedChecksum);
            Logger.log("Transfer took: " + (System.currentTimeMillis() - startTime)/1000 + " seconds");
            }
        }
        active = false;
    }

    public void endOfFile(Packet packet) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(packet.getData());
        buffer.flip();
        lastPackage = packet.getNumber() - 1;
        receivedCheckSum = buffer.getLong();
        eofReached = true;
        if (lastWritten == lastPackage) {
            checkCheckSum();
        }
    }

    public void pause() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void unPause() {
        Logger.log("unpause!");
        active = true;
        checkQueue();
    }
}
