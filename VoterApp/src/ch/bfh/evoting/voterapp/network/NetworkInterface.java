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
		
	public void joinNetwork(String networkName);
		
	public String getNetworkName();

	public String getConversationPassword();
	
	public String getMyIpAddress();
	
	public Map<String,Participant> getConversationParticipants();
	
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
}

