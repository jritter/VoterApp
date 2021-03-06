package ch.bfh.evoting.voterapp.network;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;

import ch.bfh.evoting.alljoyn.BusHandler;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class AllJoynNetworkInterface extends AbstractNetworkInterface{

	private BusHandler mBusHandler;
	private String groupName;
	private String groupPassword;
	private String saltShortDigest;
	private boolean feedbackReceived;

	public AllJoynNetworkInterface(Context context) {
		super(context);
		HandlerThread busThread = new HandlerThread("BusHandler");
		busThread.start();
		mBusHandler = new BusHandler(busThread.getLooper(), context);

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
		return this.groupPassword;
	}

	@Override
	public String getMyUniqueId() {
		return mBusHandler.getIdentification();
	}

	@Override
	public Map<String, Participant> getGroupParticipants() {
		TreeMap<String,Participant> parts = new TreeMap<String,Participant>();
		for(String s : mBusHandler.getParticipants(this.groupName)){
			String wellKnownName = s;
			if(mBusHandler.getPeerWellKnownName(s)!=null){
				wellKnownName = mBusHandler.getPeerWellKnownName(s);
			}
			parts.put(s, new Participant(wellKnownName, s, false, false, false));
		}
		return parts;
	}

	@Override
	public void sendMessage(VoteMessage votemessage) {
		votemessage.setSenderUniqueId(getMyUniqueId());
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
	public void sendMessage(VoteMessage votemessage, String destinationUniqueId) {
		throw new UnsupportedOperationException("Unicast is not supported with AllJoyn Network interface");
	}

	@Override
	public void disconnect() {

		//leave actual group
		Message msg1 = mBusHandler.obtainMessage(BusHandler.LEAVE_GROUP, this.groupName);
		mBusHandler.sendMessage(msg1);

		Message msg2 = mBusHandler.obtainMessage(BusHandler.DESTROY_GROUP, this.groupName);
		mBusHandler.sendMessage(msg2);

		this.groupName = null;

	}

	@Override
	public void joinGroup(String groupName) {

		connectionTimeOut(10000);

		//Close previous connections
		if(this.groupName!=null && this.groupName!=""){
			disconnect();
		}

		if(AndroidApplication.getInstance().isAdmin()){
			//Generate group name
			int groupNumber = 1;
			groupName = "group"+groupNumber;
			while(mBusHandler.listGroups().contains(groupName)){
				groupNumber++;
				groupName = "group"+groupNumber;
			}
			//generate group password
			this.groupPassword = generatePassword();
		}

		this.groupName = groupName;


		boolean apOn = new WifiAPManager().isWifiAPEnabled((WifiManager) context.getSystemService(Context.WIFI_SERVICE));

		if(AndroidApplication.getInstance().isAdmin() || apOn){
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
	 * this broadcast receiver listens for incoming event indicating that the joined group was destroyed
	 */
	private BroadcastReceiver mGroupEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String groupName = intent.getStringExtra("groupName");

			if(AllJoynNetworkInterface.this.groupName!=null && groupName !=null && AllJoynNetworkInterface.this.groupName.equals(groupName)){
				groupName = null;
				Intent i  = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
			}
		}
	};

	/**
	 * this broadcast receiver listens for incoming events notifying that connection to network failed
	 */
	private BroadcastReceiver mNetworkConectionFailedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			feedbackReceived = true;
			groupName = null;

			int status = intent.getIntExtra("error", 0);
			if(status==1){
				for(int i=0; i < 2; i++)
					Toast.makeText(context, context.getString(R.string.toast_join_error_invalid_name), Toast.LENGTH_SHORT).show();
			} else if (status == 2){
				for(int i=0; i < 2; i++)
					Toast.makeText(context, context.getString(R.string.toast_join_error_admin), Toast.LENGTH_SHORT).show();
			} else if (status == 3){
				for(int i=0; i < 2; i++)
					Toast.makeText(context, context.getString(R.string.toast_join_error_voter), Toast.LENGTH_SHORT).show();
					//TODO
				HandlerThread busThread = new HandlerThread("BusHandler");
				busThread.start();
				BusHandler mBusHandler2 = new BusHandler(busThread.getLooper(), context);
			} else if (status == 4){
				for(int i=0; i < 2; i++)
					Toast.makeText(context, context.getString(R.string.toast_join_error_voter_network), Toast.LENGTH_SHORT).show();
			} else {
				for(int i=0; i < 2; i++)
					Toast.makeText(context, context.getString(R.string.toast_join_error), Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Helper method that generates the network password
	 * @return a random string of 10 lower case chars
	 */
	private String generatePassword(){
		//Inspired from: http://stackoverflow.com/questions/5683327/how-to-generate-a-random-string-of-20-characters
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

	public void connectionTimeOut(long time){
		new Handler().postDelayed(new Runnable() {

			public void run() {  
				if(!mBusHandler.getConnected() && !feedbackReceived){
					//TODO is that allowed
					disconnect();
					LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("networkConnectionFailed"));
				}
				feedbackReceived = false;
			}
		}, time); 
	}
}
