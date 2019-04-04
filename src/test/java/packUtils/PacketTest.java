package packUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import packetUtils.Packet;
import packetUtils.PacketOption;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class PacketTest {
    private static final byte PACKET_ID = 1;
    private static final int PACKET_NUMBER = 2;
    private static final PacketOption PACKET_OPTION = PacketOption.Setup;
    private static final byte[] PACKET_DATA = new byte[]{1,2,3,4,5};
    private static final String DESTINATION_ADDRESS = "localhost";
    private static final int DESTINATION_PORT = 1000;
    InetAddress destination;
    Packet newPacket;

    @Before
    public void setUp() {
        Packet packet = new Packet();
        packet.setId(PACKET_ID);
        packet.setOption(PACKET_OPTION);
        packet.setNumber(PACKET_NUMBER);
        packet.setData(PACKET_DATA);

        try {
            destination = InetAddress.getByName(DESTINATION_ADDRESS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        DatagramPacket udpPacket = packet.asDatagramPacket(destination, DESTINATION_PORT);
        newPacket = new Packet(udpPacket.getData());
    }

    @Test
    public void idTest() {
        Assert.assertEquals(newPacket.getId(), PACKET_ID);
    }

    @Test
    public void optionTest() {
        Assert.assertEquals(newPacket.getOption(), PACKET_OPTION);
    }

    @Test
    public void numberTest() {
        Assert.assertEquals(newPacket.getNumber(), PACKET_NUMBER);
    }

    @Test
    public void dataTest() {
        Assert.assertArrayEquals(newPacket.getData(), PACKET_DATA);
    }



}
