package fileserver.tui.screens;

import fileserver.tui.functionalInterfaces.MenuCommand;
import fileserver.tui.functionalInterfaces.stringMenuCommand;

/**
 * The upload screen shows how far along a upload is, in the end it shows the time spent on the upload.
 * It has pause and unpause functionality.
 */
public class UploadScreen extends Screen {

    private String content;
    private stringMenuCommand askFiles;
    private MenuCommand pause;
    private boolean done;

    public UploadScreen(String name, stringMenuCommand askFiles, MenuCommand pause) {
        super("Uploading file: " + name, "Packets written: \nPercentage done: ");
        this.askFiles = askFiles;
        this.pause = pause;
    }

    public Screen update(int lastSent, float v) {
        this.content = "Packets sent: " + lastSent + "\nPercentage done: " + v + "%" + "\nPress [enter] to pause/unpause";
        super.setContent(content);
        return this;
    }

    public void reset() {
        askFiles.menuFunction("/");
    }

    public Screen last(float lapsedTime, long length) {
        done = true;
        this.content = "Packets sent: all" + "\nPercentage done: " + "100%";
        content += "\nTransferred: " + length + " bytes in " + lapsedTime + " seconds." + "\nPress [enter] to go back";
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
