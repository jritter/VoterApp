package ch.bfh.evoting.voterapp.cgs97.network;

import java.util.Map;

import ch.bfh.evoting.voterapp.cgs97.entities.Participant;
import ch.bfh.evoting.voterapp.cgs97.entities.VoteMessage;

/**
 * Interface to the network component
 * @author Philémon von Bergen
 *
 */
public interface NetworkInterface {
		
	/**
	 * Join a group
	 * @param groupName name of the group to join
	 */
	public void joinGroup(String groupName);
		
	/**
	 * Get the name of the network in which the group exists
	 * @return the name of the network in which the group exists
	 */
	public String getNetworkName();

	/**
	 * Get the name of the group
	 * @return the name of the group
	 */
	public String getGroupName();

	/**
	 * Get the password of the group
	 * @return the password of the group
	 */
	public String getGroupPassword();
	
	/**
	 * Get the truncated digest of the salt used to derive the symmetric encryption key encrypting the network communication
	 * @return the truncated digest of the salt
	 */
	public String getSaltShortDigest();
	
	/**
	 * Get the unique identifier of myself
	 * @return the unique identifier of myself
	 */
	public String getMyUniqueId();
	
	/**
	 * Get the participants in the group
	 * @return a map (<uniqueId, Participant Object>) of the participants in the group 
	 */
	public Map<String,Participant> getGroupParticipants();
	
	/**
	 * Set the name of the group to connect to
	 * @param groupName the name of the group to connect to
	 */
	public void setGroupName(String groupName);
	
	/**
	 * Set the password of the group to connect to
	 * @param password the password of the group to connect to
	 */
	public void setGroupPassword(String password);
	
	/**
	 * Lock the group
	 * Works only if the caller of this method is also the creator of the group
	 */
	public void lockGroup();
	
	/**
	 * Unlock the group
	 * Works only if the caller of this method is also the creator of the group
	 */
	public void unlockGroup();
	
	/**
	 * Leave the currently connected group and destroy it if the caller of this method is the creator of the group
	 */
	public void disconnect();
	
	/**
	 * This method can be used to send a broadcast message
	 * 
	 * @param votemessage The votemessage which should be sent
	 */
	public void sendMessage(VoteMessage votemessage);
	
	/**
	 * This method signature can be used to send unicast message to a specific ip address
	 * 
	 * 
	 * @param votemessage The votemessage which should be sent
	 * @param destinationUniqueId The destination of the message
	 */
	public void sendMessage(VoteMessage votemessage, String destinationUniqueId);		
}

