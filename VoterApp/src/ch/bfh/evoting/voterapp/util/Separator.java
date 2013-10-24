package ch.bfh.evoting.voterapp.util;

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
