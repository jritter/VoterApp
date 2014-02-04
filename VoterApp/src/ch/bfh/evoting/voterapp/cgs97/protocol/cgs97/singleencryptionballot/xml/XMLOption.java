package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.singleencryptionballot.xml;

import org.simpleframework.xml.Element;

import ch.bfh.evoting.voterapp.cgs97.entities.Option;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;

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
	
	public XMLOption(Option option, XMLGqElement representation){
		this.text = option.getText();
		this.votes = option.getVotes();
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
	
	public XMLGqElement getRepresentation() {
		return representation;
	}

	public void setRepresentation(XMLGqElement representation) {
		this.representation = representation;
	}
}