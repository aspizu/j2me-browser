package browser;

public class Quote extends GemtextElement {
    public String content;

    public Quote(String content) {
        this.content = content;
    }
    public String getType() {
        return "quote";
    }
}
