package browser;

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
}
