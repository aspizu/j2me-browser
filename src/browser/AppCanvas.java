package browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

class AppCanvas extends GameCanvas {
    Font paragraphFont;
    Font headingFont;
    Font linkFont;
    Font listItemFont;
    Font preformattedFont;
    Font quoteFont;
    Browser midlet;
    Graphics g;
    int w;
    int h;
    int time = 0;
    Vector document = null;
    boolean isLoading = false;
    boolean upPressed = false;
    boolean downPressed = false;
    int scrollY = 0;
    int scrollVel = 0;

    AppCanvas(Browser midlet) {
        super(false);
        this.midlet = midlet;
    }

    void run() {
        setFullScreenMode(true);
        g = getGraphics();
        paragraphFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        headingFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        linkFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        listItemFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        preformattedFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        quoteFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
        w = getWidth();
        h = getHeight();

        while (true) {
            update();
            render();
            flushGraphics();
            time++;
            try {
                Thread.sleep(1000/16);
            } catch (InterruptedException ignored) {}
        }
    }

    void update() {
        if (document != null) {
            isLoading = false;
        }
        if (document == null && !isLoading) {
            isLoading = true;
            new FetchDocumentThread(this).start();
        }
        if (!(upPressed || downPressed)) {
        	scrollVel = 0;
        }
        if (upPressed) {
        	scrollVel -= 2;
        }
        if (downPressed) {
        	scrollVel += 2;
        }
        scrollY += scrollVel;
        if (scrollY < 0) {
        	scrollY = 0;
        }
    }

    void render() {
        g.setColor(0x000000);
        g.fillRect(0, 0, w, h);
        
        if (document != null) {
        	document(document);
        }
    }

    void document(Vector document) {
        int y = -scrollY;
        for (int i = 0; i < document.size(); i++) {
            Line line = (Line) document.elementAt(i);
            String type = line.type;
            Font font = null;
            if (type.equals("heading")) {
                g.setColor(0xFF0000);
                font = headingFont;
            } else if (type.equals("link")) {
                g.setColor(0x0000FF);
                font = linkFont;
            } else if (type.equals("list_item")) {
                g.setColor(0xFFFF00);
                font = listItemFont;
            } else if (type.equals("paragraph")) {
                g.setColor(0xFFFFFF);
                font = paragraphFont;
            } else if (type.equals("preformatted")) {
                g.setColor(0xCCCCCC);
                font = preformattedFont;
            } else if (type.equals("quote")) {
                g.setColor(0x00FF00);
                font = quoteFont;
            }
            g.setFont(font);
            if (0 <= (y + font.getHeight())) {
            	g.drawString(line.text, 0, y, Graphics.TOP | Graphics.LEFT);
            }
            y += font.getHeight();
            if (y > h) {
            	return;
            }
        }
    }
    
    int text(Font font, String text, int x, int y) {
        int height = font.getHeight();
        String[] words = splitByWhitespace(text);
        for (int i = 0; i < words.length; i++) {
            int width = font.stringWidth(words[i]);
            if (x + width > w) {
                x = 0;
                y += height;
            }
            g.setFont(font);
            g.drawString(words[i], x, y, Graphics.TOP | Graphics.LEFT);
            x += width + 4;
        }
        y += height;
        return y;
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (action == GameCanvas.UP) {
        	upPressed = true;
        } else if (action == GameCanvas.DOWN) {
        	downPressed = true;
        }
    }
    
    protected void keyReleased(int keyCode) {
    	int action = getGameAction(keyCode);
    	if (action == GameCanvas.UP) {
    		upPressed = false;
    	} else if (action == GameCanvas.DOWN) {
    		downPressed = false;
    	}
    }

    static String[] splitByWhitespace(String input) {
        if (input == null || input.length() == 0) {
            return new String[0];
        }

        Vector parts = new Vector();
        int length = input.length();
        int start = 0;

        while (start < length) {
            while (start < length && isWhitespace(input.charAt(start))) {
                start++;
            }

            int end = start;
            while (end < length && !isWhitespace(input.charAt(end))) {
                end++;
            }

            if (start < end) {
                parts.addElement(input.substring(start, end));
                start = end;
            }
        }

        String[] result = new String[parts.size()];
        parts.copyInto(result);
        return result;
    }

