package packetUtils;

public class RequestCreator {

    public static String filesRequest(String pathToFile) {
        return Request.files + " " + pathToFile;
    }

    public static String downloadRequest(String pathToFile) {
        return Request.down + " " + pathToFile;
    }
}
