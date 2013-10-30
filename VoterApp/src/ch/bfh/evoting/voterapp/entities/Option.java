package ch.bfh.evoting.voterapp.entities;

import java.io.Serializable;

/**
 * Class representing an option that can be chosen for a poll
 * @author Philémon von Bergen
 *
 */
public class Option implements Serializable {

	private static final long serialVersionUID = 1L;
	private String text;
	private int votes;
	private int pollId;
	private int id;
	private double percentage;
	
	/**
	 * Create an empty Option object
	 */
	public Option(){}
	
	/**
	 * Constructs an Option object
	 * @param text text of the option
	 * @param votes number of votes this option has received
	 * @param percentage percentage of votes this option received
	 * @param id id in the database
	 * @param pollId id of the poll to whom it belongs
	 */
	public Option(String text, int votes, double percentage, int id, int pollId){
		this.text = text;
		this.votes = votes;
		this.id = id;
		this.pollId = pollId;
		this.percentage = percentage;
	}
	
	/**
	 * Get the text of the option
	 * @return the text of the option
	 */
	public String getText() {
		return text;
	}

	/**
	 * Set text of the option
	 * @param text the text of the option
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Get the number of votes this option has received
	 * @return the number of votes this option has received
	 */
	public int getVotes() {
		return votes;
	}

	/**
	 * Set the number of votes this option has received
	 * @param votes the number of votes this option has received
	 */
	public void setVotes(int votes) {
		this.votes = votes;
	}
	
	/**
	 * Get the percentage of votes received in comparison of the total number of votes
	 * @return the percentage of votes received in comparison of the total number of votes (in the form 50,6)
	 */
	public double getPercentage() {
		return percentage;
	}

	/**
	 * Set the percentage of votes received in comparison of the total number of votes
	 * @param percentage the percentage of votes received in comparison of the total number of votes (in the form 50,6)
	 */
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
	
	/**
	 * Get the id of the poll to whom this option belongs
	 * @return id of the poll to whom it belongs
	 */
	public int getPollId() {
		return pollId;
	}

	/**
	 * Set the id of the poll to whom this options belongs
	 * @param pollId the id of the poll to whom it belongs
	 */
	public void setPollId(int pollId) {
		this.pollId = pollId;
	}

	/**
	 * Get the id in the database
	 * @return the id in the database
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set the id in the database
	 * @param id the id of this option in the database
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		Option other = (Option) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
}
