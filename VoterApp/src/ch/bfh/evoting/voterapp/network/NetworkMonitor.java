package ch.bfh.evoting.voterapp.network;

import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Broadcast receiver listening for events concerning the wifi network
 * @author Philémon von Bergen
 *
 */
public class NetworkMonitor extends BroadcastReceiver {

	private Context context;
	private boolean wifiEnabled;
	private boolean connected;
	private String ssid;
	private WifiManager wifi;
	private ConnectivityManager connManager;

	/**
	 * Create an object
	 * @param context android apllication context
	 */
	public NetworkMonitor(Context context){
		this.context=context;
		wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		checkConnectivity();
	}
	
	/**
	 * Helper method checking the state of the connectivity
	 */
	private void checkConnectivity(){
				
	    if (wifi.isWifiEnabled()==true) {
	      wifiEnabled = true;
	    } else {
	    	wifiEnabled = false;
	    }
	    NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if(netInfo.isConnected()){
	    	connected = true;
	    	ssid = wifi.getConnectionInfo().getSSID();
		} else {
			connected = false;
			ssid = "";
		}
	    if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLING){
	    	this.onLosingConnection();
	    }
	    if(WifiInfo.getDetailedStateOf(wifi.getConnectionInfo().getSupplicantState())==NetworkInfo.DetailedState.DISCONNECTING){
	    	this.onLosingConnection();
	    }
	}
	
	/**
	 * Helper method called when connectivity has been lost
	 */
	private void onLosingConnection() {
		Intent intent = new Intent(BroadcastIntentTypes.networkGroupDestroyedEvent);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	/**
	 * Indicate if wifi is enabled
	 * @return true if yes, false otherwise
	 */
	public boolean isWifiEnabled(){
		checkConnectivity();
		return wifiEnabled;
	}
	
	/**
	 * Indicate if the device is connected to a network
	 * @return true if yes, false otherwise
	 */
	public boolean isConnected(){
		/*WifiManager wm = (WifiManager) getActivity().getSystemService(NetworkConfigActivity.WIFI_SERVICE);
		WifiInfo wifiInfo = wm.getConnectionInfo().g;*/
		checkConnectivity();
		return connected;
	}
	
	/**
	 * Get the currently connected SSID
	 * @return the currently connected SSID
	 */
	public String getConnectedSSID(){
		checkConnectivity();
		return ssid;
	}
}