package fileserver.trafficUtils;

import java.io.File;
import java.util.StringJoiner;

/**
 * The FileLister puts the folders and file in a certain path in the correct format:
 * /current/path/|folders,seperated,by,comma|files,seperated,by,comma
 */
public class FileLister {

    public static String filesFormat(String path, String visiblePath) {
        File file = new File(path);
        return visiblePath + ":" + fileFormatFromPath(file.listFiles(File::isDirectory)) + ":" + fileFormatFromPath(file.listFiles(File::isFile));
    }

    private static String fileFormatFromPath(File[] files) {
        StringJoiner stringJoiner = new StringJoiner("|");
        for (int i = 0; i < files.length; i++) {
            stringJoiner.add(files[i].getName());
        }
        return stringJoiner.toString();
    }
}

