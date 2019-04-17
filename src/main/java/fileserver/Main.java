package fileserver;

import fileserver.client.Client;
import fileserver.server.Guard;
import fileserver.utils.Constants;
import fileserver.utils.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        if (args.length == 2 && Files.exists(Paths.get(args[1])) && Files.isDirectory(Paths.get(args[1]))) {
            switch (args[0]) {
                case "-s":
                    Guard guard = new Guard(Constants.LISTENING_PORT, args[1]);
                    return;
                case "-c":
                    Client client = new Client(args[1]);
                    return;
            }
        }

        Logger.log("Please give proper arguments. To start a fileserver.server give the home directory, for example: \n" +
                "java -jar jarfile.jar -s /home/user/folder/ \n" +
                "When starting a fileserver.client, give the download directory: \n" +
                "java -jar jarfile.jar -c /home/user/downloadFolder/");
    }
}
