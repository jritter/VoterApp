package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import java.math.BigInteger;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.ProtocolBallotOption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqPair;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLValidityProof;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;


public class XMLBallotOption {
	
	private XMLValidityProof ballotValidityProof;
	private XMLGqPair ballotOptionEncryption;
	
	public XMLBallotOption(XMLValidityProof ballotValidityProof,
			XMLGqPair ballotOptionEncryption) {
		super();
		this.ballotValidityProof = ballotValidityProof;
		this.ballotOptionEncryption = ballotOptionEncryption;
	}
	
	public XMLBallotOption(ProtocolBallotOption protocolBallotOption){
		
		XMLGqElement encryptionLeft = new XMLGqElement(((BigInteger)protocolBallotOption.getBallotOptionEncryption().getAt(0).getValue()).toString(10));
		XMLGqElement encryptionRight = new XMLGqElement(((BigInteger)protocolBallotOption.getBallotOptionEncryption().getAt(1).getValue()).toString(10));
		
		ballotOptionEncryption = new XMLGqPair(encryptionLeft, encryptionRight);
		
		Tuple validityProof = protocolBallotOption.getValidityProof();
		
		this.ballotValidityProof = new XMLValidityProof(
				validityProof);
		
	}

	public XMLValidityProof getBallotValidityProof() {
		return ballotValidityProof;
	}

	public void setBallotValidityProof(XMLValidityProof ballotValidityProof) {
		this.ballotValidityProof = ballotValidityProof;
	}

	public XMLGqPair getBallotOptionEncryption() {
		return ballotOptionEncryption;
	}

	public void setBallotOptionEncryption(XMLGqPair ballotOptionEncryption) {
		this.ballotOptionEncryption = ballotOptionEncryption;
	}
	
}
