package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

public class ProtocolBallot implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Tuple ballot;
	private final Tuple validityProof;
	private final long timestamp;
	
	
	public ProtocolBallot(Tuple ballot,
			Tuple validityProof) {
		this.ballot = ballot;
		this.validityProof = validityProof;
		this.timestamp = System.currentTimeMillis();
	}


	public Tuple getBallot() {
		return ballot;
	}


	public Tuple getValidityProof() {
		return validityProof;
	}


	public long getTimestamp() {
		return timestamp;
	}
	
}
