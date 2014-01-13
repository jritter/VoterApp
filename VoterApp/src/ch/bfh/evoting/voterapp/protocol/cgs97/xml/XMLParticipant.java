package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

public class XMLParticipant {

	private String identification;
	private String uniqueId;

	private XMLGqElement ai = null;
	private XMLKnowledgeProof proofForXi = null;
	private XMLGqElement hi = null;
	private XMLGqElement bi = null;
	private XMLValidityProof proofValidVote = null;
	private XMLGqElement hiHat = null;
	private XMLGqElement hiHatPowXi = null;
	private XMLEqualityProof proofForHiHat = null;
	private int protocolParticipantIndex;

	public XMLParticipant() {

	}

	public XMLParticipant(String identification, String uniqueId,
			XMLGqElement ai, XMLKnowledgeProof proofForXi, XMLGqElement hi,
			XMLGqElement bi, XMLValidityProof proofValidVote,
			XMLGqElement hiHat, XMLGqElement hiHatPowXi,
			XMLEqualityProof proofForHiHat, int protocolParticipantIndex) {
		super();
		this.identification = identification;
		this.uniqueId = uniqueId;
		this.ai = ai;
		this.proofForXi = proofForXi;
		this.hi = hi;
		this.bi = bi;
		this.proofValidVote = proofValidVote;
		this.hiHat = hiHat;
		this.hiHatPowXi = hiHatPowXi;
		this.proofForHiHat = proofForHiHat;
		this.protocolParticipantIndex = protocolParticipantIndex;
	}

	public String getIdentification() {
		return identification;
	}

	public void setIdentification(String identification) {
		this.identification = identification;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public XMLGqElement getAi() {
		return ai;
	}

	public void setAi(XMLGqElement ai) {
		this.ai = ai;
	}

	public XMLKnowledgeProof getProofForXi() {
		return proofForXi;
	}

	public void setProofForXi(XMLKnowledgeProof proofForXi) {
		this.proofForXi = proofForXi;
	}

	public XMLGqElement getHi() {
		return hi;
	}

	public void setHi(XMLGqElement hi) {
		this.hi = hi;
	}

	public XMLGqElement getBi() {
		return bi;
	}

	public void setBi(XMLGqElement bi) {
		this.bi = bi;
	}

	public XMLValidityProof getProofValidVote() {
		return proofValidVote;
	}

	public void setProofValidVote(XMLValidityProof proofValidVote) {
		this.proofValidVote = proofValidVote;
	}

	public XMLGqElement getHiHat() {
		return hiHat;
	}

	public void setHiHat(XMLGqElement hiHat) {
		this.hiHat = hiHat;
	}

	public XMLGqElement getHiHatPowXi() {
		return hiHatPowXi;
	}

	public void setHiHatPowXi(XMLGqElement hiHatPowXi) {
		this.hiHatPowXi = hiHatPowXi;
	}

	public XMLEqualityProof getProofForHiHat() {
		return proofForHiHat;
	}

	public void setProofForHiHat(XMLEqualityProof proofForHiHat) {
		this.proofForHiHat = proofForHiHat;
	}

	public int getProtocolParticipantIndex() {
		return protocolParticipantIndex;
	}

	public void setProtocolParticipantIndex(int protocolParticipantIndex) {
		this.protocolParticipantIndex = protocolParticipantIndex;
	}
}
