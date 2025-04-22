package browser;

import java.util.Vector;
import javax.microedition.lcdui.Font;

public class TextWrapper {

    public static Vector wrapText(String text, int maxWidth, Font font) {
        Vector lines = new Vector();

        // Manually split text into words
        Vector words = new Vector();
        int len = text.length();
        int start = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                if (i > start) {
                    words.addElement(text.substring(start, i));
                }
                start = i + 1;
            }
        }
        // Add last word
        if (start < len) {
            words.addElement(text.substring(start));
        }

        // Build lines within maxWidth
        String currentLine = "";
        for (int i = 0; i < words.size(); i++) {
            String word = (String) words.elementAt(i);
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;

            if (font.stringWidth(testLine) <= maxWidth) {
                currentLine = testLine;
            } else {
                if (currentLine.length() > 0) {
                    lines.addElement(currentLine);
                }
                currentLine = word;
            }
        }

        if (currentLine.length() > 0) {
            lines.addElement(currentLine);
        }

        return lines;
    }
}
