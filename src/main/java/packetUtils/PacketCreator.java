package packetUtils;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PacketCreator {
    private static final int DISCOVER_PORT = 3651;

    private static Packet createPacket(byte id, PacketOption option, int number, byte[] data) {
        Packet packet = new Packet();
        packet.setId(id);
        packet.setOption(option);
        packet.setNumber(number);
        packet.setData(data);
        return packet;
    }

    public static DatagramPacket discoverPacket() {
        InetAddress address;
        DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0);

        Packet packet = new Packet((byte) 0, PacketOption.Discover, 0, "Hello there".getBytes());
        try {
            address = InetAddress.getByName("255.255.255.255");
            datagramPacket = new DatagramPacket(packet.asByteArray(), 0, packet.asByteArray().length, address, DISCOVER_PORT );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        return datagramPacket;
    }

    public static DatagramPacket discoveredPacket(InetAddress address, int port) {
        Packet packet = new Packet((byte) 0, PacketOption.Discovered, 0, "General kenobi".getBytes());
        DatagramPacket datagramPacket = new DatagramPacket(packet.asByteArray(), packet.asByteArray().length, address, port);
        return datagramPacket;
    }

    public static DatagramPacket acceptPacket(InetAddress address, int port, byte id) {
        Packet packet = new Packet(id, PacketOption.Accept, 1, new byte[0]);
        DatagramPacket datagramPacket = new DatagramPacket(packet.asByteArray(), packet.asByteArray().length, address, port);
        return datagramPacket;
    }

    public static DatagramPacket setupPacket(InetAddress address, int port) {
        Packet packet = new Packet((byte) 0, PacketOption.Setup, 0, new byte[0]);
        DatagramPacket datagramPacket = new DatagramPacket(packet.asByteArray(), packet.asByteArray().length, address, port);
        return datagramPacket;
    }
}
