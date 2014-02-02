package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot;

import java.io.Serializable;
import java.util.List;

import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

public class ProtocolBallot implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<ProtocolBallotOption> options;
	private final Tuple validityProof;
	private final long timestamp;
	
	
	public ProtocolBallot(List<ProtocolBallotOption> options,
			Tuple validityProof) {
		this.options = options;
		this.validityProof = validityProof;
		this.timestamp = System.currentTimeMillis();
	}


	public List<ProtocolBallotOption> getOptions() {
		return options;
	}


	public Tuple getValidityProof() {
		return validityProof;
	}


	public long getTimestamp() {
		return timestamp;
	}
	
}