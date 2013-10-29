package ch.bfh.evoting.voterapp.fragment;

import java.io.Serializable;
import java.util.HashMap;

import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.NetworkConfigActivity;
import ch.bfh.evoting.voterapp.NetworkInformationActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.VoteActivity;
import ch.bfh.evoting.voterapp.network.wifi.AdhocWifiManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Fragment displaying the review of a poll
 * 
 */
public class NetworkOptionsFragment extends Fragment {

	public static final int DIALOG_FRAGMENT = 1;
	private static final String TAG = NetworkListFragment.class.getSimpleName();

	private NetworkConfigActivity activity;

	private ConnectNetworkDialogFragment dialogFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		this.activity = (NetworkConfigActivity)this.getActivity();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_network_options, container, false);

		WifiManager wm = (WifiManager) getActivity().getSystemService(NetworkConfigActivity.WIFI_SERVICE);
		WifiInfo wifiInfo = wm.getConnectionInfo();
		Button btnUseActualNetwork = (Button)v.findViewById(R.id.button_use_actual_ssid);
		btnUseActualNetwork.setText(getString(R.string.button_use_actual_ssid, wifiInfo.getSSID()));
		Button btnScanQrCode = (Button)v.findViewById(R.id.button_capture_qrcode);
		Button btnScanNFC = (Button)v.findViewById(R.id.button_scan_nfc);
		Button btnAdvancedConfig = (Button)v.findViewById(R.id.button_advanced_network_config);

		LinearLayout layout = (LinearLayout)v.findViewById(R.id.layout_network_options);

		if(AndroidApplication.getInstance().isAdmin()){
			layout.removeView(btnScanQrCode);
			layout.removeView(btnScanNFC);
		}

		if(!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC) && btnScanNFC!=null){
			layout.removeView(btnScanNFC);
		}

		btnUseActualNetwork.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

					dialogFragment = new ConnectNetworkDialogFragment(false);

					dialogFragment.setTargetFragment(NetworkOptionsFragment.this, DIALOG_FRAGMENT);
					dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Dialog);
					dialogFragment.show(getFragmentManager(), TAG);
			}
		});

		if(btnScanQrCode!=null){
			btnScanQrCode.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.setPackage(activity.getApplication().getPackageName());
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(intent, 0);

				}
			});
		}

		if(btnScanNFC!=null){
			btnScanNFC.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Create new fragment and transaction
					Fragment nfcFragment = new NFCFragment();
					FragmentTransaction transaction = getFragmentManager().beginTransaction();

					// Replace whatever is in the fragment_container view with this fragment,
					// and add the transaction to the back stack
					transaction.replace(R.id.fragment_container, nfcFragment);
					transaction.addToBackStack(null);

					// Commit the transaction
					transaction.commit();

				}
			});
		}

		btnAdvancedConfig.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Create new fragment and transaction
				Fragment advancedNetworkFragment = new NetworkListFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();

				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack
				transaction.replace(R.id.fragment_container, advancedNetworkFragment);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();

			}
		});

		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){

		case DIALOG_FRAGMENT:

			WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
			AdhocWifiManager adhoc = new AdhocWifiManager(wifiManager);

			if (resultCode == Activity.RESULT_OK) {

				adhoc.connectToNetwork(this.getCurrentSsid(this.getActivity()),
						((ConnectNetworkDialogFragment) dialogFragment).getNetworkKey(),
						getActivity());


				dialogFragment.dismiss();
			} else if (resultCode == Activity.RESULT_CANCELED){
				dialogFragment.dismiss();
			}

			break;
		default:
			activity.onActivityResult(requestCode, resultCode, data);
		}
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	public String getCurrentSsid(Context context) {
		String ssid = null;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null && !connectionInfo.getSSID().equals("")) {
				ssid = connectionInfo.getSSID();
			}
		}
		return ssid;
	}
}
