package fileserver.utils.packetUtils;

import fileserver.utils.trafficUtils.FileLister;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Helper methods to construct the different kinds of packages
 */
public class PacketCreator {
    private static final int DISCOVER_PORT = 3651;

    public static DatagramPacket packetToUdp(Packet packet, int port, InetAddress address) {
        return new DatagramPacket(packet.asByteArray(), packet.asByteArray().length, address, port);
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

    public static DatagramPacket discoveredPacket(InetAddress address, int port, String name) {
        Packet packet = new Packet((byte) 0, PacketOption.Discovered, 0, name.getBytes());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket acceptPacket(InetAddress address, int port, byte id) {
        Packet packet = new Packet(id, PacketOption.Accept, 0, new byte[0]);
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket setupPacket(InetAddress address, int port) {
        Packet packet = new Packet((byte) 0, PacketOption.Setup, 0, new byte[0]);
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket ackPacket(InetAddress address, int port, int number, byte id) {
        Packet packet = new Packet(id, PacketOption.Acknowledge, number, new byte[0]);
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket requestPacket(String request, InetAddress address, int port, byte connectionId) {
        Packet packet = new Packet(connectionId, PacketOption.Request, (byte) 0, request.getBytes());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket filesPacket(String path, String visiblePath,  InetAddress address, int port, byte connectionId) {
        Packet packet = new Packet(connectionId, PacketOption.Files, (byte) 0, FileLister.filesFormat(path, visiblePath).getBytes());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket dataPacket(int number, byte id, byte[] data, InetAddress address, int port) {
        Packet packet = new Packet(id, PacketOption.Data, number, data);
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket bofPacket(String name, long length, byte id, InetAddress address, int port) {
        Packet packet = new Packet(id, PacketOption.BeginOfFile, (byte) 1, (name + "|" + length).getBytes());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket eofPacket(int number, byte id, InetAddress address, int port, long checksum) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(checksum);
        Packet packet = new Packet(id, PacketOption.EndOfFile, number, buffer.array());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket errorPacket(String message, InetAddress address, int port, byte id) {
        Packet packet = new Packet(id, PacketOption.Error, 0,  message.getBytes());
        return packetToUdp(packet, port, address);
    }

    public static DatagramPacket closePacket(String message, InetAddress address, int port, byte id) {
        Packet packet = new Packet(id, PacketOption.Close, 0, message.getBytes());
        return packetToUdp(packet, port, address);
    }
}
