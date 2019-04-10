package trafficUtils;

import packetUtils.Packet;
import packetUtils.PacketCreator;
import server.ServerSession;
import utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashSet;
import java.util.Set;

public class SendHandler {

    private static final int MAXIMUM_WINDOW_SIZE = 10;
    private static final int DATA_SIZE = 1000;
    private static final int RESEND_TIME = 100;
    private byte id;
    private ServerSession serverSession;
    private Set<Packet> sentPackets;
    private boolean active;
    private boolean isdone;
    private FileInputStream fileInputStream;
    private int number;

    public SendHandler(byte id, ServerSession serverSession) {
        this.id = id;
        this.serverSession = serverSession;
        this.active = false;
        number = 1;
        isdone = false;
    }

    public void init(File file) throws FileNotFoundException {
        Logger.log("starting download for: " + file);
        active = true;
        fileInputStream = new FileInputStream(file);
        sentPackets = new HashSet<>();
        serverSession.sender.send(PacketCreator.beginOfFile(file.getName(),file.length(), id , serverSession.getDestinationAddress(), serverSession.getDestinationPort()));
        number = 2;
        try {
            fillPackets();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillPackets() throws IOException {
        if (sentPackets.size() < MAXIMUM_WINDOW_SIZE) {
            if (fileInputStream.available() > 0) {
                byte[] data = new byte[DATA_SIZE];
                fileInputStream.read(data);
                number++;
                DatagramPacket datagramPacket = PacketCreator.dataPacket(number, id, data, serverSession.getDestinationAddress(), serverSession.getDestinationPort());
                sentPackets.add(new Packet(datagramPacket));
                resend(datagramPacket);
                fillPackets();
            } else if (!isdone){
                DatagramPacket datagramPacket = PacketCreator.eofPacket(number, id, serverSession.getDestinationAddress(), serverSession.getDestinationPort());
                serverSession.sender.send(datagramPacket);
                sentPackets.add(new Packet(datagramPacket));
                isdone = true;
            }
        }
        Logger.log("Sent Packet after filling:" + sentPackets);
    }

    public boolean isActive() {
        return this.active;
    }

    public void acknowledge(Packet packet) throws IOException {
        int acknowledged = packet.getNumber();
        sentPackets.removeIf(sentPacket -> sentPacket.getNumber() == acknowledged);
        Logger.log("SentPackets after ack: " + sentPackets);
        if (!(fileInputStream.available() == 0 &&  sentPackets.size() == 0)) {
            try {
                fillPackets();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void resend(DatagramPacket datagramPacket) {
        serverSession.sender.send(datagramPacket);

    }
}
