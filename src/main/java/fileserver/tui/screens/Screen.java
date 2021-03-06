package fileserver.tui.screens;

/**
 * A Screen is the standard format for the fileserver.tui. It has a title and content which are formatted in a frame. The Screen
 * can also come with commands that handle te different user inputs in the fileserver.tui.
 */
public class Screen {
    private String title;
    private String content;
    private String seperator = new String(new char[SCREEN_SIZE]).replace('\0', '-');
    private static final int SCREEN_SIZE = 80;
    private static final int VERTICAL_SCREEN_SIZE = 15;

    public Screen(String title) {
        this(title, "");
    }
    public Screen(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getScreen() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/").append(seperator).append("\\\n");
        stringBuilder.append("|").append(center(title)).append("|\n");
        stringBuilder.append("|").append(seperator).append("/\n");
        stringBuilder.append("| ").append(content.replace("\n", "\n| ")).append("\n");
        for (int i = countLines(stringBuilder.toString()); i < VERTICAL_SCREEN_SIZE; i++) {
            stringBuilder.append("|\n");
        }
        stringBuilder.append("\\").append(seperator).append("-\n");
        return stringBuilder.toString();
    }

    public void handleCommand(String command) {

    }

    public static String center(String text){
        String out = String.format("%"+SCREEN_SIZE+"s%s%"+SCREEN_SIZE+"s", "",text,"");
        float mid = (out.length()/2);
        float start = mid - (SCREEN_SIZE/2);
        float end = start + SCREEN_SIZE;
        return out.substring((int)start, (int)end );
    }

    public static int countLines(String str) {
        if(str == null || str.isEmpty())
        {
            return 0;
        }
        int lines = 1;
        int pos = 0;
        while ((pos = str.indexOf("\n", pos) + 1) != 0) {
            lines++;
        }
        return lines;
    }

    protected void setContent(String content) {
        this.content = content;
    }
}
