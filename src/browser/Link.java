package browser;
public class Link extends GemtextElement {
    public String url;
    public String label;

    public Link(String url, String label) {
        this.url = url;
        this.label = label;
    }

    public String getType() {
        return "link";
    }
}
