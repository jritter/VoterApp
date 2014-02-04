package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.ProtocolBallot;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.ProtocolBallotOption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLValidityProof;

public class XMLBallot {

	@ElementList
	private List<XMLBallotOption> ballotOptions;

	@Element
	private XMLValidityProof ballotValidityProof;

	public XMLBallot(List<XMLBallotOption> ballotOptions,
			XMLValidityProof ballotValidityProof) {
		super();
		this.ballotOptions = ballotOptions;
		this.ballotValidityProof = ballotValidityProof;
	}
	
	public XMLBallot(ProtocolBallot ballot){
		
		ballotOptions = new ArrayList<XMLBallotOption>();
		
		
		for (ProtocolBallotOption option : ballot.getOptions()){
			this.ballotOptions.add(new XMLBallotOption(option));
		}
		
		ballotValidityProof = new XMLValidityProof(ballot.getValidityProof());
	}

	public List<XMLBallotOption> getBallotOptions() {
		return ballotOptions;
	}

	public void setBallotOptions(List<XMLBallotOption> ballotOptions) {
		this.ballotOptions = ballotOptions;
	}

	public XMLValidityProof getBallotValidityProof() {
		return ballotValidityProof;
	}

	public void setBallotValidityProof(XMLValidityProof ballotValidityProof) {
		this.ballotValidityProof = ballotValidityProof;
	}

}
