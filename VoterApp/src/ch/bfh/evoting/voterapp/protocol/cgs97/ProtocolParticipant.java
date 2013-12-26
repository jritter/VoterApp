package ch.bfh.evoting.voterapp.protocol.cgs97;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolParticipant extends Participant {

	private GStarModElement[] coefficientCommitments;
	private GStarModElement partDecryption;
	private ZModElement keyShareFrom;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a ProtocolParticipant by giving a Participant object
	 * @param p the participant object to use in this ProtocolParticipant
	 */
	public ProtocolParticipant(Participant p){
		super(p.getIdentification(), p.getUniqueId(), p.isSelected(), p.hasVoted(), p.hasAcceptedReview());
	}

	public GStarModElement[] getCoefficientCommitments() {
		return coefficientCommitments;
	}

	public void setCoefficientCommitments(GStarModElement[] coefficientCommitments) {
		this.coefficientCommitments = coefficientCommitments;
	}

	public ZModElement getKeyShareFrom() {
		return keyShareFrom;
	}

	public void setKeyShareFrom(ZModElement keyShareFrom) {
		this.keyShareFrom = keyShareFrom;
	}

	public void setPartDecryption(GStarModElement partDecryption) {
		this.partDecryption = partDecryption;
	}
	
	public GStarModElement getPartDecryption() {
		return partDecryption;
	}
}
