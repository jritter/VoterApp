package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;

public class XMLParticipant {

	@Element
	private String identification;
	@Element
	private String uniqueId;

	@ElementList
	private List<XMLGqElement> coefficientCommitments;

	@Element
	private XMLGqElement keyShareCommitment;

	@Element
	private XMLPartDecryption partDecryption;

	@Element
	private XMLBallot ballot;

	public XMLParticipant(String identification, String uniqueId,
			List<XMLGqElement> coefficientCommitments,
			XMLGqElement keyShareCommitment, XMLPartDecryption partDecryption,
			XMLBallot ballot) {
		
		super();
		this.identification = identification;
		this.uniqueId = uniqueId;
		this.coefficientCommitments = coefficientCommitments;
		this.keyShareCommitment = keyShareCommitment;
		this.partDecryption = partDecryption;
		this.ballot = ballot;
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

	public List<XMLGqElement> getCoefficientCommitments() {
		return coefficientCommitments;
	}

	public void setCoefficientCommitments(
			List<XMLGqElement> coefficientCommitments) {
		this.coefficientCommitments = coefficientCommitments;
	}

	public XMLGqElement getKeyShareCommitment() {
		return keyShareCommitment;
	}

	public void setKeyShareCommitment(XMLGqElement keyShareCommitment) {
		this.keyShareCommitment = keyShareCommitment;
	}

	public XMLPartDecryption getPartDecryption() {
		return partDecryption;
	}

	public void setPartDecryption(XMLPartDecryption partDecryption) {
		this.partDecryption = partDecryption;
	}

	public XMLBallot getBallot() {
		return ballot;
	}

	public void setBallot(XMLBallot ballot) {
		this.ballot = ballot;
	}

}
