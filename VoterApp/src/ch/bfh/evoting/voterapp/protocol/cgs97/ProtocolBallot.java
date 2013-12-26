package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;

public class ProtocolBallot implements Serializable {
	

	private final ProtocolParticipant participant;
	private final Tuple ballot;
	private final Tuple validityProof;
	private final long timestamp;
	
	
	public ProtocolBallot(ProtocolParticipant participant, Tuple ballot,
			Tuple validityProof) {
		this.participant = participant;
		this.ballot = ballot;
		this.validityProof = validityProof;
		this.timestamp = System.currentTimeMillis();
	}


	public ProtocolParticipant getParticipant() {
		return participant;
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
