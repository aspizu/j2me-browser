package browser;
public class Preformatted extends GemtextElement {
    public String[] lines;

    public Preformatted(String[] lines) {
        this.lines = lines;
    }

    public String getType() {
        return "preformatted";
    }
}
