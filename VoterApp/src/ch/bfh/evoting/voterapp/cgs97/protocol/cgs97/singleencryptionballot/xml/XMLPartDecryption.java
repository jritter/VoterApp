package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.singleencryptionballot.xml;

import org.simpleframework.xml.Element;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLEqualityProof;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;

public class XMLPartDecryption {

	@Element
	private XMLGqElement partDecryption;
	@Element
	private XMLEqualityProof proof;

	public XMLPartDecryption(XMLGqElement partDecryption, XMLEqualityProof proof) {
		super();
		this.partDecryption = partDecryption;
		this.proof = proof;
	}

	public XMLGqElement getPartDecryption() {
		return partDecryption;
	}

	public void setPartDecryption(XMLGqElement partDecryption) {
		this.partDecryption = partDecryption;
	}

	public XMLEqualityProof getProof() {
		return proof;
	}

	public void setProof(XMLEqualityProof proof) {
		this.proof = proof;
	}

}