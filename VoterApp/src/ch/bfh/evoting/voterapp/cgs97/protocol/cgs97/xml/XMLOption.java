package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

public class XMLOption {

	@Element
	private String text;
	@Element
	private int votes;
	@Element
	private XMLGqElement representation;

	public XMLOption(String text, int votes, XMLGqElement representation) {
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
}