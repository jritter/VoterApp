package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

public class XMLGqElement {

	@Element
	private String value;

	public XMLGqElement() {
	}

	public XMLGqElement(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}