package ch.bfh.evoting.voterapp.util.xml;

import org.simpleframework.xml.Element;


public class XMLZqElement {

	@Element
	private String value;
	
	public XMLZqElement(){}
	
	public XMLZqElement(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}