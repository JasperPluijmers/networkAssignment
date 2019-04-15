package trafficUtils;


import utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class Listener implements Runnable {
    private static final byte[] buffer = new byte[Constants.MAXIMUM_DATA_SIZE + Constants.HEADER_SIZE];

    private DatagramSocket listeningSocket;
    private boolean running;

    public Listener() {
        try {
            listeningSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
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
        running = true;
        while (running) {
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                listeningSocket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            handlePackage(receivedPacket);
        }
    }

    public abstract void handlePackage(DatagramPacket receivedPacket);

    public void close() {
        running = false;
    }

    public DatagramSocket getSocket() {
        return this.listeningSocket;
    }
}
