package ch.bfh.evoting.voterapp.util;

/**
 * Class used to display a title with a line at its bottom in the UI
 * @author Jürg Ritter
 *
 */
public class Separator {
	
	private final String text;
	
	public Separator(String text) {
		this.text = text;
	}
	
	public String getText(){
		return text;
	}

	@Override
	public String toString() {
		return text;
	}
}
