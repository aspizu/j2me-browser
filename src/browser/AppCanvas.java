package browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

class AppCanvas extends GameCanvas {
	Image cursorImage = null;
    Font paragraphFont;
    Font headingFont;
    Font linkFont;
    Font listItemFont;
    Font preformattedFont;
    Font quoteFont;
    Font menuFont;
    Browser midlet;
    Graphics g;
    int w;
    int h;
    int time = 0;
    String url = "gemini://skyjake.fi/lagrange/";
    Vector history = null;
    Vector document = null;
    Image[] documentCache = null;
    boolean isLoading = false;
    boolean upPressed = false;
    boolean downPressed = false;
    boolean softLeftPressed = false;
    boolean softRightPressed = false;
    int scrollY = 0;
    int cursorVel = 0;
    int documentPadding = 6;
    int documentHeight = 0;
    int cursorY = 0;
    int viewh;
    GemtextElement hoveredElement = null;
    Calendar cal;

    AppCanvas(Browser midlet) {
        super(false);
        this.midlet = midlet;
    }

    void run() throws IOException {
        setFullScreenMode(true);
        g = getGraphics();
        paragraphFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        headingFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_LARGE);
        linkFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        listItemFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
        preformattedFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        quoteFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_ITALIC, Font.SIZE_MEDIUM);
        menuFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL);
        cal = Calendar.getInstance(TimeZone.getDefault());
        w = getWidth();
        h = getHeight();
        viewh = h - 16;
        cursorImage = Image.createImage("/cursor.png");
        isLoading = true;
        new FetchDocumentThread(this).start();
        history = new Vector();
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
        if (!(upPressed || downPressed)) {
        	cursorVel = 0;
        }
        if (upPressed) {
        	cursorVel -= 2;
        }
        if (downPressed) {
        	cursorVel += 2;
        }
        cursorY += cursorVel;
        if (cursorY - viewh/4 < scrollY) {
        	scrollY = cursorY - viewh/4; 
        }
        if (cursorY - viewh*3/4 > scrollY) {
        	scrollY = cursorY - viewh*3/4;
        }
        if (scrollY < 0) {
        	scrollY = 0;
        }
        if (cursorY < 0) {
        	cursorY = 0;
        }
        if (cursorY > documentHeight) {
    		cursorY = documentHeight;
    	}
    	if (scrollY+viewh > documentHeight) {
    		scrollY = documentHeight-viewh;
    	}
        hoveredElement = null;
        if (document != null) {	
        	int y = 0;
            for (int i = 0; i < document.size(); i++) {
                Line line = (Line) document.elementAt(i);
                boolean isSingle = line.isSingleLine(document, i);
                int height = line.getHeight(this, isSingle);
            	if (y <= cursorY && cursorY <= y + height) {
            		hoveredElement = line.element;
            		break;
            	}
            	y += height;
            }
        }
    }

    void render() {
    	g.setColor(0xFFFFFF);
    	g.fillRect(0, 0, w, viewh);
        if (document != null) {
        	document(document);
        	renderScrollBar();
        }
        g.setColor(0xEEEEEE);
        g.fillRect(0, h-16, w, 16);
        g.setFont(menuFont);
        int softKeyWidth = 80;
        if (softLeftPressed) {
        	g.setColor(0x888888);
        	g.fillRect(0, h-16, softKeyWidth, 16);
        	g.setColor(0xEEEEEE);
        } else {
        	g.setColor(0x888888);
        }
        g.drawString("Options", softKeyWidth/2 - menuFont.stringWidth("Options")/2, h-8-menuFont.getHeight()/2, Graphics.TOP|Graphics.LEFT);
        
        if (softRightPressed) {
        	g.setColor(0x888888);
        	g.fillRect(w-softKeyWidth, h-16, softKeyWidth, 16);
        	g.setColor(0xEEEEEE);
        } else {
        	g.setColor(0x888888);
        }
        g.drawString("Back", w - softKeyWidth/2 + menuFont.stringWidth("Back")/2, h-8-menuFont.getHeight()/2, Graphics.TOP|Graphics.RIGHT);
        String time = getTime();
        g.setColor(0x888888);
        g.drawString(time, w/2 + menuFont.stringWidth(time)/2, h-8-menuFont.getHeight()/2, Graphics.TOP|Graphics.RIGHT);
        
        g.drawImage(cursorImage, w/4, cursorY - scrollY, Graphics.TOP|Graphics.LEFT);
    }
    
    String getTime() {
    	int hour = cal.get(Calendar.HOUR);
    	int minute = cal.get(Calendar.MINUTE);
    	int am_pm = cal.get(Calendar.AM_PM);
    	if (hour == 0) {
    		hour = 12;
    	}
    	String minuteStr = (minute < 10 ? "0" : "") + minute;
    	String ampm = (am_pm == Calendar.AM) ? "AM" : "PM";
    	return hour + ":" + minuteStr + " " + ampm;
    }
    
    void renderScrollBar() {
    	int scrollbarWidth = 2;
    	int scrollbarX = w - scrollbarWidth;

    	// Calculate scrollbar height based on visible area
    	int scrollbarHeight = Math.max((viewh * viewh) / documentHeight, 10); // Ensure a minimum size

    	// Calculate scrollbar Y position based on scroll
    	int scrollbarY = (scrollY * (viewh - scrollbarHeight)) / (documentHeight - viewh);

    	g.setColor(0xAAAAAA);
    	g.fillRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);
    }

    void document(Vector document) {
        int y = 0 - scrollY;
        for (int i = 0; i < document.size(); i++) {
            Line line = (Line) document.elementAt(i);
            boolean isSingle = line.isSingleLine(document, i);
            String type = line.type;
            Font font = line.getFont(this);
            int marginTop = line.getMarginTop();
            int height = line.getHeight(this, isSingle);
            if (0 <= (y + height) && y <= viewh) {
            	if (documentCache[i] == null) {
            		Image image = Image.createImage(w, height);
            		documentCache[i] = image;
            		Graphics cg = image.getGraphics();
            		cg.setColor(getColor(type));
            		cg.setFont(font);
            		int marginLeft = 0;
            		if (line.type == "list_item") {
            			marginLeft = 10;
            		}
            		cg.drawString(line.text, documentPadding + marginLeft, marginTop, Graphics.TOP|Graphics.LEFT);
            		if (line.type == "list_item" && line.position == Line.START) {
            			cg.setColor(0x8888FF);
            			cg.fillRect(documentPadding, marginTop + font.getHeight()/2 - 2, 4, 4);
            		}
            	}
            	if (line.element == hoveredElement && hoveredElement.getType() == "link") {
            		g.setColor(0xFF0000);
            		g.setFont(font);
            		g.drawString(line.text, documentPadding, y+marginTop, Graphics.TOP|Graphics.LEFT);
            	} else {
            		g.drawImage(documentCache[i], 0, y, Graphics.TOP|Graphics.LEFT);
            	}
            }
            y += height;
        }
        documentHeight = y + scrollY;
    }
    
    int getColor(String type) {
    	if (type.equals("heading")) {
		    return(0x1A237E); // Deep Indigo - strong and eye-catching
		} else if (type.equals("link")) {
		    return(0x1565C0); // Vivid Blue - stands out but easy on the eyes
		} else if (type.equals("list_item")) {
		    return(0x455A64); // Slate Gray - neutral and readable
		} else if (type.equals("paragraph")) {
		    return(0x37474F); // Dark Gray Blue - subtle and clean
		} else if (type.equals("preformatted")) {
		    return(0x90A4AE); // Light Gray Blue - good for code blocks
		} else if (type.equals("quote")) {
		    return(0x388E3C); // Rich Green - distinct but calm
		}
		return 0;
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
        } else if (action == GameCanvas.FIRE) {
        	if (!(isLoading || hoveredElement == null) && hoveredElement.getType() == "link") {
        		navigate(((Link)hoveredElement).url);
        	}
        } else if (keyCode == -6) {
        	// LEFT SOFT KEY
        	softLeftPressed = true;
        } else if (keyCode == -7) {
        	// RIGHT SOFT KEY
        	softRightPressed = true;
        }
    }
    
    void navigateBack() {
    	if (history.isEmpty()) {
    		return;
    	}
    	url = (String) history.elementAt(history.size() - 1);
    	history.removeElementAt(history.size() - 1);
		new FetchDocumentThread(this).start();
    }
    
    void navigate(String url) {
    	isLoading = true;
		history.addElement(this.url);
		this.url = url;
		new FetchDocumentThread(this).start();
    }
    
    protected void keyReleased(int keyCode) {
    	int action = getGameAction(keyCode);
    	if (action == GameCanvas.UP) {
    		upPressed = false;
    	} else if (action == GameCanvas.DOWN) {
    		downPressed = false;
    	} else if (keyCode == -6) {
    		softLeftPressed = false;
    	} else if (keyCode == -7) {
    		softRightPressed = false;
    		navigateBack();
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
			text = canvas.gemget(canvas.url);
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
		int width = canvas.w - canvas.documentPadding*2;
		for (int i = 0; i < document.length; i++) {
			if (document[i].getType() == "heading") {
				Heading element = (Heading) document[i];
				Vector wrappedLines = TextWrapper.wrapText(element.text, width, canvas.headingFont);
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
				Vector wrappedLines = TextWrapper.wrapText(element.text, width, canvas.paragraphFont);
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
				Vector wrappedLines = TextWrapper.wrapText(element.label, width, canvas.linkFont);
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
				Vector wrappedLines = TextWrapper.wrapText(element.content, width - 10, canvas.listItemFont);
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
				Vector wrappedLines = TextWrapper.wrapText(element.content, width, canvas.quoteFont);
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
		canvas.documentCache = new Image[lines.size()];
		canvas.scrollY = 0;
		canvas.cursorY = 0;
		canvas.documentHeight = 0;
		canvas.cursorVel = 0;
	}
}