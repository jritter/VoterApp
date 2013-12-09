package ch.bfh.evoting.voterapp.protocol.hkrs12;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

public class ProtocolParticipant extends Participant {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int protocolParticipantIndex;
	
	//transient fields are not serialized
	//TODO exclude secret when sending poll over network
	private Element xi = null;
	private Element ai = null;
	private Element proofForXi = null;
	private Element hi = null;
	private Element bi = null;
	private Element proofValidVote = null;
	private Element hiHat = null;
	private Element hiHatPowXi = null;
	private Element proofForHiHat = null;

	/**
	 * Create a ProtocolParticipant by giving a Participant object
	 * @param p the participant object to use in this ProtocolParticipant
	 */
	public ProtocolParticipant(Participant p){
		super(p.getIdentification(), p.getUniqueId(), p.isSelected(), p.hasVoted(), p.hasAcceptedReview());
	}
	
	/**
	 * Get the index of the participant in the protocol
	 * @return identification integer
	 */
	public int getProtocolParticipantIndex() {
		return protocolParticipantIndex;
	}

	/**
	 * Set the index of the participant in the protocol
	 * @param protocolParticipantIndex the index of the participant in the protocol
	 */
	public void setProtocolParticipantIndex(int protocolParticipantIndex) {
		this.protocolParticipantIndex = protocolParticipantIndex;
	}

	/**
	 * Get value x of the protocol for this participant
	 * @return value x of the protocol for this participant
	 */
	public Element getXi() {
		return xi;
	}

	/**
	 * Set value x of the protocol for this participant
	 * @param xi value x of the protocol for this participant
	 */
	public void setXi(Element xi) {
		this.xi = xi;
	}
	
	/**
	 * Get value a of the protocol for this participant
	 * @return value a of the protocol for this participant
	 */
	public Element getAi() {
		return ai;
	}

	/**
	 * Set value a of the protocol for this participant
	 * @param ai value a of the protocol for this participant
	 */
	public void setAi(Element ai) {
		this.ai = ai;
	}

	/**
	 * Get the ZK proof for knowledge of x
	 * @return the ZK proof for knowledge of x
	 */
	public Element getProofForXi() {
		return proofForXi;
	}

	/**
	 * Set the ZK proof for knowledge of x
	 * @param proofForXi the ZK proof for knowledge of x
	 */
	public void setProofForXi(Element proofForXi) {
		this.proofForXi = proofForXi;
	}

	/**
	 * Get value h of the protocol for this participant
	 * @return value h of the protocol for this participant
	 */
	public Element getHi() {
		return hi;
	}

	/**
	 * Set value h of the protocol for this participant
	 * @param hi value h of the protocol for this participant
	 */
	public void setHi(Element hi) {
		this.hi = hi;
	}

	/**
	 * Get value b of the protocol for this participant
	 * @return value b of the protocol for this participant
	 */
	public Element getBi() {
		return bi;
	}

	/**
	 * Set value b of the protocol for this participant
	 * @param bi value b of the protocol for this participant
	 */
	public void setBi(Element bi) {
		this.bi = bi;
	}

	/**
	 * Get the validity proof for the vote
	 * @return the validity proof for the vote
	 */
	public Element getProofValidVote() {
		return proofValidVote;
	}

	/**
	 * Set the validity proof for the vote
	 * @param proofValidVote the validity proof for the vote
	 */
	public void setProofValidVote(Element proofValidVote) {
		this.proofValidVote = proofValidVote;
	}
	
	/**
	 * Get value h hat of the protocol for this participant
	 * @return value h hat of the protocol for this participant
	 */
	public Element getHiHat() {
		return hiHat;
	}

	/**
	 * Set value h hat of the protocol for this participant
	 * @param hiHat value h hat of the protocol for this participant
	 */
	public void setHiHat(Element hiHat) {
		this.hiHat = hiHat;
	}

	/**
	 * Get value (h hat)^x of the protocol for this participant
	 * @return value (h hat)^x of the protocol for this participant
	 */
	public Element getHiHatPowXi() {
		return hiHatPowXi;
	}

	/**
	 * Set value (h hat)^x of the protocol for this participant
	 * @param hiHatPowXi value (h hat)^x of the protocol for this participant
	 */
	public void setHiHatPowXi(Element hiHatPowXi) {
		this.hiHatPowXi = hiHatPowXi;
	}

	/**
	 * Get the Equality between logs ZK proof 
	 * @return the Equality between logs ZK proof 
	 */
	public Element getProofForHiHat() {
		return proofForHiHat;
	}

	/**
	 * Set the Equality between logs ZK proof
	 * @param proofForHiHat the Equality between logs ZK proof 
	 */
	public void setProofForHiHat(Element proofForHiHat) {
		this.proofForHiHat = proofForHiHat;
	}

//	@Override
//	public String toString() {
//		String s = "Participant object\n";
//		s+="\tIdentification: "+s+"\n";
//		s+="\tIP Address: "+this.ipAddress+"\n";
//		s+="\tState at this moment: "+this.state+"\n";
//		s+="\tProtocol participant index: "+this.protocolParticipantIndex+"\n";
//		if(this.xi!=null){
//			s+="\txi: "+((AtomicElement)this.xi).getBigInteger()+"\n";
//		} else {
//			s+="\txi: "+this.xi+"\n";
//		}
//		if(this.ai!=null){
//			s+="\tai: "+((AtomicElement)this.ai).getBigInteger()+"\n";
//		} else {
//			s+="\tai: "+this.ai+"\n";
//		}
//		if(this.bi!=null){
//			s+="\tbi: "+((AtomicElement)this.bi).getBigInteger()+"\n";
//		} else {
//			s+="\tbi: "+this.bi+"\n";
//		}
//		if(this.hi!=null){
//			s+="\thi: "+((AtomicElement)this.hi).getBigInteger()+"\n";
//		} else {
//			s+="\thi: "+this.hi+"\n";
//		}
//		if(this.hiHat!=null){
//			s+="\thi hat: "+((AtomicElement)this.hiHat).getBigInteger()+"\n";
//		} else {
//			s+="\thi hat: "+this.hiHat+"\n";
//		}
//		if(this.hiHatPowXi!=null){
//			s+="\thi hat pow xi: "+((AtomicElement)this.hiHatPowXi).getBigInteger()+"\n";
//		} else {
//			s+="\thi hat pow xi: "+this.hiHatPowXi+"\n";
//		}
//		if(this.proofForXi!=null){
//			s+="\tProof for xi: "+this.proofForXi+"\n";
//		} else {
//			s+="\tProof for xi: "+this.proofForXi+"\n";
//		}
//		if(this.proofValidVote!=null){
//			s+="\tProof of valid vote: "+this.proofValidVote+"\n";
//		} else {
//			s+="\tProof of valid vote: "+this.proofValidVote+"\n";
//		}
//		if(this.proofValidVote!=null){
//			s+="\tProof for hi hat pow xi : "+this.proofForHiHat+"\n";
//		} else {
//			s+="\tProof for hi hat pow xi : "+this.proofForHiHat+"\n";
//		}
//		return s;
//	}
	
	@Override
	public int hashCode() {
		//TODO
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		//TODO
		return super.equals(obj);
	}

}
