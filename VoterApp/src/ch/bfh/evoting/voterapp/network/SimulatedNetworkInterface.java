/*
 * This class was commented out because is no more used since we adopted AllJoyn as network layer
 * We keep it 
 */

//package ch.bfh.evoting.voterapp.network;
//
//import java.util.Map;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
//import ch.bfh.evoting.voterapp.entities.Participant;
//import ch.bfh.evoting.voterapp.entities.VoteMessage;
//import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
//import ch.bfh.evoting.voterapp.util.NetworkSimulator;
////public class SimulatedNetworkInterface extends AbstractNetworkInterface{
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
//	@Override
//	public String getConversationPassword() {
//		return "1234";
//	}
//
//	@Override
//	public Map<String,Participant> getConversationParticipants(){
//		return ns.createDummyParticipants();
//	}
//
//	/**
//	 * This method can be used to send a broadcast message
//	 * 
//	 * @param votemessage The votemessage which should be sent
//	 * @param sender The origin of the message
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
//	 * @param sender The origin of the message
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
//	@Override
//	public String getMyIpAddress() {
//
//		String packageName = context.getPackageName();
//		if(packageName.equals("ch.bfh.evoting.voterapp")){
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
//	/**
//	 * this broadcast receiver listens for incoming instacircle messages
//	 */
//	private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			handleReceivedMessage((Message) intent.getSerializableExtra("message"));
//		}
//	};
//	
//	/**
//	 * This method acts as a wrapper between instacircle messages and vote
//	 * messages by extracting the vote message and rebroadcast the notification.
//	 * 
//	 * @param message
//	 */
//	private void handleReceivedMessage(Message message) {
//		if(message.getMessageType()==Message.MSG_CONTENT){
//			// Extract the votemessage out of the message
//			VoteMessage voteMessage = (VoteMessage) su.deserialize(message.getMessage());
//			if(voteMessage==null) return;
//			this.transmitReceivedMessage(voteMessage);
//		}
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
//	public void joinNetwork(String networkName) {
//		//Not needed
//	}
//
//
//
//}
