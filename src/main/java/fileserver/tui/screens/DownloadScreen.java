package fileserver.tui.screens;

import fileserver.tui.functionalInterfaces.MenuCommand;
import fileserver.tui.functionalInterfaces.stringMenuCommand;

/**
 * The download screen shows how far along a download is, in the end it shows the time spent on the download.
 * It has pause and unpause functionality.
 */
public class DownloadScreen extends Screen {

    private String content;
    private stringMenuCommand askFiles;
    private MenuCommand pause;
    private boolean done;

    public DownloadScreen(String name, stringMenuCommand askFiles, MenuCommand pause) {
        super("Downloading file: " + name, "Packets written: \nPercentage done: ");
        this.askFiles = askFiles;
        this.pause = pause;
    }

    public Screen update(int lastWritten, float v) {
        this.content = "Packets written: " + lastWritten + "\nPercentage done: " + v + "%" + "\nPress [enter] to pause/unpause";
        super.setContent(content);
        return this;
    }

    public void reset() {
        askFiles.menuFunction("/");
    }

    public Screen last(boolean correct, float lapsedTime, long length) {
        done = true;
        this.content = "Packets written: all" + "\nPercentage done: " + "100%";
        content += "\nPacket transferred correctly: " + correct + "\nTransferred: " + length + " bytes in " + lapsedTime + " seconds." + "\nPress [enter] to go back";
        super.setContent(content);
        return this;
    }

    @Override
    public void handleCommand(String command) {
        if (done) {
            reset();
        } else {
            pause.menuFunction();
        }
    }
}
