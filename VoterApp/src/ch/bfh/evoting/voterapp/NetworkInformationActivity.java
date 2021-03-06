package ch.bfh.evoting.voterapp;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;

public class NetworkInformationActivity extends Activity implements OnClickListener {
	
	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;
	private PendingIntent pendingIntent;
	
	private Button btnNext;
	private Button btnRecreateNetwork;
	
	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().getNetworkInterface().unlockGroup();


		Poll serializedPoll = (Poll)getIntent().getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}
		
		
		AndroidApplication.getInstance().setCurrentActivity(this);
		
		setContentView(R.layout.activity_network_information);
		setupActionBar();
		
		if(getResources().getBoolean(R.bool.display_bottom_bar) == false){
	        findViewById(R.id.layout_bottom_bar).setVisibility(View.GONE);
	    }
		
		btnRecreateNetwork = (Button) findViewById(R.id.button_recreate_network);
		btnRecreateNetwork.setOnClickListener(this);

		btnNext = (Button) findViewById(R.id.button_next);
		btnNext.setOnClickListener(this);
		
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
	protected void onPause() {
		super.onPause();
		if (nfcAvailable) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
		
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
		getMenuInflater().inflate(R.menu.network_information, menu);
		
		if(getResources().getBoolean(R.bool.display_bottom_bar)){
			menu.findItem(R.id.action_modify_network).setVisible(false);
			menu.findItem(R.id.action_next).setVisible(false);
	    }
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		Intent i;
		
		switch (item.getItemId()) {
		case android.R.id.home:
			if (this.getIntent().getBooleanExtra("goToMain", false)) {
				NavUtils.navigateUpFromSameTask(this);
			} else {
				super.onBackPressed();
			}
			if(AndroidApplication.getInstance().getNetworkInterface().getGroupName()!=null){
				if(AndroidApplication.getInstance().isAdmin()){
					//poll details
					NavUtils.navigateUpFromSameTask(this);
				} else {
					Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
							Intent.FLAG_ACTIVITY_CLEAR_TASK |
							Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			} else {
				//net config
				super.onBackPressed();
			}
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
					getString(R.string.help_title_network_info),
					getString(R.string.help_text_network_info));
			hdf.show(getFragmentManager(), "help");
			return true;
			
		case R.id.action_modify_network:
			i = new Intent(this, NetworkConfigActivity.class);
			i.putExtra("poll", poll);
			startActivity(i);
			return true;
		case R.id.action_next:
			if(AndroidApplication.getInstance().isAdmin()){
				i = new Intent(this, ElectorateActivity.class);
				i.putExtra("poll", poll);
				startActivity(i);
			} else {
				i = new Intent(this, CheckElectorateActivity.class);
				startActivity(i);
			}
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {

		if(view == btnRecreateNetwork){
			Intent i = new Intent(this, NetworkConfigActivity.class);
			i.putExtra("poll", poll);
			startActivity(i);
		} else if(view == btnNext) {
			if(AndroidApplication.getInstance().isAdmin()){
				Intent i = new Intent(this, ElectorateActivity.class);
				i.putExtra("poll", poll);
				startActivity(i);
			} else {
				Intent i = new Intent(this, CheckElectorateActivity.class);
				startActivity(i);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	public void onNewIntent(Intent intent) {
		Intent broadcastIntent = new Intent(BroadcastIntentTypes.nfcTagTapped);
		broadcastIntent.putExtra(NfcAdapter.EXTRA_TAG, intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
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
	
}
