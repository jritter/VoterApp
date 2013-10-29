package ch.bfh.evoting.voterapp.network;

import java.util.Map;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage;

/**
 * Interface to the network component
 * @author Philémon von Bergen
 *
 */
public interface NetworkInterface {
		
	public void joinGroup(String groupName);
		
	public String getNetworkName();

	public String getGroupName();

	public String getGroupPassword();
	
	public String getMyIpAddress();
	
	public Map<String,Participant> getConversationParticipants();
	
	public void setGroupName(String groupName);
	
	public void setGroupPassword(String password);
	
	public void lockGroup();
	
	public void unlockGroup();
	
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
	 * @param destinationIPAddress The destination of the message
	 */
	public void sendMessage(VoteMessage votemessage, String destinationIPAddress);	

	public void disconnect();

//	public void setSaltShortDigest(String saltShortDigest);

	public String getSaltShortDigest();
	
}

