package browser;
public class ListItem extends GemtextElement {
    public String content;

    public ListItem(String content) {
        this.content = content;
    }

    public String getType() {
        return "list_item";
    }
}
