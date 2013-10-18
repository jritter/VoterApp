package ch.bfh.evoting.voterapp.network;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.alljoyn.bus.Status;



import ch.bfh.evoting.alljoyn.BusHandler;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.WaitForVotesActivity;
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
import android.text.BoringLayout;
import android.util.Log;

public class AllJoynNetworkInterface extends AbstractNetworkInterface{

	private static final String PREFS_NAME = "network_preferences";

	private BusHandler mBusHandler;
	private String networkName;

	public AllJoynNetworkInterface(Context context) {
		super(context);
		HandlerThread busThread = new HandlerThread("BusHandler");
		busThread.start();
		mBusHandler = new BusHandler(busThread.getLooper(), AndroidApplication.getInstance().getApplicationContext());

		// Listening for arriving messages
		LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("messageArrived"));
		// Listening for group destroy signal
		LocalBroadcastManager.getInstance(context).registerReceiver(mGroupEventReceiver, new IntentFilter("groupDestroyed"));
	}

	@Override
	public String getNetworkName() {
		SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
		preferences = context.getSharedPreferences(PREFS_NAME, 0);
		return preferences.getString("SSID", "");
	}

	@Override
	public String getConversationPassword() {
		return networkName;
	}

	@Override
	public String getMyIpAddress() {
		return mBusHandler.getIdentification();
	}

	@Override
	public Map<String, Participant> getConversationParticipants() {
		TreeMap<String,Participant> parts = new TreeMap<String,Participant>();
		for(String s : mBusHandler.getParticipants(this.networkName)){
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
		data.putString("groupName", this.networkName);
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
		Message msg1 = mBusHandler.obtainMessage(BusHandler.LEAVE_GROUP, this.networkName);
		mBusHandler.sendMessage(msg1);
		
		if(AndroidApplication.getInstance().isAdmin()){
			Message msg2 = mBusHandler.obtainMessage(BusHandler.DESTROY_GROUP, this.networkName);
			mBusHandler.sendMessage(msg2);
		}
		this.networkName = null;
		
	}

	@Override
	public void joinNetwork(String networkName) {
		String oldNetworkName = this.networkName;
		this.networkName = networkName;

		boolean apOn = new WifiAPManager().isWifiAPEnabled((WifiManager) context.getSystemService(Context.WIFI_SERVICE));

		if(AndroidApplication.getInstance().isAdmin() || apOn){
			if(oldNetworkName!=null && oldNetworkName!=""){
				Message msg1 = mBusHandler.obtainMessage(BusHandler.DESTROY_GROUP, oldNetworkName);
				mBusHandler.sendMessage(msg1);
			}
			Message msg2 = mBusHandler.obtainMessage(BusHandler.CREATE_GROUP, this.networkName);
			mBusHandler.sendMessage(msg2);
		} else {
			Message msg3 = mBusHandler.obtainMessage(BusHandler.JOIN_GROUP, this.networkName);
			mBusHandler.sendMessage(msg3);
		}		

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
			if(groupName.equals(networkName)){
				networkName = null;
				Intent i  = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}
		}
	};

}
