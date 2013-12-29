package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.util.List;

import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolPoll extends Poll {
	
	
	private GStarModElement publicKey;
	private List<ProtocolBallot> ballots;
	private int threshold;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ProtocolPoll(Poll poll){
        super(poll.getId(), poll.getQuestion(), poll.getStartTime(), poll.getOptions(), poll.getParticipants(), poll.isTerminated());
	}

	public GStarModElement getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(GStarModElement privateKey) {
		this.publicKey = privateKey;
	}
	
	public void setThreshold(int threshold){
		if (threshold < 1 || threshold > this.getParticipants().size()){
			throw new IllegalArgumentException();
		}
		this.threshold = threshold;
	}
	
	public int getThreshold() {
		return threshold;
	}
}
