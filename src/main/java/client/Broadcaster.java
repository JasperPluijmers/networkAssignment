package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Broadcaster {

    private static DatagramSocket socket;


    public static void main(String[] args) throws IOException {
        broadcast("Hello", InetAddress.getByName("255.255.255.255"));
    }

    public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);


        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, 3651);
        socket.send(packet);
        socket.close();
    }
}
