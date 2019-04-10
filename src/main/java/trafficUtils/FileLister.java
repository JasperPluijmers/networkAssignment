package trafficUtils;

import java.io.File;
import java.util.Arrays;

public class FileLister {

    public static String files(String path) {
        File file = new File(path);
        String files = Arrays.toString(filenamesFromPath(file.listFiles(File::isFile))).replace(" ","");
        return files.substring(1,files.length() - 1);
    }

    public static String folders(String path) {
        File file = new File(path);
        String folders = Arrays.toString(filenamesFromPath(file.listFiles(File::isDirectory))).replace(" ","");
        return folders.substring(1, folders.length() - 1);
    }

    public static String filesFormat(String path) {
        return path + ";" + folders(path) + ";" + files(path);
    }

    public static String[] filenamesFromPath(File[] files) {
        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fileNames[i] = files[i].getName();
        }

        return fileNames;
    }
}

