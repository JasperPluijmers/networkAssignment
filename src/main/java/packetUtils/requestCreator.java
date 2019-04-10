package packetUtils;

public class requestCreator {

    public static String filesRequest(String pathToFile) {
        return Request.files + " " + pathToFile;
    }

    public static String downloadRequest(String pathToFile) {
        return Request.down + " " + pathToFile;
    }

    public static String uploadRequest(String pathToFile) {
        return Request.up + " " + pathToFile;
    }
}
