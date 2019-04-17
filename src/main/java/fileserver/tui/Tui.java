package fileserver.tui;

import fileserver.client.Client;
import fileserver.tui.screens.Screen;
import fileserver.utils.Logger;

import java.util.Scanner;

/**
 * The Tui has 2 functions: it displays a Screen and reads user input. The user input gets forwarded to the
 * handleCommand() of the current Screen;
 */
public class Tui implements Runnable {
    private final Client client;
    private Screen currentScreen;

    public Tui(Client client) {
        this.client = client;
    }

    public void update(Screen newScreen){
        System.out.print("\033[H\033[2J");
        System.out.flush();
        this.currentScreen = newScreen;
        System.out.println(currentScreen.getScreen());
        Logger.log("Input, for help press [enter]:");
    }

    @Override
    public void run() {
        Scanner line = new Scanner(System.in);

        while (true) {
            String nextLine = line.nextLine();
            currentScreen.handleCommand(nextLine);
        }
    }
}
