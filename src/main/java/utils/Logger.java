package utils;

import java.sql.Timestamp;

public class Logger {
    public static void log(String message) {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " - " +  message);
    }
}
