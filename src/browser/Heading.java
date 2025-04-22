package browser;
public class Heading extends GemtextElement {
    public int level;
    public String text;

    public Heading(int level, String text) {
        this.level = level;
        this.text = text;
    }

    public String getType() {
        return "heading";
    }

    public String toString() {
        return "Heading(level=" + level + ", text=\"" + text + "\")";
    }
}
