package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97;

import ch.bfh.evoting.voterapp.cgs97.entities.Poll;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolPoll extends Poll {
	
	
	private GStarModElement publicKey;
	private int threshold;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ProtocolPoll(Poll poll){
        super(poll.getId(), poll.getQuestion(), poll.getStartTime(), poll.getOptions(), poll.getParticipants(), poll.isTerminated());
        super.setNumberOfParticipants(poll.getNumberOfParticipants());
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

	@Override
	public String toString() {
		return "ProtocolPoll [publicKey=" + publicKey + ", threshold="
				+ threshold + ", question=" + getQuestion()
				+ ", options=" + getOptions() + "]";
	}
	
}
