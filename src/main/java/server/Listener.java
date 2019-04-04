package server;

import java.net.DatagramSocket;
import java.net.SocketException;

public class Listener implements Runnable {
    private static final int LISTENING_PORT = 3651;

    @Override
    public void run() {
        while (true) {
            try {
                DatagramSocket listeningSocket = new DatagramSocket(3651);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

    }
}
