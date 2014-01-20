package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

public class ProtocolBallotOption implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Pair ballotOptionEncryption;
	private final Tuple validityProof;
	
	public ProtocolBallotOption(Pair ballotOptionEncryption, Tuple validityProof) {
		super();
		this.ballotOptionEncryption = ballotOptionEncryption;
		this.validityProof = validityProof;
		
	}
	
	public Pair getBallotOptionEncryption() {
		return ballotOptionEncryption;
	}
	
	public Tuple getValidityProof() {
		return validityProof;
	}

}
