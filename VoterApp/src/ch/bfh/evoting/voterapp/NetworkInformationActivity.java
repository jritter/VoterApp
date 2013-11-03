package ch.bfh.evoting.voterapp;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;

public class NetworkInformationActivity extends Activity implements OnClickListener {
	
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
	}

	@Override
	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.network_information, menu);
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

		//TODO can we remove this ??
		/*if (view == btnWriteNfcTag){
			if (!nfcAdapter.isEnabled()) {

				// if nfc is available but deactivated ask the user whether he
				// wants to enable it. If yes, redirect to the settings.
				AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
				.create();
				alertDialog.setTitle("InstaCircle - NFC needs to be enabled");
				alertDialog
				.setMessage("In order to use this feature, NFC must be enabled. Enable now?");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
						startActivity(new Intent(
								android.provider.Settings.ACTION_WIRELESS_SETTINGS));
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				});
				alertDialog.show();
			} else {
				// display a progress dialog waiting for the NFC tag to be
				// tapped
				writeNfcEnabled = true;
				writeNfcTagDialog = new ProgressDialog(getActivity());
				writeNfcTagDialog
				.setTitle("InstaCircle - Share Networkconfiguration with NFC Tag");
				writeNfcTagDialog
				.setMessage("Please tap a writeable NFC Tag on the back of your device...");
				writeNfcTagDialog.setCancelable(false);
				writeNfcTagDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						writeNfcEnabled = false;
						dialog.dismiss();
					}
				});

				writeNfcTagDialog.show();
			}
		} else */if(view == btnRecreateNetwork){
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
