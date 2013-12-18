package ch.bfh.evoting.voterapp.util.xml;


public class XMLParticipant {

	private String identification;
	private String uniqueId;

	private String ai = null;
	private String proofForXi = null;
	private String hi = null;
	private String bi = null;
	private String proofValidVote = null;
	private String hiHat = null;
	private String hiHatPowXi = null;
	private String proofForHiHat = null;
	
	public XMLParticipant(){
		
	}
	
	public XMLParticipant(String identification, String uniqueId, String ai,
			String proofForXi, String hi, String bi, String proofValidVote,
			String hiHat, String hiHatPowXi, String proofForHiHat) {
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
	public String getAi() {
		return ai;
	}
	public void setAi(String ai) {
		this.ai = ai;
	}
	public String getProofForXi() {
		return proofForXi;
	}
	public void setProofForXi(String proofForXi) {
		this.proofForXi = proofForXi;
	}
	public String getHi() {
		return hi;
	}
	public void setHi(String hi) {
		this.hi = hi;
	}
	public String getBi() {
		return bi;
	}
	public void setBi(String bi) {
		this.bi = bi;
	}
	public String getProofValidVote() {
		return proofValidVote;
	}
	public void setProofValidVote(String proofValidVote) {
		this.proofValidVote = proofValidVote;
	}
	public String getHiHat() {
		return hiHat;
	}
	public void setHiHat(String hiHat) {
		this.hiHat = hiHat;
	}
	public String getHiHatPowXi() {
		return hiHatPowXi;
	}
	public void setHiHatPowXi(String hiHatPowXi) {
		this.hiHatPowXi = hiHatPowXi;
	}
	public String getProofForHiHat() {
		return proofForHiHat;
	}
	public void setProofForHiHat(String proofForHiHat) {
		this.proofForHiHat = proofForHiHat;
	}
	
	
}
