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
//		Tuple subPartT = (Tuple) validityProof.getAt(0); // list of Gq
//															// pairs
//		Tuple subPartC = (Tuple) validityProof.getAt(1); // list
//		// ZModElements
//		Tuple subPartS = (Tuple) validityProof.getAt(2); // list
//		// ZModElements
//		List<XMLGqPair> valueListT = new ArrayList<XMLGqPair>();
//		for (Element e : subPartT.getAll()) {
//			Tuple tuple = (Tuple) e;
//			XMLGqPair pair = new XMLGqPair(new XMLGqElement(((BigInteger)tuple
//					.getAt(0).getValue()).toString(10)),
//					new XMLGqElement(((BigInteger)tuple.getAt(1).getValue())
//							.toString(10)));
//			valueListT.add(pair);
//		}
//		List<XMLZqElement> valueListC = new ArrayList<XMLZqElement>();
//		for (Element e : subPartC.getAll()) {
//			valueListC.add(new XMLZqElement(((BigInteger)e.getValue()).toString(10)));
//		}
//		List<XMLZqElement> valueListS = new ArrayList<XMLZqElement>();
//		for (Element e : subPartS.getAll()) {
//			valueListS.add(new XMLZqElement(((BigInteger)e.getValue()).toString(10)));
//		}
//
//		this.ballotValidityProof = new XMLValidityProof(
//				valueListT, valueListC, valueListS);
		
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
