package browser;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class Browser extends MIDlet {
    Display display;
    AppCanvas canvas;

    protected void startApp() {
        display = Display.getDisplay(this);
        canvas = new AppCanvas(this);
        display.setCurrent(canvas);
        canvas.run();
    }

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		// TODO Auto-generated method stub
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub
	}
}
