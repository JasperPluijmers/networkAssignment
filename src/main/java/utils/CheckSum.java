package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CheckSum {

    public static long getCheckSum(String path) throws IOException {
        FileInputStream file = new FileInputStream(path);
        CheckedInputStream check = new CheckedInputStream(file, new CRC32());
        BufferedInputStream in = new BufferedInputStream(check);
        byte[] buffer = new byte[1024];
        while (in.read(buffer) != -1) {
        }
        return check.getChecksum().getValue();
    }
}
