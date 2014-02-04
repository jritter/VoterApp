package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import org.simpleframework.xml.Element;

import ch.bfh.evoting.voterapp.cgs97.entities.Option;

public class XMLOption {

	@Element
	private String text;
	@Element
	private int votes;
	
	public XMLOption(String text, int votes) {
		super();
		this.text = text;
		this.votes = votes;
	}
	
	public XMLOption(Option option){
		this.text = option.getText();
		this.votes = option.getVotes();
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