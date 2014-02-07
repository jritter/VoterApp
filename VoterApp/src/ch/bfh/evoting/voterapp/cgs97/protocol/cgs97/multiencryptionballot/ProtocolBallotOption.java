package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

/**
 * 
 * This class represents an option of a ProtocolBallot and contains an encryption and a validity proof.
 * 
 * @author Juerg Ritter
 *
 */
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