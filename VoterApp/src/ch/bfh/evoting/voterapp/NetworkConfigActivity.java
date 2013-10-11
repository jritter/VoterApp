package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.votinglib.AndroidApplication;
import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity displaying the available networks
 * @author Philémon von Bergen
 *
 */
public class NetworkConfigActivity extends Activity implements TextWatcher, OnClickListener {

	
	private WifiManager wifi;

	private static final String PREFS_NAME = "network_preferences";
	private SharedPreferences preferences;
	private EditText etIdentification;
	private BroadcastReceiver serviceStartedListener;
	
	private Button btnRescanWifi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_config);
		// Show the Up button in the action bar.
		setupActionBar();

		// reading the identification from the preferences, if it is not there
		// it will try to read the name of the device owner
		preferences = getSharedPreferences(PREFS_NAME, 0);
		String identification = preferences.getString("identification",	"");

		if(identification.equals("")){
			identification = readOwnerName();
			// saving the identification field
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("identification", identification);
			editor.commit();
		}
		
		btnRescanWifi = (Button) findViewById(R.id.button_rescan_wifi);
		btnRescanWifi.setOnClickListener(this);

		etIdentification = (EditText) findViewById(R.id.edittext_identification);
		etIdentification.setText(identification);

		etIdentification.addTextChangedListener(this);
		
		serviceStartedListener = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				startActivity(new Intent(NetworkConfigActivity.this, CheckElectorateActivity.class));
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(serviceStartedListener, new IntentFilter("NetworkServiceStarted"));

		
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.network_info:
			Intent i = new Intent(this, ch.bfh.evoting.votinglib.NetworkInformationsActivity.class);
			startActivity(i);
			LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceStartedListener);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_network_config), getString(R.string.help_text_network_config) );
	        hdf.show( getFragmentManager( ), "help" );
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
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
	
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
	
//	/**
//	 * this broadcast receiver listens for incoming instacircle broadcast notifying that network service was started
//	 */
//	private BroadcastReceiver serviceStartedListener = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			startActivity(new Intent(NetworkConfigActivity.this, CheckElectorateActivity.class));
//		}
//	};

	@Override
	public void onClick(View view) {
		if (view == btnRescanWifi){
			wifi.startScan();
			Toast.makeText(this, "Rescan initiated", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		//do nothing because we don't want that people access to an anterior activity
	}

}
