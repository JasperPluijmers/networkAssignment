package trafficUtils;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import utils.CheckSum;
import utils.Logger;
import utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SendHandler {

    private static final int MAXIMUM_WINDOW_SIZE = 10;
    private static final int RESEND_TIME = 5;
    private final Sender sender;
    private final int destinationPort;
    private final InetAddress destinationAddress;

    private ScheduledExecutorService resendManager = Executors.newSingleThreadScheduledExecutor();
    private byte id;
    private Set<Packet> sentPackets;
    private boolean active;
    private boolean isDone;
    private FileInputStream fileInputStream;
    private AtomicInteger number;
    private Map<Integer, ScheduledFuture> retransmitSchedules;
    private File readFile;

    public SendHandler(byte id, InetAddress destinationAddress, int destinationPort, Sender sender) {
        this.id = id;
        this.active = false;
        this.sender = sender;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.number = new AtomicInteger(1);
        this.isDone = false;
        retransmitSchedules = new HashMap<>();
        sentPackets = new HashSet<>();
    }

    public void init(File file) throws FileNotFoundException {
        this.readFile=file;
        Logger.log("starting download for: " + file);
        active = true;
        fileInputStream = new FileInputStream(file);
        sender.send(PacketCreator.bofPacket(file.getName(),file.length(), id , destinationAddress, destinationPort));
        number.set(2);
    }

    public void fillPackets() throws IOException {
        if (sentPackets.size() < MAXIMUM_WINDOW_SIZE) {
            if (fileInputStream.available() > 0) {
                byte[] data = new byte[Constants.MAXIMUM_DATA_SIZE];
                int bytesRead = fileInputStream.read(data);
                DatagramPacket datagramPacket;
                if (bytesRead < Constants.MAXIMUM_DATA_SIZE) {
                    datagramPacket = PacketCreator.dataPacket(number.incrementAndGet(), id, Arrays.copyOfRange(data, 0, bytesRead), destinationAddress, destinationPort);
                } else {
                     datagramPacket = PacketCreator.dataPacket(number.incrementAndGet(), id, data, destinationAddress, destinationPort);

                }
                sentPackets.add(new Packet(datagramPacket));
                send(datagramPacket);
                fillPackets();
            } else if (!isDone){
                DatagramPacket datagramPacket = PacketCreator.eofPacket(number.incrementAndGet(), id, destinationAddress, destinationPort, CheckSum.getCheckSum(readFile.getAbsolutePath()));
                send(datagramPacket);
                sentPackets.add(new Packet(datagramPacket));
                isDone = true;
            }
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void acknowledge(Packet packet) throws IOException {
        int acknowledged = packet.getNumber();
        sentPackets.removeIf(sentPacket -> sentPacket.getNumber() == acknowledged);
        if (retransmitSchedules.containsKey(acknowledged)) {
            retransmitSchedules.get(acknowledged).cancel(true);
        }
        if (!(fileInputStream.available() == 0 &&  sentPackets.size() == 0)) {
            try {

                fillPackets();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void send(DatagramPacket datagramPacket) {
        sender.send(datagramPacket);
        ScheduledFuture<?> scheduledEvent = resendManager.schedule(() -> this.send(datagramPacket), RESEND_TIME, TimeUnit.MILLISECONDS);
        retransmitSchedules.put(new Packet(datagramPacket).getNumber(), scheduledEvent);
    }
}
