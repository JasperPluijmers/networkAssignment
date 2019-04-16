package tui.screens;

public class DownloadScreen extends Screen {

    private String content;
    private stringMenuCommand askFiles;
    private boolean done;

    public DownloadScreen(String name, stringMenuCommand askFiles) {
        super("Downloading file: " + name, "Packets written: \nPercentage done: ");
        this.askFiles = askFiles;
    }

    public Screen update(int lastWritten, float v) {
        this.content = "Packets written: " + lastWritten + "\nPercentage done: " + v;
        super.setContent(content);
        return this;
    }

    public void reset() {
        askFiles.menuFunction("/");
    }

    public Screen last(boolean correct, float lapsedTime, long length) {
        done = true;
        content += "\n Packet transferred correctly: " + correct + "\nTransferred: " + length + " bytes in " + lapsedTime + " seconds." + "\nPress [enter] to go back";
        super.setContent(content);
        return this;
    }

    @Override
    public void handleCommand(String command) {
        if (done) {
            reset();
        }
    }
}
