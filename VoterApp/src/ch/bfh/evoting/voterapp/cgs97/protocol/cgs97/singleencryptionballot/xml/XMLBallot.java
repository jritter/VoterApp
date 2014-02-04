package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.singleencryptionballot.xml;

import org.simpleframework.xml.Element;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqPair;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLValidityProof;

public class XMLBallot {
	@Element
	private XMLGqPair ballotEncryption;
	@Element
	private XMLValidityProof ballotValidityProof;

	public XMLBallot(XMLGqPair ballotEncryption,
			XMLValidityProof ballotValidityProof) {
		super();
		this.ballotEncryption = ballotEncryption;
		this.ballotValidityProof = ballotValidityProof;
	}

	public XMLGqPair getBallotEncryption() {
		return ballotEncryption;
	}

	public void setBallotEncryption(XMLGqPair ballotEncryption) {
		this.ballotEncryption = ballotEncryption;
	}

	public XMLValidityProof getBallotValidityProof() {
		return ballotValidityProof;
	}

	public void setBallotValidityProof(XMLValidityProof ballotValidityProof) {
		this.ballotValidityProof = ballotValidityProof;
	}

}
