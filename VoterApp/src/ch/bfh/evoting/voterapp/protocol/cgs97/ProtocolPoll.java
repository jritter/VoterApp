package ch.bfh.evoting.voterapp.protocol.cgs97;

import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolPoll extends Poll {
	
	
	private GStarModElement publicKey;

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
}
