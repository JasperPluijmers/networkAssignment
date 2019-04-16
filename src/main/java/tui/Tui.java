package tui;

import client.Client;
import tui.screens.Screen;
import utils.Logger;

import java.io.IOException;
import java.util.Scanner;

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
        try {
            Runtime.getRuntime().exec("clear");
        } catch (IOException e) {
            Logger.log("Can't clear screen");
            System.exit(1);
        }
        System.out.println(currentScreen.getScreen());
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
