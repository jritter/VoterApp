package ch.bfh.evoting.voterapp;

import java.util.concurrent.Callable;

import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * First activity, displaying the buttons for the different actions
 * @author Philémon von Bergen
 *
 */
public class MainActivity extends Activity implements OnClickListener {
	
	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;
	private PendingIntent pendingIntent;

	private Button btnSetupNetwork;
	private Button btnPollArchive;
	private Button btnPolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		setContentView(R.layout.activity_main);
		AndroidApplication.getInstance().setVoteRunning(false);
		AndroidApplication.getInstance().setCurrentActivity(this);

		AndroidApplication.getInstance().setIsAdmin(false);

		btnSetupNetwork = (Button) findViewById(R.id.button_joinnetwork);
		btnPolls = (Button) findViewById(R.id.button_polls);
		btnPollArchive = (Button) findViewById(R.id.button_archive);

		btnSetupNetwork.setOnClickListener(this);
		btnPolls.setOnClickListener(this);
		btnPollArchive.setOnClickListener(this);
		
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
	protected void onResume(){
		AndroidApplication.getInstance().setIsAdmin(false);
		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		
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
	public void onClick(View view) {
		if (view == btnSetupNetwork) {
			if(!AndroidApplication.getInstance().getNetworkMonitor().isWifiEnabled()){
				for(int i=0; i<2; i++)
					Toast.makeText(this, getString(R.string.toast_wifi_is_disabled), Toast.LENGTH_SHORT).show();
				return;
			}
			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return goToNetworkConfig();
				}
			});

		} else if (view == btnPolls){
			Intent intent = new Intent(this, PollActivity.class);
			startActivity(intent);
		} else if (view == btnPollArchive){
			Intent intent = new Intent(this, ListTerminatedPollsActivity.class);
			startActivity(intent);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.network_info:
			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return showNetworkInfoDialog();
				}
			});
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_main), getString(R.string.help_text_main) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	private void waitForNetworkInterface(final Callable<Void> methodToExecute){
		//Network interface can be null since it is created in an async task, so we wait until the task is completed
		if(AndroidApplication.getInstance().getNetworkInterface()==null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_wait_wifi);
			final AlertDialog waitDialog = builder.create();
			waitDialog.show();

			new AsyncTask<Object, Object, Object>(){

				@Override
				protected Object doInBackground(Object... params) {
					while(AndroidApplication.getInstance().getNetworkInterface()==null){
						//wait
					}
					waitDialog.dismiss();
					try {
						methodToExecute.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return;
		}
		//then start next activity
		try {
			methodToExecute.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Void goToNetworkConfig(){
		//then start next activity
		if(AndroidApplication.getInstance().getNetworkInterface().getGroupName()==null){
			Intent intent = new Intent(this, NetworkConfigActivity.class);
			intent.putExtra("hideCreateNetwork", true);
			startActivity(intent);
		} else {
			Intent i = new Intent(this, NetworkInformationActivity.class);
			startActivity(i);
		}
		return null;
	}

	private Void showNetworkInfoDialog(){
		NetworkDialogFragment ndf = NetworkDialogFragment.newInstance();			
		ndf.show( getFragmentManager( ), "networkInfo" );
		return null;
	}


}