    static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    String gemget(String url) throws IOException {
        HttpConnection connection = null;
        InputStream is = null;
        StringBuffer response = new StringBuffer();

        if (url.startsWith("gemini://")) {
            url = url.substring(9);
        }

        url = replaceUnsafeChars(url);

        try {
            String fullUrl = "http://portal.mozz.us/gemini/" + url + "?raw=1";
            connection = (HttpConnection) Connector.open(fullUrl);
            connection.setRequestMethod(HttpConnection.GET);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpConnection.HTTP_OK) {
                is = connection.openInputStream();
                int ch;
                while ((ch = is.read()) != -1) {
                    response.append((char) ch);
                }
                return response.toString();
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Request failed: " + e.toString());
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ignored) {}
            }
        }

        return null;
    }

    private String replaceUnsafeChars(String url) {
        StringBuffer safe = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == ' ') {
                safe.append("%20");
            } else {
                safe.append(c);
            }
        }
        return safe.toString();
    }
}


class FetchDocumentThread extends Thread {
	AppCanvas canvas;
	
	FetchDocumentThread(AppCanvas canvas) {
		this.canvas = canvas; 
	}
	
	public void run() {
		String text = null;
		try {
			text = canvas.gemget("gemini://skyjake.fi/lagrange/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (text == null) {
			canvas.document = null;
			return;
		}
		GemtextElement[] document = GemtextParser.parseGemtext(text);
		Vector lines = new Vector();
		for (int i = 0; i < document.length; i++) {
			if (document[i].getType() == "heading") {
				Heading element = (Heading) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.text, canvas.w, canvas.headingFont);
				for (int j = 0; j < wrappedLines.size(); j++) {
					lines.addElement(new Line(
						"heading",
						(String) wrappedLines.elementAt(j),
						element,
						Line.getPosition(j, wrappedLines.size())
					));
				}
			} else if (document[i].getType() == "paragraph") {
				Paragraph element = (Paragraph) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.text, canvas.w, canvas.paragraphFont);
				for (int j = 0; j < wrappedLines.size(); j++) {
					lines.addElement(new Line(
						"paragraph",
						(String) wrappedLines.elementAt(j),
						element,
						Line.getPosition(j, wrappedLines.size())
					));
				}
			} else if (document[i].getType() == "link") {
				Link element = (Link) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.label, canvas.w, canvas.linkFont);
				for (int j = 0; j < wrappedLines.size(); j++) {
					lines.addElement(new Line(
						"link",
						(String) wrappedLines.elementAt(j),
						element,
						Line.getPosition(j, wrappedLines.size())
					));
				}
			} else if (document[i].getType() == "list_item") {
				ListItem element = (ListItem) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.content, canvas.w, canvas.linkFont);
				for (int j = 0; j < wrappedLines.size(); j++) {
					lines.addElement(new Line(
						"list_item",
						(String) wrappedLines.elementAt(j),
						element,
						Line.getPosition(j, wrappedLines.size())
					));
				}
			} else if (document[i].getType() == "preformatted") {
				Preformatted element = (Preformatted) document[i];
				for (int j = 0; j < element.lines.length; j++) {
					lines.addElement(new Line(
						"preformatted",
						(String) element.lines[j],
						element,
						Line.getPosition(j, element.lines.length)
					));
				}
			} else if (document[i].getType() == "quote") {
				Quote element = (Quote) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.content, canvas.w, canvas.linkFont);
				for (int j = 0; j < wrappedLines.size(); j++) {
					lines.addElement(new Line(
						"quote",
						(String) wrappedLines.elementAt(j),
						element,
						Line.getPosition(j, wrappedLines.size())
					));
				}
			}
		}
		canvas.document = lines;
	}
}