package ch.bfh.evoting.voterapp;


import org.apache.log4j.Level;

import ch.bfh.evoting.voterapp.network.AllJoynNetworkInterface;
import ch.bfh.evoting.voterapp.network.NetworkInterface;
import ch.bfh.evoting.voterapp.network.NetworkMonitor;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.JavaSerialization;
import ch.bfh.evoting.voterapp.util.SerializationUtil;
import ch.bfh.evoting.voterapp.util.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

/**
 * Class representing the application. This class is used to do some initializations and to share data.
 * @author Philémon von Bergen
 *
 */
public class AndroidApplication extends Application {

	public static final String PREFS_NAME = "network_preferences";
	public static final Level LEVEL = Level.DEBUG;

	private static AndroidApplication instance;
	private SerializationUtil su;
	private NetworkInterface ni;
	private Activity currentActivity = null;
	private boolean isAdmin = false;
	private boolean voteRunning;
	
	private AlertDialog dialogNetworkLost;
	private NetworkMonitor networkMonitor;

	/**
	 * Return the single instance of this class
	 * @return the single instance of this class
	 */
	public static AndroidApplication getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		//TODO remove when not used anymore
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
//		settings.edit().putBoolean("first_run_ReviewPollVoterActivity", true).commit();
//		settings.edit().putBoolean("first_run_NetworkConfigActivity", true).commit();
//		settings.edit().putBoolean("first_run", true).commit();

		WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if(!wm.isWifiEnabled()){
			wm.setWifiEnabled(true);
		}

		instance = this;
		instance.initializeInstance();
		Utility.initialiseLogging();
		
		//wifi event listener
		IntentFilter filters = new IntentFilter();
		filters.addAction("android.net.wifi.STATE_CHANGED");
		filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		networkMonitor = new NetworkMonitor(this);
		this.registerReceiver(networkMonitor, filters);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(mGroupEventReceiver, new IntentFilter(BroadcastIntentTypes.networkGroupDestroyedEvent));
		LocalBroadcastManager.getInstance(this).registerReceiver(mAttackDetecter, new IntentFilter(BroadcastIntentTypes.attackDetected));
		LocalBroadcastManager.getInstance(this).registerReceiver(startPollReceiver, new IntentFilter(BroadcastIntentTypes.electorate));
	}
	
	@Override
	public void onTerminate() {
		if(this.ni!=null)
			this.ni.disconnect();
		this.unregisterReceiver(networkMonitor);
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel("mobivote", 1);
		super.onTerminate();
	}


	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/
	
	/**
	 * Initialize the Serialization method and the Network Component to use
	 */
	private void initializeInstance() {
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				su = new SerializationUtil(new JavaSerialization());
				ni = new AllJoynNetworkInterface(AndroidApplication.this.getApplicationContext());///* new InstaCircleNetworkInterface(this.getApplicationContext());*/new SimulatedNetworkInterface(AndroidApplication.this.getApplicationContext());

				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	/*--------------------------------------------------------------------------------------------
	 * Getters/Setters
	--------------------------------------------------------------------------------------------*/
	
	/**
	 * Get the serialization helper
	 * @return the serialization helper
	 */
	public SerializationUtil getSerializationUtil(){
		return su;
	}

	/**
	 * Get the network component
	 * @return the network component
	 */
	public NetworkInterface getNetworkInterface(){
		return ni;
	}

	public NetworkMonitor getNetworkMonitor(){
		return this.networkMonitor;
	}
	
	public Activity getCurrentActivity(){
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity){
		this.currentActivity = currentActivity;
		
		if(isVoteRunning()){
			// Create a pending intent which will be invoked after tapping on the
			// Android notification
			Intent notificationIntent = new Intent(this,
					currentActivity.getClass());
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingNotificationIntent = PendingIntent.getActivity(
					this, 0, notificationIntent, 0);

			// Setting up the notification which is being displayed
			Notification.Builder notificationBuilder = new Notification.Builder(
					this);
			notificationBuilder.setContentTitle(getResources().getString(
					R.string.app_name));
			notificationBuilder
					.setContentText(getResources().getString(R.string.notification));
			notificationBuilder
					.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentIntent(pendingNotificationIntent);
			notificationBuilder.setOngoing(true);
			Notification notification = notificationBuilder.getNotification();

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify("mobivote", 1, notification);
		} else {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel("mobivote", 1);
		}
	}

	public void unregisterCurrentActivity(Activity activity){
		if (currentActivity != null && currentActivity.equals(activity))
			this.setCurrentActivity(null);
	}

	public void setVoteRunning(boolean running){
		this.voteRunning = running;
	}

	public boolean isVoteRunning(){
		return voteRunning;
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}


	/*--------------------------------------------------------------------------------------------
	 * Broadcast receivers
	--------------------------------------------------------------------------------------------*/
	
	/**
	 * this broadcast receiver listens for information about the network group destruction
	 */
	private BroadcastReceiver mGroupEventReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(currentActivity!=null && ni.getNetworkName()!=null){
				if(voteRunning){
				AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
				// Add the buttons
				builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(AndroidApplication.this, MainActivity.class);
						currentActivity.startActivity(i);
					}
				});

				builder.setTitle(R.string.dialog_title_network_lost);
				builder.setMessage(R.string.dialog_network_lost);

				
				dialogNetworkLost = builder.create();
				dialogNetworkLost.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
						dialogNetworkLost.getButton(AlertDialog.BUTTON_NEUTRAL).setBackgroundResource(
								R.drawable.selectable_background_votebartheme);
					}
				});
				
				// Create the AlertDialog
				dialogNetworkLost.show();
				} else {
					for(int i=0; i < 2; i++)
						Toast.makeText(currentActivity, getResources().getString(R.string.toast_network_lost), Toast.LENGTH_SHORT).show();
				}
				ni.disconnect();
			}
		}
	};

	/**
	 * this broadcast receiver listens for information about an attack
	 */
	private BroadcastReceiver mAttackDetecter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(currentActivity!=null && voteRunning){
				AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
				// Add the buttons
				builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent i = new Intent(AndroidApplication.this, MainActivity.class);
						currentActivity.startActivity(i);
					}
				});

				builder.setTitle(R.string.dialog_title_attack_detected);
				if(intent.getIntExtra("type", 0)==1){
					builder.setMessage(R.string.dialog_attack_impersonalization);
				} else if(intent.getIntExtra("type", 0)==2){
					builder.setMessage(R.string.dialog_attack_different_senders);
				} else if(intent.getIntExtra("type", 0)==3){
					builder.setMessage(R.string.dialog_attack_different_salts);
				}

				// Create the AlertDialog
				builder.create().show();
			}
		}
	};
	
