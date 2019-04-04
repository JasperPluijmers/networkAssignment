package server;

import utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Listener implements Runnable {
    private static final int LISTENING_PORT = 3651;
    private static final byte[] buffer = new byte[256];

    private DatagramSocket listeningSocket;

    public static void main(String[] args) {
        Listener listener = new Listener(LISTENING_PORT);
        Thread t = new Thread(listener);
        t.start();
    }

    public Listener(int port) {
        try {
            listeningSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                listeningSocket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handlePackage(receivedPacket);
        }
    }

    private void handlePackage(DatagramPacket receivedPacket) {
        Logger.log("Received a packet with payload: " + new String(receivedPacket.getData()));
        Logger.log("Address:" + receivedPacket.getAddress());
        Logger.log("" + receivedPacket.getSocketAddress());
    }
}
