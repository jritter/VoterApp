package ch.bfh.evoting.voterapp.entities;

import java.io.Serializable;

/**
 * Class representing a participant to the poll
 * @author Philémon von Bergen
 *
 */
public class Participant implements Serializable{

	private static final long serialVersionUID = 1L;
	private String identification;
	private String uniqueId;
	private boolean hasVoted;
	private boolean isSelected;
	private boolean hasAcceptedReview;
	
	/**
	 * Construct a Participant object
	 * @param identification the identification of the participant
	 * @param uniqueId the IP address of the participant
	 * @param isSelected indicate if the participant in the network is selected as member of the electorate
	 * @param hasVoted indicate if the participant already has submitted a vote
	 * @param hasAcceptedReview indicate if the participant already has accepted the review
	 */
	public Participant(String identification, String uniqueId, boolean isSelected, boolean hasVoted, boolean hasAcceptedReview){
		this.identification = identification;
		this.uniqueId = uniqueId;
		this.hasVoted = hasVoted;
		this.isSelected = isSelected;
		this.hasAcceptedReview = hasAcceptedReview;
	}
	
	/**
	 * Get the identification the identification of the participant
	 * @return the identification the identification of the participant
	 */
	public String getIdentification() {
		return identification;
	}

	/**
	 * Set the identification the identification of the participant
	 * @param identification the identification the identification of the participant
	 */
	public void setIdentification(String identification) {
		this.identification = identification;
	}

	/**
	 * Get the unique identifier of the participant
	 * @return the unique identifier of the participant
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * Set the IP address of the participant
	 * @param uniqueId the unique identifier of the participant
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	/**
	 * Indicates if the participant has already cast her ballot
	 * @return true if casted
	 */
	public boolean hasVoted() {
		return hasVoted;
	}

	/**
	 * Set if the participant has already cast her ballot
	 * @param hasVoted true if casted
	 */
	public void setHasVoted(boolean hasVoted) {
		this.hasVoted = hasVoted;
	}

	/**
	 * Indicate if the participant in the network is selected as member of the electorate
	 * @return true if she belongs to the electorate
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Set if the participant in the network is selected as member of the electorate
	 * @param isSelected true if she belongs to the electorate
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	/**
	 * Indicate if the participant has accepted the review of the poll
	 * @return true if participant has accepted, else otherwise
	 */
	public boolean hasAcceptedReview() {
		return hasAcceptedReview;
	}

	/**
	 * Set the flag indicating if the participant has accepted the review of the poll
	 * @param hasAcceptedReview true if participant has accepted, else otherwise
	 */
	public void setHasAcceptedReview(boolean hasAcceptedReview) {
		this.hasAcceptedReview = hasAcceptedReview;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identification == null) ? 0 : identification.hashCode());
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Participant other = (Participant) obj;
		if (identification == null) {
			if (other.identification != null)
				return false;
		} else if (!identification.equals(other.identification))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}

	
}
