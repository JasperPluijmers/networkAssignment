package fileserver.trafficUtils;

import fileserver.packetUtils.Packet;
import fileserver.packetUtils.PacketCreator;
import fileserver.tui.Tui;
import fileserver.tui.screens.UploadScreen;
import fileserver.utils.CheckSum;
import fileserver.utils.Logger;
import fileserver.utils.Constants;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The SendHandler handles sending a file in udp packets. The SendHandler keeps track of a HashSet of a maximum of
 * 10 packets. It tries to fill them and submits the task of resending them to a ScheduledExecutorService. After sending
 * all data it sends an end of file packet with a checksum. It can be started with or without a fileserver.tui depending on a
 * fileserver.server or fileserver.client implementation.
 */
public class SendHandler {

    private static final int MAXIMUM_WINDOW_SIZE = 10;
    private static final int RESEND_TIME = 10;
    private final Sender sender;
    private final int destinationPort;
    private final InetAddress destinationAddress;
    private UploadScreen screen;
    private Tui tui;
    private boolean hasTui;

    private ScheduledExecutorService resendManager = Executors.newSingleThreadScheduledExecutor();
    private byte id;
    private Set<Packet> sentPackets;
    private boolean active;
    private boolean isDone;
    private BufferedInputStream bufferedInputStream;
    private AtomicInteger number;
    private Map<Integer, ScheduledFuture> retransmitSchedules;
    private File readFile;
    private float fileSize;
    private float dataSent;
    private long startingTime;

    public SendHandler(byte id, InetAddress destinationAddress, int destinationPort, Sender sender) {
        this.id = id;
        this.active = false;
        this.sender = sender;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.number = new AtomicInteger(1);
        this.isDone = false;
        this.retransmitSchedules = new HashMap<>();
        this.sentPackets = new HashSet<>();
        this.hasTui = false;
    }

    public SendHandler(byte id, InetAddress destinationAddress, int destinationPort, Sender sender, Tui tui, UploadScreen screen) {
        this(id, destinationAddress, destinationPort, sender);
        this.hasTui = true;
        this.tui = tui;
        this.screen = screen;
    }

    public void init(File file, String destination) throws FileNotFoundException {
        this.readFile = file;
        Logger.log("starting transfer for: " + file);
        active = true;
        FileInputStream fileInputStream = new FileInputStream(file);
        bufferedInputStream = new BufferedInputStream(fileInputStream);
        sender.send(PacketCreator.bofPacket(destination + file.getName(), file.length(), id, destinationAddress, destinationPort));
        number.set(2);

        this.fileSize = file.length();
        this.dataSent = 0;
        this.startingTime = System.currentTimeMillis();

        if (hasTui) {
            tui.update(screen);
        }
    }

    public void init(File file) throws FileNotFoundException {
        init(file, "");
    }

    public void fillPackets() throws IOException {
        if (sentPackets.size() < MAXIMUM_WINDOW_SIZE) {
            if ( !isDone && bufferedInputStream.available() > 0) {
                byte[] data = new byte[Constants.MAXIMUM_DATA_SIZE];
                int bytesRead = bufferedInputStream.read(data);
                DatagramPacket datagramPacket;
                if (bytesRead < Constants.MAXIMUM_DATA_SIZE) {
                    datagramPacket = PacketCreator.dataPacket(number.incrementAndGet(), id, Arrays.copyOfRange(data, 0, bytesRead), destinationAddress, destinationPort);
                } else {
                    datagramPacket = PacketCreator.dataPacket(number.incrementAndGet(), id, data, destinationAddress, destinationPort);
                }
                dataSent += bytesRead;
                sentPackets.add(new Packet(datagramPacket));
                send(datagramPacket);
                if (number.get() % 1000 == 0) {
                    if (hasTui) {
                        tui.update(screen.update(number.get(), 100 * dataSent / fileSize));
                    }
                }
                fillPackets();
            } else if (!isDone) {
                DatagramPacket datagramPacket = PacketCreator.eofPacket(number.incrementAndGet(), id, destinationAddress, destinationPort, CheckSum.getCheckSum(readFile.getAbsolutePath()));
                send(datagramPacket);
                sentPackets.add(new Packet(datagramPacket));
                isDone = true;
                bufferedInputStream.close();
            }
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void acknowledge(Packet packet) {
        int acknowledged = packet.getNumber();
        sentPackets.removeIf(sentPacket -> sentPacket.getNumber() == acknowledged);
        if (retransmitSchedules.containsKey(acknowledged)) {
            retransmitSchedules.get(acknowledged).cancel(true);
        }
        try {
            fillPackets();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isDone && sentPackets.isEmpty()) {
            float lapsedTime = ((float) System.currentTimeMillis() - startingTime) / 1000;
            if (hasTui) {
                tui.update(screen.last(lapsedTime, (long) fileSize));
            }
        }
    }

    private void send(DatagramPacket datagramPacket) {
        sender.send(datagramPacket);
        ScheduledFuture<?> scheduledEvent = resendManager.schedule(() -> this.send(datagramPacket), RESEND_TIME, TimeUnit.MILLISECONDS);
        retransmitSchedules.put(new Packet(datagramPacket).getNumber(), scheduledEvent);
    }

    public void close() {
        active = false;
    }

    public void pause() {
        this.active = false;
    }

    public void unPause() {
        this.active = true;
    }
}
