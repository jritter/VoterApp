package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml;

import org.simpleframework.xml.Element;

public class XMLEqualityProof {

	@Element
	private XMLGqElement valueT1;
	@Element
	private XMLGqElement valueT2;
	@Element
	private XMLZqElement valueC;
	@Element
	private XMLZqElement valueS;

	public XMLEqualityProof(XMLGqElement valueT1, XMLGqElement valueT2,
			XMLZqElement valueC, XMLZqElement valueS) {
		super();
		this.valueT1 = valueT1;
		this.valueT2 = valueT2;
		this.valueC = valueC;
		this.valueS = valueS;
	}

	public XMLGqElement getValueT1() {
		return valueT1;
	}

	public void setValueT1(XMLGqElement valueT1) {
		this.valueT1 = valueT1;
	}

	public XMLGqElement getValueT2() {
		return valueT2;
	}

	public void setValueT2(XMLGqElement valueT2) {
		this.valueT2 = valueT2;
	}

	public XMLZqElement getValueC() {
		return valueC;
	}

	public void setValueC(XMLZqElement valueC) {
		this.valueC = valueC;
	}

	public XMLZqElement getValueS() {
		return valueS;
	}

	public void setValueS(XMLZqElement valueS) {
		this.valueS = valueS;
	}

}
