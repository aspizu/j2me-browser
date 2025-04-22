package browser;
public class Paragraph extends GemtextElement {
    public String text;

    public Paragraph(String text) {
        this.text = text;
    }

    public String getType() {
        return "paragraph";
    }
}
