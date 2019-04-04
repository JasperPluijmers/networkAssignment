package packetUtils;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Packet {
    private byte id;
    private byte[] number;
    private PacketOption option;
    private byte[] data;

    public Packet() {

    }

    public Packet(byte[] packetData) {
        this.id = packetData[0];
        this.option = PacketOption.fromValue(packetData[1]);
        this.number = Arrays.copyOfRange(packetData,2,6);
        this.data = Arrays.copyOfRange(packetData,6,packetData.length);
    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return toInt(this.number);
    }

    public PacketOption getOption() {
        return option;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public void setId(byte id) {
        this.id = id;
    }

    public void setNumber(int number) {
        this.number = toBytes(number);
    }

    public void setOption(PacketOption option) {
        this.option = option;
    }

    public byte[] asByteArray() {
        byte[] byteRep = new byte[6 + data.length];
        byteRep[0] = this.id;
        byteRep[1] = this.option.getValue();
        System.arraycopy(this.number, 0, byteRep, 2, this.number.length);

        System.arraycopy(this.data,0,byteRep,6,this.data.length);
        return byteRep;
    }

    public DatagramPacket asDatagramPacket(InetAddress destination, int port) {
        byte[] byteRep = asByteArray();
        return new DatagramPacket(byteRep,byteRep.length, destination, port);
    }
    private byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);

        return result;
    }

    private static int toInt(byte[] bytes) {
        int ret = 0;
        for (int i=0; i<4 && i<bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }


}