//	/**
//	 * this broadcast receiver listens for information about an attack
//	 */
//	private BroadcastReceiver networkMonitor = new BroadcastReceiver() {
//		
//		private boolean wifiEnabled;
//		private boolean connected;
//		private String ssid;
//		private boolean connectivityChecked = false;
//		
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			checkConnectivity();
//			connectivityChecked =true;
//		}
//		
//		private void checkConnectivity(){
//			WifiManager wifi = (WifiManager) AndroidApplication.this.getSystemService(Context.WIFI_SERVICE);
//		    if (wifi.isWifiEnabled()==true) {
//		      wifiEnabled = true;
//		    } else {
//		    	wifiEnabled = false;
//		    }
//		    if(WifiInfo.getDetailedStateOf(wifi.getConnectionInfo().getSupplicantState())==NetworkInfo.DetailedState.CONNECTED){
//		    	connected = true;
//		    	ssid = wifi.getConnectionInfo().getSSID();
//			} else {
//				connected = false;
//				ssid = "";
//			}
//		    if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLING){
//		    	this.onLosingConnection();
//		    }
//		    if(WifiInfo.getDetailedStateOf(wifi.getConnectionInfo().getSupplicantState())==NetworkInfo.DetailedState.DISCONNECTING){
//		    	this.onLosingConnection();
//		    }
//		}
//		
//		private void onLosingConnection() {
//			Intent intent = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
//			LocalBroadcastManager.getInstance(AndroidApplication.this).sendBroadcast(intent);
//		}
//		
//		public boolean isWifiEnabled(){
//			if(!connectivityChecked) checkConnectivity();
//			return wifiEnabled;
//		}
//		
//		public boolean isConnected(){
//			if(!connectivityChecked) checkConnectivity();
//			return connected;
//		}
//		
//		public String getConnectedSSID(){
//			if(!connectivityChecked) checkConnectivity();
//			return ssid;
//		}
//	};

	/**
	 * this broadcast receiver listen for broadcasts containing the electorate. So, if the user is member
	 * of a session, when the admin sends the electorate, the user is redirected to the correct activity, wherever
	 * he is.
	 */
	private BroadcastReceiver startPollReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(!isAdmin && !(currentActivity instanceof CheckElectorateActivity) && currentActivity!=null){
				Intent i = new Intent(AndroidApplication.this, CheckElectorateActivity.class);
				i.putExtra("participants", intent.getSerializableExtra("participants"));
				currentActivity.startActivity(i);
			}
		}
	};

}
