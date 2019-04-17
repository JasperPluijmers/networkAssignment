package fileserver.tui.screens;

import fileserver.tui.functionalInterfaces.MenuCommand;
import fileserver.tui.functionalInterfaces.stringMenuCommand;
import fileserver.utils.Logger;

/**
 * The main menu screen, allows for discovery of servers and changing of the download directory
 */
public class MenuScreen extends Screen {

    private MenuCommand discover;
    private stringMenuCommand setDownloadDirectory;

    public MenuScreen(MenuCommand discover, stringMenuCommand setDownloadDirectory, String directory) {
        super("File Client", "Current  download directory: " + directory + "\n\n" + "1- Set download directory\n2- Discover servers");
        this.discover = discover;
        this.setDownloadDirectory = setDownloadDirectory;
    }

    @Override
    public void handleCommand(String command) {
        String[] args = command.split(" ");
        int commandNumber = 0;
        try {
            commandNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e){
            Logger.log("Command invalid");
            help();
            return;
        }

        switch (commandNumber) {
            case 1:
                if (args.length == 2) {
                    setDownloadDirectory.menuFunction(args[1]);
                } else {
                    Logger.log("Command invalid");
                    help();
                    return;
                }
                break;
            case 2:
                discover.menuFunction();
                break;
            default:
                Logger.log("Command invalid");
                help();
                return;
        }
    }

    private void help() {
        Logger.log("Use 1 to set a download directory");
        Logger.log("example: \"1 /home/user/downloads\"");
        Logger.log("Use 2 to start the discovery of servers on the network");
    }
}
