package fileserver.tui.screens;

import fileserver.utils.trafficUtils.Remote;
import fileserver.tui.functionalInterfaces.DoubleStringMenuCommand;
import fileserver.tui.functionalInterfaces.stringMenuCommand;
import fileserver.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The Files screen gets shown after a server sends over available files and folders. It allows for
 * opening new folders, downloading files and uploading files to the current folder.
 */
public class FilesScreen extends Screen {
    public static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_REVERSE = "\u001b[7m";
    private final DoubleStringMenuCommand upload;

    private Map<Integer, String> foldersMap;
    private Map<Integer, String> filesMap;

    private stringMenuCommand download;
    private stringMenuCommand askfiles;

    private String currentPath;

    public FilesScreen(Remote remote, String files, stringMenuCommand download, stringMenuCommand askfiles, DoubleStringMenuCommand upload) {
        super("Connected to: " + remote.getName() + " on: " + remote.getAddress());
        this.filesMap = new HashMap<>();
        this.foldersMap = new HashMap<>();
        this.download = download;
        this.askfiles = askfiles;
        this.upload = upload;
        super.setContent(parseFiles(files));
    }

    private String parseFiles(String fileFormat) {
        String path = fileFormat.split(":")[0];
        currentPath = path;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Current directory: ").append(path).append("\n");

        int count = 1;

        if (!currentPath.equals("/")) {
            stringBuilder.append(ANSI_REVERSE);
            stringBuilder.append("0 ~ ..");
            stringBuilder.append(ANSI_RESET + "\n");
        }

        if (fileFormat.split(":").length > 1) {
            String folders = fileFormat.split(":")[1];
            for (String folder : folders.split("\\|")) {
                foldersMap.put(count, folder);
                stringBuilder.append(ANSI_REVERSE);
                stringBuilder.append(count).append(" ~ ").append(folder).append(ANSI_RESET).append("\n");
                count++;
            }
        }
        if (fileFormat.split(":").length > 2) {
            String files = fileFormat.split(":")[2];
            for (String file : files.split("\\|")) {
                filesMap.put(count, file);
                stringBuilder.append(count).append(" - ").append(file).append("\n");
                count++;
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void handleCommand(String command) {
        if (command.split(" ").length == 2 && command.split(" ")[0].equals("up")) {
            upload.menuFunction(command.split(" ")[1], currentPath + "/");
        }
        int commandNumber;
        try {
            commandNumber = Integer.parseInt(command);
        } catch (NumberFormatException e) {
            help();
            return;
        }

        if (!currentPath.equals("/") && commandNumber == 0) {
            String strippedSlash = currentPath.substring(0, currentPath.length() - 1);
            askfiles.menuFunction(strippedSlash.substring(0,strippedSlash.lastIndexOf("/") + 1));
        }
        if (foldersMap.containsKey(commandNumber)) {
            askfiles.menuFunction(currentPath + foldersMap.get(commandNumber) + "/");
        } else if (filesMap.containsKey(commandNumber)) {
            download.menuFunction(currentPath + filesMap.get(commandNumber));
        } else {
            help();
        }

    }

    private void help() {
        Logger.log("To download a file, give the number before it. \n" +
                "To open a " + ANSI_REVERSE + "folder" + ANSI_RESET + " give the number before it \n" +
                "To upload a file to the current directory, use: \n" +
                "up /path/to/file.txt");
    }
}
