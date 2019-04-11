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

public class SendHandler {

    private static final int MAXIMUM_WINDOW_SIZE = 10;
    private static final int DATA_SIZE = Constants.MAXIMUM_DATA_SIZE;
    private static final int RESEND_TIME = 5;
    private final Sender sender;
    private final int destinationPort;
    private final InetAddress destinationAddress;

    private ScheduledExecutorService resendManager = Executors.newSingleThreadScheduledExecutor();
    private byte id;
    private Set<Packet> sentPackets;
    private boolean active;
    private boolean isdone;
    private FileInputStream fileInputStream;
    private int number;
    private Map<Integer, ScheduledFuture> scheduledResends;
    private File readFile;

    public SendHandler(byte id, InetAddress destinationAddress, int destinationPort, Sender sender) {
        this.id = id;
        this.active = false;
        this.sender = sender;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.number = 1;
        this.isdone = false;
    }

    public void init(File file) throws FileNotFoundException {
        this.readFile=file;
        Logger.log("starting download for: " + file);
        active = true;
        fileInputStream = new FileInputStream(file);
        sentPackets = new HashSet<>();
        sender.send(PacketCreator.bofPacket(file.getName(),file.length(), id , destinationAddress, destinationPort));
        scheduledResends = new HashMap<>();
        number = 2;
    }

    public void fillPackets() throws IOException {
        if (sentPackets.size() < MAXIMUM_WINDOW_SIZE) {
            if (fileInputStream.available() > 0) {
                byte[] data = new byte[DATA_SIZE];
                int bytesRead = fileInputStream.read(data);
                number++;
                DatagramPacket datagramPacket;
                if (bytesRead < DATA_SIZE) {
                    datagramPacket = PacketCreator.dataPacket(number, id, Arrays.copyOfRange(data, 0, bytesRead), destinationAddress, destinationPort);
                } else {
                     datagramPacket = PacketCreator.dataPacket(number, id, data, destinationAddress, destinationPort);

                }
                sentPackets.add(new Packet(datagramPacket));
                resend(datagramPacket);
                fillPackets();
            } else if (!isdone){
                number++;
                DatagramPacket datagramPacket = PacketCreator.eofPacket(number, id, destinationAddress, destinationPort, CheckSum.getCheckSum(readFile.getAbsolutePath()));
                resend(datagramPacket);
                sentPackets.add(new Packet(datagramPacket));
                isdone = true;
            }
        }/*
        Logger.log("Sent Packet after filling:" + sentPackets);*/
    }

    public boolean isActive() {
        return this.active;
    }

    public void acknowledge(Packet packet) throws IOException {
        int acknowledged = packet.getNumber();
        sentPackets.removeIf(sentPacket -> sentPacket.getNumber() == acknowledged);
        if (scheduledResends.containsKey(acknowledged)) {
            scheduledResends.get(acknowledged).cancel(true);
        }
        if (!(fileInputStream.available() == 0 &&  sentPackets.size() == 0)) {
            try {

                fillPackets();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resend(DatagramPacket datagramPacket) {
        sender.send(datagramPacket);
        ScheduledFuture<?> scheduledEvent = resendManager.schedule(() -> this.resend(datagramPacket), RESEND_TIME, TimeUnit.MILLISECONDS);
        scheduledResends.put(new Packet(datagramPacket).getNumber(), scheduledEvent);
    }
}
