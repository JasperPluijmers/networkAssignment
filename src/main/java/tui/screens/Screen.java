package tui.screens;

public class Screen {
    private String title;
    private String content;
    private String seperator = new String(new char[SCREEN_SIZE]).replace('\0', '-');
    private static final int SCREEN_SIZE = 50;
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
        stringBuilder.append(center(title) + "\n");
        stringBuilder.append(seperator + "\n");
        stringBuilder.append(content + "\n");
        for (int i = countLines(stringBuilder.toString()); i < VERTICAL_SCREEN_SIZE; i++) {
            stringBuilder.append("\n");
        }
        stringBuilder.append(seperator + "\n");
        return stringBuilder.toString();
    }

    public void handleCommand(String command) {

    }

    public static String center(String text){
        String out = String.format("%"+SCREEN_SIZE+"s%s%"+SCREEN_SIZE+"s", "",text,"");
        float mid = (out.length()/2);
        float start = mid - (SCREEN_SIZE/2);
        float end = start + SCREEN_SIZE;
        return out.substring((int)start, (int)end);
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
