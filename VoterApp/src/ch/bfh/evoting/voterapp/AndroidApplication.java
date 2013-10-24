package ch.bfh.evoting.voterapp;


import org.apache.log4j.Level;

import ch.bfh.evoting.voterapp.network.AllJoynNetworkInterface;
import ch.bfh.evoting.voterapp.network.NetworkInterface;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.JavaSerialization;
import ch.bfh.evoting.voterapp.util.SerializationUtil;
import ch.bfh.evoting.voterapp.util.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

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
		instance = this;
		instance.initializeInstance();
		Utility.initialiseLogging();
		LocalBroadcastManager.getInstance(this).registerReceiver(mGroupEventReceiver, new IntentFilter(BroadcastIntentTypes.networkGroupDestroyedEvent));
		LocalBroadcastManager.getInstance(this).registerReceiver(mAttackDetecter, new IntentFilter(BroadcastIntentTypes.attackDetected));
	}

	/**
	 * Initialize the Serialization method and the Network Component to use
	 */
	private void initializeInstance() {
		su = new SerializationUtil(new JavaSerialization());
		ni = new AllJoynNetworkInterface(this.getApplicationContext());// new InstaCircleNetworkInterface(this.getApplicationContext());//new SimulatedNetworkInterface(this.getApplicationContext());
	}

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

	public Activity getCurrentActivity(){
		return currentActivity;
	}

	public void setCurrentActivity(Activity currentActivity){
		this.currentActivity = currentActivity;
	}
	
	public void unregisterCurrentActivity(Activity activity){
        if (currentActivity != null && currentActivity.equals(activity))
            this.setCurrentActivity(null);
	}

	/**
	 * this broadcast receiver listens for information about the network group destruction
	 */
	private BroadcastReceiver mGroupEventReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(currentActivity!=null){
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

				// Create the AlertDialog
				builder.create().show();
			}
		}
	};
	
	/**
	 * this broadcast receiver listens for information about an attack
	 */
	private BroadcastReceiver mAttackDetecter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(currentActivity!=null){
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
				}

				// Create the AlertDialog
				builder.create().show();
			}
		}
	};
	
	@Override
	public void onTerminate() {
		if(this.ni!=null)
			this.ni.disconnect();
		super.onTerminate();
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
