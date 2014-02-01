package ch.bfh.evoting.voterapp.cgs97.network;
/*
 * This network interface was used at the beginning of the project to simulate the network
 * It is no more needed but we keep it for development reasons
 */
//package ch.bfh.evoting.voterapp.cgs97.network;
//
//import java.util.Map;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
//import ch.bfh.evoting.voterapp.cgs97.entities.Participant;
//import ch.bfh.evoting.voterapp.cgs97.entities.VoteMessage;
//import ch.bfh.evoting.voterapp.cgs97.util.BroadcastIntentTypes;
//import ch.bfh.evoting.voterapp.cgs97.util.NetworkSimulator;
//
//public class SimulatedNetworkInterface extends AbstractNetworkInterface{
//
//	private NetworkSimulator ns;
//
//	public SimulatedNetworkInterface (Context context) {
//		super(context);
//		ns = new NetworkSimulator(context);
//
//		// Listening for arriving messages
//		LocalBroadcastManager.getInstance(context).registerReceiver(
//				messageReceiver, new IntentFilter("messageArrived"));
//
//		// Subscribing to the participantJoined and participantChangedState events
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("participantJoined");
//		intentFilter.addAction("participantChangedState");
//		LocalBroadcastManager.getInstance(context).registerReceiver(participantsDiscoverer, intentFilter);
//	}
//
//
//	@Override
//	public String getNetworkName() {
//		return "Simulated network";
//	}
//
//	
//	/**
//	 * This method can be used to send a broadcast message
//	 * 
//	 * @param votemessage The votemessage which should be sent
//	 */
//	@Override
//	public void sendMessage(VoteMessage votemessage){
//		//Not needed. As this is a simulation, no real message will be sent
//	}
//
//	/**
//	 * This method signature can be used to send unicast message to a specific ip address
//	 * 
//	 * 
//	 * @param votemessage The votemessage which should be sent
//	 * @param destinationUniqueId The destination of the message
//	 */
//	@Override
//	public void sendMessage(VoteMessage votemessage, String destinationUniqueId){
//		//Not needed. As this is a simulation, no real message will be sent
//	}
//	
//	@Override
//	public void disconnect(){
//		//Not needed. As no real connection was needed
//	}
//
//	
//	/**
//	 * this broadcast receiver listens for incoming instacircle messages
//	 */
//	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			handleReceivedMessage((VoteMessage) intent.getSerializableExtra("message"));
//		}
//	};
//	
//	/**
//	 * This method acts as a wrapper between instacircle messages and vote
//	 * messages by extracting the vote message and rebroadcast the notification.
//	 * 
//	 * @param message
//	 */
//	private void handleReceivedMessage(VoteMessage voteMessage) {
//			// Extract the votemessage out of the message
//			if(voteMessage==null) return;
//			this.transmitReceivedMessage(voteMessage);
//	}
//
//	/**
//	 * this broadcast receiver listens for incoming instacircle broadcast notifying set of participants has changed
//	 */
//	private BroadcastReceiver participantsDiscoverer = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Intent participantsUpdate = new Intent(BroadcastIntentTypes.participantStateUpdate);
//			LocalBroadcastManager.getInstance(context).sendBroadcast(participantsUpdate);
//		}
//	};
//
//	@Override
//	public void joinGroup(String groupName) {
//		//Not needed
//	}
//
//
//	@Override
//	public String getGroupName() {
//		return "group1";
//	}
//
//
//	@Override
//	public String getGroupPassword() {
//		return "1234";
//	}
//
//
//	@Override
//	public String getSaltShortDigest() {
//		return "abc";
//	}
//
//
//	@Override
//	public String getMyUniqueId() {
//		String packageName = context.getPackageName();
//		if(packageName.equals("ch.bfh.evoting.voterapp.cgs97")){
//			int higher = 15;
//			int lower = 2;
//			int random = (int)(Math.random() * (higher-lower)) + lower;
//			return "192.168.1."+random;
//		} else if (packageName.equals("ch.bfh.evoting.adminapp")){
//			return "192.168.1.1";
//		} else {
//			return "";
//		}
//	}
//
//
//	@Override
//	public Map<String, Participant> getGroupParticipants() {
//		return ns.createDummyParticipants();
//	}
//
//
//	@Override
//	public void setGroupName(String groupName) {
//		//Not needed		
//	}
//
//
//	@Override
//	public void setGroupPassword(String password) {
//		//Not needed		
//	}
//
//
//	@Override
//	public void lockGroup() {
//		//Not needed		
//	}
//
//
//	@Override
//	public void unlockGroup() {
//		//Not needed		
//	}
//
//
//
//}
