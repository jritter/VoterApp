package ch.bfh.evoting.voterapp.entities;

import java.io.Serializable;


/**
 * Class representing a vote message
 *
 */
public class VoteMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Type messageType;
	private String senderUniqueId;
	private Serializable messageContent;
	
	/**
	 * Construct an empty message object
	 */
	public VoteMessage(){}
	
	/**
	 * Construct a message object
	 * @param messageType type of the message's content
	 * @param messageContent content of the message
	 */
	public VoteMessage(Type messageType, Serializable messageContent){
		this.messageType = messageType;
		this.messageContent = messageContent;
	}
	
	/**
	 * Get the sender unique identifier 
	 * @return the unique identifier of the sender of this message
	 */
	public String getSenderUniqueId () {
		return senderUniqueId;		
	}
	
	/**
	 * Set the sender unique identifier
	 * @param senderUniqueId the unique identifier of the sender of this message
	 */
	public void setSenderUniqueId(String senderUniqueId) {
		this.senderUniqueId = senderUniqueId;
	}

	/**
	 * Get the content of the message
	 * @return the content of the message
	 */
	public Serializable getMessageContent() {
		return messageContent;
	}

	/**
	 * Set the content of the message
	 * @param messageContent the content of the message
	 */
	public void setMessageContent(Serializable messageContent) {
		this.messageContent = messageContent;
	}

	/**
	 * Get the type of the message
	 * @return the type of the message
	 */
	public Type getMessageType() {
		return messageType;
	}
	
	/**
	 * Set the type of the message
	 * @param messageType the type of the message
	 */
	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Type of vote messages
	 *
	 */
	public enum Type {
		VOTE_MESSAGE_ELECTORATE,
		VOTE_MESSAGE_POLL_TO_REVIEW,
		VOTE_MESSAGE_ACCEPT_REVIEW,
		VOTE_MESSAGE_VOTE,
		VOTE_MESSAGE_START_POLL,
		VOTE_MESSAGE_STOP_POLL,
		VOTE_MESSAGE_PRINT,
		VOTE_MESSAGE_CANCEL_POLL,
		VOTE_MESSAGE_COEFFICIENT_COMMITMENT,
		VOTE_MESSAGE_KEY_SHARE,
		VOTE_MESSAGE_PART_DECRYPTION;
	}
}
