package browser;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.game.GameCanvas;

class UI implements CommandListener {
	Display display = null;
	GameCanvas canvas = null;
	int w = 0;
	int h = 0;
	Graphics g = null;
	Font font = null;
	int fontHeight = 0;
	String thisMenu = null;
	String currentMenu = "root";
	Vector menuHistory = null;
	int selectedItem = 0;
	int thisItem = -1;
	int menuLength = 0;
	boolean eventSelected = false;
	public String promptValue = null;
	boolean eventPromptOk = false; 
	public int time;
	int anchor = Graphics.TOP|Graphics.LEFT;
	Command okCommand;
	Command exitCommand;
	
	UI(GameCanvas canvas, Display display, int w, int h, Graphics g) {
		this.canvas = canvas;
		this.w = w;
		this.h = h;
		this.g = g;
		this.menuHistory = new Vector();
		this.font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
		this.fontHeight = font.getHeight();
		this.g.setFont(font);
		this.display = display;
	}
	
	public void beginMenu(String id) {
		thisMenu = id;
		thisItem = -1;
		if (thisMenu.equals(currentMenu)) {
			g.setColor(0x000000);
			g.fillRect(0, 0, w, h);
		}
	}
	
	public void endMenu() {
		if (thisMenu.equals(currentMenu)) {
			menuLength = thisItem + 1;
		}
	}
	
	public void setMenu(String id) {
		menuHistory.addElement(currentMenu);
		currentMenu = id;
		selectedItem = 0;
		eventSelected = false;
	}
	
	public void previousMenu() {
		if (menuHistory.size() > 0) {
			currentMenu = (String) menuHistory.lastElement();
			menuHistory.removeElementAt(menuHistory.size() - 1);
		} else {
			currentMenu = "root";
		}
		selectedItem = 0;
		eventSelected = false;
	}
	
	public boolean button(String label) {
		thisItem++;
		if (!thisMenu.equals(currentMenu)) return false;
		drawMenuItem(label);
		if (thisItem == selectedItem) {
			if (eventSelected) {
				eventSelected = false;
				return true;
			}
		}
		return false;
	}
	
	public boolean prompt(String label) {
		thisItem++;
		if (!thisMenu.equals(currentMenu)) return false;
		drawMenuItem(label);
		if (thisItem == selectedItem) {
			if (eventSelected) {
				eventSelected = false;
				TextBox textBox = new TextBox(label, "", 255, TextField.ANY);
				createPrompt(textBox, "OK", "Cancel");
			}
			if (eventPromptOk) {
				eventPromptOk = false;
				return true;
			}
		}
		return false;
	}
	
	public void keyPressed(int keyCode) {
		int gameAction = canvas.getGameAction(keyCode);
		if (gameAction == Canvas.UP) {
			selectedItem--;
			if (selectedItem < 0) {
				selectedItem = 0;
			}
		} else if (gameAction == Canvas.DOWN) {
			selectedItem++;
			if (selectedItem >= menuLength) {
				selectedItem = menuLength - 1;
			}
		} else if (gameAction == Canvas.FIRE) {
			eventSelected = true;
		}
	}
	
	void drawMenuItem(String label) {
		int height = fontHeight + 4;
		int y = thisItem * height;
		if (thisItem == selectedItem) {
			g.setColor(0xFFFFFF);
			g.fillRect(0, y, w, height);
			g.setColor(0x000000);
		} else {
			g.setColor(0x000000);
			g.fillRect(0, y, w, height);
			g.setColor(0xFFFFFF);
		}
		g.drawString(label, 2, y + 2, anchor);
	}
	
	void createPrompt(TextBox textBox, String okCommandLabel, String exitCommandLabel) {
		okCommand = new Command(okCommandLabel, Command.OK, 1);
		exitCommand = new Command(exitCommandLabel, Command.CANCEL, 1);
		textBox.addCommand(okCommand);
		textBox.addCommand(exitCommand);
		textBox.setCommandListener(this);
		display.setCurrent(textBox);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == okCommand) {
			if (d instanceof TextBox) {
				TextBox textBox = (TextBox) d;
				promptValue = textBox.getString();
				okCommand = null;
				exitCommand = null;
				eventPromptOk = true;
				display.setCurrent(canvas);
			}
		} else if (c == exitCommand) {
			if (d instanceof TextBox) {
				okCommand = null;
				exitCommand = null;
				display.setCurrent(canvas);
			}
		}
	}
}