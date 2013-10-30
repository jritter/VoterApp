/*
 * This network interface is no more used since the AllJoyn is used as network layer
 * We keep it if that would change some day
 */
//package ch.bfh.evoting.voterapp.network;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.database.CursorIndexOutOfBoundsException;
//import android.net.DhcpInfo;
//import android.net.wifi.WifiManager;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import ch.bfh.evoting.instacirclelib.Message;
//import ch.bfh.evoting.instacirclelib.db.NetworkDbHelper;
//import ch.bfh.evoting.instacirclelib.service.NetworkService;
//import ch.bfh.evoting.voterapp.AndroidApplication;
//import ch.bfh.evoting.voterapp.entities.Participant;
//import ch.bfh.evoting.voterapp.entities.VoteMessage;
//import ch.bfh.evoting.voterapp.network.wifi.AdhocWifiManager;
//import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
//import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
//
//public class InstaCircleNetworkInterface extends AbstractNetworkInterface {
//
//
//	private static final String PREFS_NAME = "network_preferences";
//
//	private final NetworkDbHelper dbHelper;
//
//	public InstaCircleNetworkInterface (Context context) {
//		super(context);
//
//		dbHelper = NetworkDbHelper.getInstance(context);
//
//		// Listening for arriving messages
//		LocalBroadcastManager.getInstance(context).registerReceiver(
//				mMessageReceiver, new IntentFilter("messageArrived"));
//
//		// Subscribing to the participantJoined and participantChangedState events
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("participantJoined");
//		intentFilter.addAction("participantChangedState");
//		LocalBroadcastManager.getInstance(context).registerReceiver(participantsDiscoverer, intentFilter);
//		LocalBroadcastManager.getInstance(context).registerReceiver(serviceStopListener, new IntentFilter("NetworkServiceStopped"));
//		
//	}
//
//	@Override
//	public String getNetworkName() {
//		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
//		preferences = context.getSharedPreferences(PREFS_NAME, 0);
//		return preferences.getString("SSID", "");
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
//		votemessage.setSenderUniqueId(this.getMyIpAddress());
//		Message message = new Message(su.serialize(votemessage), Message.MSG_CONTENT, this.getMyIpAddress());
//		Intent intent = new Intent("messageSend");
//		intent.putExtra("message", message);
//		intent.putExtra("broadcast", true);
//		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
//		votemessage.setSenderUniqueId(this.getMyIpAddress());
//		Message message = new Message(su.serialize(votemessage), Message.MSG_CONTENT, this.getMyIpAddress());
//		Intent intent = new Intent("messageSend");
//		intent.putExtra("message", message);
//		intent.putExtra("ipAddress", destinationUniqueId);
//		intent.putExtra("broadcast", false);
//		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//	}
//
//	/**
//	 * this broadcast receiver listens for incoming instacircle messages
//	 */
//	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
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
//			Log.e("InstaCircleNetworkInterface", "Participant State Update received from IC");
//			Intent participantsUpdate = new Intent(BroadcastIntentTypes.participantStateUpdate);
//			LocalBroadcastManager.getInstance(context).sendBroadcast(participantsUpdate);
//		}
//	};
//	
//	/**
//	 * this broadcast receiver listens for incoming instacircle broadcast notifying that network service has been stopped
//	 */
//	private BroadcastReceiver serviceStopListener = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			WifiManager wifiman = (WifiManager) AndroidApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
//			new AdhocWifiManager(wifiman)
//			.restoreWifiConfiguration(AndroidApplication.getInstance().getBaseContext());
//			WifiAPManager wifiAP = new WifiAPManager();
//			if (wifiAP.isWifiAPEnabled(wifiman)) {
//				wifiAP.disableHotspot(wifiman, AndroidApplication.getInstance().getBaseContext());
//			}
//		}
//	};
//
//	
//	/**
//	 * Get IP address from first non-localhost interface
//	 * @param ipv4  true=return ipv4, false=return ipv6
//	 * @return  address or empty string
//	 * @author http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
//	 */
//	private String getIPAddress(boolean useIPv4) {
//
//		WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
//		String ipString = null;
//
//		if(new WifiAPManager().isWifiAPEnabled(wifiManager)){
//
//			try{
//				for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
//						.hasMoreElements();) {
//					NetworkInterface intf = en.nextElement();
//					if (intf.getName().contains("wlan")) {
//						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
//								.hasMoreElements();) {
//							InetAddress inetAddress = enumIpAddr.nextElement();
//							if (!inetAddress.isLoopbackAddress()
//									&& (inetAddress.getAddress().length == 4)) {
//								ipString = inetAddress.getHostAddress();
//								break;
//							}
//						}
//					}
//				}
//			} catch (SocketException e){
//				e.printStackTrace();
//			}
//		} else {
//
//			WifiManager wifi = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
//			DhcpInfo dhcp = wifi.getDhcpInfo();
//
//			InetAddress found_ip_address = null;
//			int ip = dhcp.ipAddress;
//			byte[] quads = new byte[4];
//			for (int k = 0; k < 4; k++)
//				quads[k] = (byte) (ip >> (k * 8));
//			try {
//				found_ip_address =  InetAddress.getByAddress(quads);
//				ipString = found_ip_address.getHostAddress();
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return ipString;
//		
//	}
//	
//		@Override
//	public void joinGroup(String groupName) {
//		Intent intent = new Intent(context, NetworkService.class);
//
//		context.stopService(intent);
//		context.startService(intent);		
//	}
//
//	@Override
//	public String getGroupName() {
//		return null;
//	}
//
//	@Override
//	public String getGroupPassword() {
//		try{
//			return dbHelper.getCipherKey();
//		} catch (CursorIndexOutOfBoundsException e){
//			return null;
//		}
//	}
//
//	@Override
//	public String getSaltShortDigest() {
//		return null;
//	}
//
//	@Override
//	public String getMyUniqueId() {
//		return getIPAddress(true);
//	}
//
//	@Override
//	public Map<String, Participant> getGroupParticipants() {
//		ArrayList<Participant> participants = new ArrayList<Participant>(); 
//
//		Cursor c = dbHelper.queryParticipants();
//
//		if (c != null){
//			while(c.moveToNext()){
//				Participant p = new Participant(c.getString(c.getColumnIndex("identification")), c.getString(c.getColumnIndex("ip_address")), false, false);
//				participants.add(p);
//			}
//		}
//		c.close();
//
//		Map<String,Participant> map = new HashMap<String,Participant>();
//		for(Participant p:participants){
//			map.put(p.getUniqueId(), p);
//		}
//
//		return map;
//	}
//
//	@Override
//	public void setGroupName(String groupName) {
//		
//	}
//
//	@Override
//	public void setGroupPassword(String password) {
//		
//	}
//
//	@Override
//	public void lockGroup() {
//		
//	}
//
//	@Override
//	public void unlockGroup() {
//		
//	}
//	
//	@Override
//	public void disconnect(){
//		this.dbHelper.closeConversation();
//	}
//}
