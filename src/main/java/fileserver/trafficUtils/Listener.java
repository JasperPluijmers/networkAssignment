package fileserver.trafficUtils;


import fileserver.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Abstract class listener, which main purpose is listening on a udp port and putting those packets
 * into the handlePackage() method. Implementations should override the handlePackage() method to suit
 * their own needs.
 */
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
