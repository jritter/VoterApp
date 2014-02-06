package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.singleencryptionballot.xml;

import org.simpleframework.xml.Element;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLEqualityProof;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;

public class XMLPartDecryption {

	@Element
	private XMLGqElement value;
	@Element
	private XMLEqualityProof proof;

	public XMLPartDecryption(XMLGqElement value, XMLEqualityProof proof) {
		super();
		this.value = value;
		this.proof = proof;
	}

	public XMLGqElement getValue() {
		return value;
	}

	public void setValue(XMLGqElement value) {
		this.value = value;
	}

	public XMLEqualityProof getProof() {
		return proof;
	}

	public void setProof(XMLEqualityProof proof) {
		this.proof = proof;
	}

}
