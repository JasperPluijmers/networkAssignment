package tui;

public class MenuScreen extends Screen {

    public MenuScreen(String content) {
        super("File Client", content);
    }

    public static void main(String[] args) {
        Screen screen = new MenuScreen( "1 - Set download folder \n2 - Discover");
        System.out.println(screen.getScreen());
    }
}
