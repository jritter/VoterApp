package ch.bfh.evoting.voterapp.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ch.bfh.evoting.voterapp.CreateNetworkActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.adapters.NetworkArrayAdapter;
import ch.bfh.evoting.voterapp.network.wifi.AdhocWifiManager;

/**
 * This class implements the fragment which lists all the messages of the
 * conversation (left tab of the NetworkActiveActivity)
 * 
 * @author Juerg Ritter (rittj1@bfh.ch)
 */
public class NetworkListFragment extends ListFragment implements OnItemClickListener {

	private static final String TAG = NetworkListFragment.class.getSimpleName();

	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private int mActivatedPosition = ListView.INVALID_POSITION;

	private NetworkArrayAdapter adapter;

	private ArrayList<HashMap<String, Object>> arraylist = new ArrayList<HashMap<String, Object>>();
	private HashMap<String, Object> lastItem = new HashMap<String, Object>();

	private ListView lvNetworks;

	private List<ScanResult> results;
	private List<WifiConfiguration> configuredNetworks;

	private ScanResult selectedResult;


	private WifiManager wifi;
	private AdhocWifiManager adhoc;

	private BroadcastReceiver wifibroadcastreceiver;
	private int selectedNetId;
	
	private DialogFragment dialogFragment;
	public static final int DIALOG_FRAGMENT = 1;
	
	private boolean hideCreateNetwork = false;
	
	private AlertDialog waitWifiResutsdialog;

	public interface Callbacks {
		public void onItemSelected(String id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideCreateNetwork = getActivity().getIntent().getBooleanExtra("hideCreateNetwork", false);
		
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		this.getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

		super.onCreate(savedInstanceState);

		lvNetworks = (ListView) this.getListView();
		
		// Handling the WiFi
		wifi = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		wifi.startScan();
		adhoc = new AdhocWifiManager(wifi);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setMessage(R.string.dialog_wait_wifi);
		waitWifiResutsdialog = builder.create();

		if(wifi.getWifiState()==WifiManager.WIFI_STATE_ENABLED){
			waitWifiResutsdialog.show();
		}

		
		adapter = new NetworkArrayAdapter(getActivity(),
				R.layout.list_item_network, arraylist, hideCreateNetwork);
		
		// initializing the adapter and assign it to myself
		// adapter = new NetworkArrayAdapter(getActivity(), cursor);
		setListAdapter(adapter);
		
		if (!hideCreateNetwork){
			lastItem.put("SSID", getString(R.string.create_new_network));
			adapter.add(lastItem);
		}
		lvNetworks.setAdapter(adapter);
		lvNetworks.setOnItemClickListener(this);

		// defining what happens as soon as scan results arrive
		wifibroadcastreceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context c, Intent intent) {

				waitWifiResutsdialog.dismiss();
				results = wifi.getScanResults();
				configuredNetworks = wifi.getConfiguredNetworks();
				arraylist.clear();

				for (ScanResult result : results) {
					HashMap<String, Object> item = new HashMap<String, Object>();

					item.put("known", false);

					// check whether the network is already known, i.e. the
					// password is already stored in the device
					for (WifiConfiguration configuredNetwork : configuredNetworks) {
						if (configuredNetwork.SSID.equals("\"".concat(
								result.SSID).concat("\""))) {
							item.put("known", true);
							item.put("netid", configuredNetwork.networkId);
							break;
						}
					}

					if (result.capabilities.contains("WPA")
							|| result.capabilities.contains("WEP")) {
						item.put("secure", true);
					} else {
						item.put("secure", false);
					}
					item.put("SSID", result.SSID);
					item.put("capabilities", result.capabilities);
					item.put("object", result);
					arraylist.add(item);
					Log.d(TAG, result.SSID + " known: " + item.get("known")
							+ " netid " + item.get("netid"));
				}
				
				if (!hideCreateNetwork){
					adapter.add(lastItem);
				}
				adapter.notifyDataSetChanged();
			}
		};

		// register the receiver, subscribing for the SCAN_RESULTS_AVAILABLE
		// action
		getActivity().registerReceiver(wifibroadcastreceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}
	
	@Override
	public void onDestroy() {
		// Unregister since the activity is about to be closed.
		getActivity().unregisterReceiver(wifibroadcastreceiver);
		super.onDestroy();
	}
	
	@Override
	public void onItemClick(AdapterView<?> listview, View view, int position,
			long id) {

		if (listview.getAdapter().getCount() - 1 == position && !hideCreateNetwork) {
			// handling the last item in the list, which is the "Create network"
			// item
			
			//Start activity that creates the hotspot
			Intent intent = new Intent(this.getActivity(), CreateNetworkActivity.class);
				startActivity(intent);
			
		} else {
			// extract the Hashmap assigned to the position which has been
			// clicked
			@SuppressWarnings("unchecked")
			HashMap<String, Object> hash = (HashMap<String, Object>) listview
					.getAdapter().getItem(position);

			selectedResult = (ScanResult) hash.get("object");
			selectedNetId = -1;

			// going through the different connection scenarios
			
			if ((Boolean) hash.get("secure") && !((Boolean) hash.get("known"))) {
				dialogFragment = new ConnectNetworkDialogFragment(true);
			} else if ((Boolean) hash.get("known")) {
				selectedNetId = (Integer) hash.get("netid");
				dialogFragment = new ConnectNetworkDialogFragment(false);
			} else {
				dialogFragment = new ConnectNetworkDialogFragment(false);
			}
			dialogFragment.setTargetFragment(this, DIALOG_FRAGMENT);
			dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog);
			dialogFragment.show(getFragmentManager(), TAG);

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		
		case DIALOG_FRAGMENT:
			
            if (resultCode == Activity.RESULT_OK) {
            	if (selectedNetId != -1) {
        			adhoc.connectToNetwork(selectedNetId, getActivity());
        		} else {
        			adhoc.connectToNetwork(selectedResult.SSID,
        					((ConnectNetworkDialogFragment) dialogFragment).getNetworkKey(),
        					getActivity());
        		}

            	dialogFragment.dismiss();
            } else if (resultCode == Activity.RESULT_CANCELED){
            	dialogFragment.dismiss();
            }

            break;
		}
	}

	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/
	
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	
	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
}
