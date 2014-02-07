package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.singleencryptionballot;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

/**
 * 
 * This class represents a ballot with one overall encryption (single encryption ballot encoding)
 * 
 * @author Juerg Ritter
 */
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
