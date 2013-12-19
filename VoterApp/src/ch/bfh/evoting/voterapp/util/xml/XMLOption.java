package ch.bfh.evoting.voterapp.util.xml;

import org.simpleframework.xml.Element;


public class XMLOption {

	@Element
	private String text;
	@Element
	private int votes;
	@Element
	private String representation;
	
	public XMLOption(){}
	
	public XMLOption(String text, int votes, String representation) {
		super();
		this.text = text;
		this.votes = votes;
		this.representation = representation;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	public String getRepresentation() {
		return representation;
	}
	public void setRepresentation(String representation) {
		this.representation = representation;
	}
	
	
}
