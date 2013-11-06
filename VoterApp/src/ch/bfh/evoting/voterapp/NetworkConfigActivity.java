package ch.bfh.evoting.voterapp;


import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkOptionsFragment;
import ch.bfh.evoting.voterapp.network.wifi.AdhocWifiManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

/**
 * Activity displaying the available networks
 * 
 * @author Philémon von Bergen
 * 
 */
public class NetworkConfigActivity extends Activity implements TextWatcher{
	
	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;
	private PendingIntent pendingIntent;

	private WifiManager wifi;
	private AdhocWifiManager adhoc;

	private SharedPreferences preferences;
	private EditText etIdentification;
	private BroadcastReceiver serviceStartedListener;

	private boolean active;
	private Poll poll;

	private AsyncTask<Object, Object, Object> rescanWifiTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		final FrameLayout overlayFramelayout = new FrameLayout(this);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
		overlayFramelayout.setLayoutParams(layoutParams);

		View view = getLayoutInflater().inflate(R.layout.activity_network_config, overlayFramelayout,false);
		overlayFramelayout.addView(view);

		final SharedPreferences settings = getSharedPreferences(AndroidApplication.PREFS_NAME, MODE_PRIVATE);

		if(settings.getBoolean("first_run", true)){
			//Show General Help Overlay
			final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_parent_button, null,false);
			overlayFramelayout.addView(overlay_view);
			overlay_view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					overlayFramelayout.removeView(overlay_view);
					settings.edit().putBoolean("first_run", false).commit();
					//Show Help Overlay for this activity
					if(settings.getBoolean("first_run_"+NetworkConfigActivity.this.getClass().getSimpleName(), true)){
						final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_network_config, null,false);
						overlayFramelayout.addView(overlay_view);
						overlay_view.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								overlayFramelayout.removeView(overlay_view);
								settings.edit().putBoolean("first_run_"+NetworkConfigActivity.this.getClass().getSimpleName(), false).commit();					
							}
						});
					}
				}
			});
		} else if(settings.getBoolean("first_run_"+this.getClass().getSimpleName(), true)){
			//Show Help Overlay for this activity
			final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_network_config, null,false);
			overlayFramelayout.addView(overlay_view);
			overlay_view.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					overlayFramelayout.removeView(overlay_view);
					settings.edit().putBoolean("first_run_"+NetworkConfigActivity.this.getClass().getSimpleName(), false).commit();					
				}
			});
		}
		setContentView(overlayFramelayout);


		AndroidApplication.getInstance().setCurrentActivity(this);

		Fragment fg = new NetworkOptionsFragment();
		// adding fragment to relative layout by using layout id
		getFragmentManager().beginTransaction().add(R.id.fragment_container, fg).commit();

		// Show the Up button in the action bar.
		setupActionBar();

		Poll serializedPoll = (Poll)getIntent().getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}


		// reading the identification from the preferences, if it is not there
		// it will try to read the name of the device owner
		preferences = getSharedPreferences(AndroidApplication.PREFS_NAME, 0);
		String identification = preferences.getString("identification", "");

		if (identification.equals("")) {
			identification = readOwnerName();
			// saving the identification field
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("identification", identification);
			editor.commit();
		}

		etIdentification = (EditText) findViewById(R.id.edittext_identification);
		etIdentification.setText(identification);

		etIdentification.addTextChangedListener(this);

		serviceStartedListener = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				active = false;
				rescanWifiTask.cancel(true);
				LocalBroadcastManager.getInstance(NetworkConfigActivity.this).unregisterReceiver(this);
				if(AndroidApplication.getInstance().isAdmin()){
					Intent i = new Intent(NetworkConfigActivity.this, NetworkInformationActivity.class);
					i.putExtra("poll", poll);
					startActivity(i);
				} else {
					startActivity(new Intent(NetworkConfigActivity.this, CheckElectorateActivity.class));
				}
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(
				serviceStartedListener,
				new IntentFilter(BroadcastIntentTypes.networkConnectionSuccessful));

		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		adhoc = new AdhocWifiManager(wifi);

		active = true;
		rescanWifiTask = new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... arg0) {

				while (active) {
					SystemClock.sleep(5000);
					wifi.startScan();
				}
				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		// Is NFC available on this device?
		nfcAvailable = this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_NFC);

		if (nfcAvailable) {

			nfcAdapter = NfcAdapter.getDefaultAdapter(this);

			if (nfcAdapter.isEnabled()) {

				// Setting up a pending intent that is invoked when an NFC tag
				// is tapped on the back
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			} else {
				nfcAvailable = false;
			}
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		//if extra is present, it has priority on the saved poll
		Poll serializedPoll = (Poll)intent.getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}
		
		if (intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) != null){
			Intent broadcastIntent = new Intent(BroadcastIntentTypes.nfcTagTapped);
			broadcastIntent.putExtra(NfcAdapter.EXTRA_TAG, intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
		
		super.onNewIntent(intent);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		poll = (Poll)savedInstanceState.getSerializable("poll");
	}

	@Override
	protected void onPause() {
		active = false;
		rescanWifiTask.cancel(true);
		
		if (nfcAvailable) {
			nfcAdapter.disableForegroundDispatch(this);
		}
		
		super.onPause();
	}

	@Override
	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
		LocalBroadcastManager.getInstance(NetworkConfigActivity.this).registerReceiver(serviceStartedListener, new IntentFilter(BroadcastIntentTypes.networkConnectionSuccessful));

		active = true;
		rescanWifiTask = new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... arg0) {

				while (active) {
					SystemClock.sleep(5000);
					wifi.startScan();
				}
				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
			nfcAvailable = true;
		}

		// make sure that this activity is the first one which can handle the
		// NFC tags
		if (nfcAvailable) {
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					Utility.getNFCIntentFilters(), null);
		}
		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.network_config, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			//			NavUtils.navigateUpFromSameTask(this);
			super.onBackPressed();
			return true;
		case R.id.network_info:
			NetworkDialogFragment ndf = NetworkDialogFragment.newInstance();			
			ndf.show( getFragmentManager( ), "networkInfo" );
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
					getString(R.string.help_title_network_config),
					getString(R.string.help_text_network_config));
			hdf.show(getFragmentManager(), "help");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// saving the identification field
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("identification", etIdentification.getText()
				.toString());
		editor.commit();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	
	}
	
	

	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	/**
	 * This method is used to extract the name of the device owner
	 * 
	 * @return the name of the device owner
	 */
	private String readOwnerName() {

		Cursor c = getContentResolver().query(
				ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		if (c.getCount() == 0) {
			return "";
		}
		c.moveToFirst();
		String displayName = c.getString(c.getColumnIndex("display_name"));
		c.close();

		return displayName;

	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String[] config = intent.getStringExtra("SCAN_RESULT").split(
						"\\|\\|");

				// saving the values that we got
				SharedPreferences.Editor editor = preferences.edit();
				editor.putString("SSID", config[0]);
				editor.commit();

				AndroidApplication.getInstance().getNetworkInterface().setGroupName(config[1]);
				AndroidApplication.getInstance().getNetworkInterface().setGroupPassword(config[2]);

				// connect to the network
				connect(config);

			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
	}

	/**
	 * This method initiates the connect process
	 * 
	 * @param config
	 *            an array containing the SSID and the password of the network
	 */
	private void connect(String[] config) {
		boolean connectedSuccessful = false;
		// check whether the network is already known, i.e. the password is
		// already stored in the device
		for (WifiConfiguration configuredNetwork : wifi.getConfiguredNetworks()) {
			if (configuredNetwork.SSID.equals("\"".concat(config[0]).concat(
					"\""))) {
				connectedSuccessful = true;
				adhoc.connectToNetwork(configuredNetwork.networkId, this);
				break;
			}
		}
		if (!connectedSuccessful) {
			for (ScanResult result : wifi.getScanResults()) {
				if (result.SSID.equals(config[0])) {
					connectedSuccessful = true;
					adhoc.connectToNetwork(config[0], config[1], this);
					break;
				}
			}
		}

		// display a message if the connection was not successful
		if (!connectedSuccessful) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.network_not_found);
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.setMessage(getString(R.string.network_not_found_text, config[0]));
			alertDialog.show();
		}
	}

	public String getIdentification(){
		Log.d("NetworkConfigActivity", "identification is "+this.etIdentification.toString());
		return this.etIdentification.getText().toString();
	}

}
