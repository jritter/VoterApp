package ch.bfh.evoting.voterapp.network;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;

import ch.bfh.evoting.alljoyn.BusHandler;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.SerializationUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

public class AllJoynNetworkInterface extends AbstractNetworkInterface{

	private BusHandler mBusHandler;
	private String groupName;
	private String groupPassword;
	private String saltShortDigest;

	public AllJoynNetworkInterface(Context context) {
		super(context);
		HandlerThread busThread = new HandlerThread("BusHandler");
		busThread.start();
		mBusHandler = new BusHandler(busThread.getLooper(), AndroidApplication.getInstance().getApplicationContext());

		// Listening for arriving messages
		LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("messageArrived"));
		// Listening for group destroy signal
		LocalBroadcastManager.getInstance(context).registerReceiver(mGroupEventReceiver, new IntentFilter("groupDestroyed"));
		// Listening for group destroy signal
		LocalBroadcastManager.getInstance(context).registerReceiver(mNetworkConectionFailedReceiver, new IntentFilter("networkConnectionFailed"));

	}

	@Override
	public String getNetworkName() {
		SharedPreferences preferences = context.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
		return preferences.getString("SSID", "");
	}

	@Override
	public String getGroupName() {
		return groupName;
	}

	@Override
	public String getGroupPassword() {
		//		SharedPreferences preferences = context.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
		//		return preferences.getString("group_password", "");
		return this.groupPassword;
	}

	@Override
	public String getMyIpAddress() {
		return mBusHandler.getIdentification();
	}

	@Override
	public Map<String, Participant> getConversationParticipants() {
		TreeMap<String,Participant> parts = new TreeMap<String,Participant>();
		for(String s : mBusHandler.getParticipants(this.groupName)){
			String wellKnownName = s;
			if(mBusHandler.getPeerWellKnownName(s)!=null){
				wellKnownName = mBusHandler.getPeerWellKnownName(s);
			}
			parts.put(s, new Participant(wellKnownName, s, false, false));
		}
		return parts;
	}

	@Override
	public void sendMessage(VoteMessage votemessage) {
		votemessage.setSenderIPAdress(getMyIpAddress());
		SerializationUtil su = AndroidApplication.getInstance().getSerializationUtil();
		String string = su.serialize(votemessage);

		//Since message sent through AllJoyn are not sent to the sender, we do it here
		this.transmitReceivedMessage(votemessage);

		Message msg = mBusHandler.obtainMessage(BusHandler.PING);
		Bundle data = new Bundle();
		data.putString("groupName", this.groupName);
		data.putString("pingString", string);
		msg.setData(data);
		mBusHandler.sendMessage(msg);

	}

	@Override
	public void sendMessage(VoteMessage votemessage, String destinationIPAddress) {
		throw new UnsupportedOperationException("Unicast is not supported with AllJoyn Network interface");
	}

	@Override
	public void disconnect() {

		//leave actual group
		Message msg1 = mBusHandler.obtainMessage(BusHandler.LEAVE_GROUP, this.groupName);
		mBusHandler.sendMessage(msg1);

		if(AndroidApplication.getInstance().isAdmin()){
			Message msg2 = mBusHandler.obtainMessage(BusHandler.DESTROY_GROUP, this.groupName);
			mBusHandler.sendMessage(msg2);
		}
		this.groupName = null;

	}

	@Override
	public void joinGroup(String groupName) {
		if(groupName==null){
			//Generate group name
			int groupNumber = 1;
			groupName = "group"+groupNumber;
			while(mBusHandler.listGroups().contains(groupName)){
				groupNumber++;
				groupName = "group"+groupNumber;
			}
			//generate group password
			this.groupPassword = generatePassword();
			//			SharedPreferences preferences = context.getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
			//			Editor editor = preferences.edit();
			//			editor.putString("group_password", groupPassword);
			//			editor.commit();

		}
		String oldNetworkName = this.groupName;
		this.groupName = groupName;

		boolean apOn = new WifiAPManager().isWifiAPEnabled((WifiManager) context.getSystemService(Context.WIFI_SERVICE));

		if(AndroidApplication.getInstance().isAdmin() || apOn){
			if(oldNetworkName!=null && oldNetworkName!=""){
				Message msg1 = mBusHandler.obtainMessage(BusHandler.DESTROY_GROUP, oldNetworkName);
				mBusHandler.sendMessage(msg1);
			}
			Message msg2 = mBusHandler.obtainMessage(BusHandler.CREATE_GROUP);
			Bundle data = new Bundle();
			data.putString("groupName", this.groupName);
			data.putString("groupPassword", this.groupPassword);
			msg2.setData(data);
			mBusHandler.sendMessage(msg2);
		} else {
			Message msg3 = mBusHandler.obtainMessage(BusHandler.JOIN_GROUP);
			Bundle data = new Bundle();
			data.putString("groupName", this.groupName);
			data.putString("groupPassword", this.groupPassword);
			data.putString("saltShortDigest", this.saltShortDigest);
			msg3.setData(data);
			mBusHandler.sendMessage(msg3);
		}		

	}
	
	@Override
	public void lockGroup(){
		 Message msg = mBusHandler.obtainMessage(BusHandler.LOCK_GROUP, groupName);
	     mBusHandler.sendMessage(msg);
	}
	
	@Override
	public void unlockGroup(){
		 Message msg = mBusHandler.obtainMessage(BusHandler.UNLOCK_GROUP, groupName);
	     mBusHandler.sendMessage(msg);
	}

	@Override
	public void setGroupName(String groupName){
		this.groupName = groupName;
	}

	@Override
	public void setGroupPassword(String password){
		if(password.length()>3){
			this.saltShortDigest = password.substring(password.length()-3, password.length());
			this.groupPassword = password.substring(0,password.length()-3);
		}
	}

	@Override
	public String getSaltShortDigest(){
		return mBusHandler.getSaltShortDigest();
	}

	/**
	 * this broadcast receiver listens for incoming messages
	 */
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SerializationUtil su = AndroidApplication.getInstance().getSerializationUtil();
			transmitReceivedMessage((VoteMessage) su.deserialize(intent.getStringExtra("message")));
		}
	};

	/**
	 * this broadcast receiver listens for incoming messages
	 */
	private BroadcastReceiver mGroupEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String groupName = intent.getStringExtra("groupName");
			if(groupName.equals(groupName)){
				groupName = null;
				Intent i  = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}
		}
	};

	/**
	 * this broadcast receiver listens for incoming messages
	 */
	private BroadcastReceiver mNetworkConectionFailedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			groupName = null;
		}
	};

	/**
	 * Helper method that generates the network password
	 * @return
	 * Source: http://stackoverflow.com/questions/5683327/how-to-generate-a-random-string-of-20-characters
	 */
	private String generatePassword(){
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();

		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 10; i++) {
			int pos = random.generateSeed(1)[0]%26;
			if(pos<0)pos=pos+26;

			sb.append(chars[pos]);
		}
		return sb.toString();
	}

}
