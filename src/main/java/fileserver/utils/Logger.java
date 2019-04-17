package fileserver.utils;

import fileserver.packetUtils.Packet;

import java.sql.Timestamp;

public class Logger {
    public static void log(String message) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " - " +  message);
    }

    public static void logPacket(Packet packet) {
        Logger.log("packetType: " + packet.getOption());
        Logger.log("id: " + packet.getId());
        Logger.log("number: " + packet.getNumber());
        Logger.log("data:" + new String(packet.getData()));
    }
}
