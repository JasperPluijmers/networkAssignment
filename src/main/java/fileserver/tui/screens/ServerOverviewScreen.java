package fileserver.tui.screens;

import fileserver.trafficUtils.Remote;
import fileserver.tui.functionalInterfaces.RemoteMenuCommand;
import fileserver.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerOverviewScreen extends Screen {

    private Map<Integer, Remote> remoteMap;
    private RemoteMenuCommand setup;

    public ServerOverviewScreen(RemoteMenuCommand setup, Set<Remote> remotes) {
        super("Discovered Servers");
        this.setup = setup;
        remoteMap = new HashMap<>();
        super.setContent(createContent(remotes));
    }


    private String createContent(Set<Remote> remotes) {
        StringBuilder stringBuilder = new StringBuilder();
        int count = 1;
        for (Remote remote : remotes) {
            remoteMap.put(count, remote);
            stringBuilder.append(count).append("- ").append(remote.getName()).append(" on ").append(remote.getAddress()).append("\n");
            count++;
        }
        return stringBuilder.toString();
    }

    @Override
    public void handleCommand(String command) {
        int commandNumber = 0;
        try {
            commandNumber = Integer.parseInt(command);
        } catch (NumberFormatException e) {
            help();
            return;
        }

        if (remoteMap.containsKey(commandNumber)) {
            setup.menuFunction(remoteMap.get(commandNumber));
        } else {
            help();
        }

    }

    private void help() {
        Logger.log("Command not recognized, to connect to a fileserver.server, use its number as input.");
    }
}
