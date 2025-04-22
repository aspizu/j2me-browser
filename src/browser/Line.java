package browser;

import javax.microedition.lcdui.Font;
import java.util.Vector;

class Line {
	public String type;
	public String text;
	public GemtextElement element;
	public int position;
	static final int START = 1;
	static final int END = 2;
	static final int MIDDLE = 3;
	
	Line(String type, String text, GemtextElement element, int position) {
		this.type = type;
		this.text = text;
		this.element = element;
		this.position = position;
	}
	
	static int getPosition(int index, int length) {
		if (index == 0) {
			return Line.START;
		}
		if (index == length - 1) {
			return Line.END;
		}
		return Line.MIDDLE;
	}
	
	Font getFont(AppCanvas canvas) {
		if (type.equals("heading")) {
            return canvas.headingFont;
        } else if (type.equals("link")) {
            return canvas.linkFont;
        } else if (type.equals("list_item")) {
            return canvas.listItemFont;
        } else if (type.equals("paragraph")) {
            return canvas.paragraphFont;
        } else if (type.equals("preformatted")) {
            return canvas.preformattedFont;
        } else if (type.equals("quote")) {
            return canvas.quoteFont;
        }
		return null;
	}
	
	int getMarginTop() {
		if (type.equals("heading")) {
			if (position == Line.START) {
            	return 20;
            }
		}
		return 0;
	}
	
	int getMarginBottom(boolean isSingle) {
		return (isSingle || position == Line.END) ? 10 : 0;
	}
	
	int getHeight(AppCanvas canvas, boolean isSingle) {
		Font font = getFont(canvas);
		int marginTop = getMarginTop();
		int marginBottom = getMarginBottom(isSingle);
		return marginTop + font.getHeight() + marginBottom;
	}
	
	/**
	 * Determines whether this line is considered "single" within the given document at index i.
	 */
	boolean isSingleLine(Vector document, int i) {
		return position == Line.START
			? (i == document.size() - 1)
				? true
				: element.hashCode() != ((Line) document.elementAt(i + 1)).element.hashCode()
			: false;
	}
	
	public String toString() {
		return "Line(type = " + type + ", text = " + text + ", position = " + position + ")";
	}
}
