package ch.bfh.evoting.voterapp.entities;

import java.io.Serializable;


/**
 * Class representing a vote message
 *
 */
public class VoteMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Type messageType;
	private String senderIPAddress;
	private Serializable messageContent;
	private long timestamp;
	
	public VoteMessage(){}
	
	public VoteMessage(Type messageType, Serializable messageContent){
		this.messageType = messageType;
		this.messageContent = messageContent;
	}
	
	public VoteMessage(Type messageType, Serializable messageContent, String senderIPAddress, long timestamp){
		this.messageType = messageType;
		this.messageContent = messageContent;
		this.senderIPAddress = senderIPAddress;
		this.timestamp = timestamp;
	}
	
	public String getSenderIPAddress () {
		return senderIPAddress;		
	}
	
	public void setSenderIPAdress(String senderIPAdress) {
		this.senderIPAddress = senderIPAdress;
	}
		
	public long getTimestamp(){
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Serializable getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(Serializable messageContent) {
		this.messageContent = messageContent;
	}

	public Type getMessageType() {
		return messageType;
	}
	
	public void setMessageType(Type messageType) {
		this.messageType = messageType;
	}
	
	public enum Type {
		VOTE_MESSAGE_ELECTORATE,
		VOTE_MESSAGE_POLL_TO_REVIEW,
		VOTE_MESSAGE_ACCEPT_REVIEW,
		VOTE_MESSAGE_VOTE,
		VOTE_MESSAGE_START_POLL,
		VOTE_MESSAGE_STOP_POLL;
	}
}
