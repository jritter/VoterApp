package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ch.bfh.evoting.voterapp.adapters.NetworkParticipantListAdapter;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;
import ch.bfh.evoting.voterapp.network.NetworkMonitor;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;

/**
 * Class displaying the activity where the user can see which persons are present in the network and if they are included
 * in the electorate
 * @author Philémon von Bergen
 *
 */
public class CheckElectorateActivity extends ListActivity {

	private BroadcastReceiver networkParticipantUpdater;
	private BroadcastReceiver electorateReceiver;

	private Dialog dialogBack = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(true);

		setContentView(R.layout.activity_check_electorate);

		setupActionBar();

		Map<String, Participant> participants = new TreeMap<String, Participant>();

		if(this.getIntent().getSerializableExtra("participants") == null){
			participants = AndroidApplication.getInstance().getNetworkInterface().getGroupParticipants();
			if(participants.size()==0)
				participants.put("",new Participant("Please wait...", "", false, false));
		} else {
			participants = (Map<String,Participant>)this.getIntent().getSerializableExtra("participants");
		}

		final NetworkParticipantListAdapter npa = new NetworkParticipantListAdapter(CheckElectorateActivity.this, R.layout.list_item_participant_network, new ArrayList<Participant>(participants.values()));
		setListAdapter(npa);

		//Until the electorate is received from the administrator, the list is filled 
		//with the participant in the network
		networkParticipantUpdater = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Map<String,Participant> participants = AndroidApplication.getInstance().getNetworkInterface().getGroupParticipants();
				npa.clear();
				npa.addAll(participants.values());
				npa.notifyDataSetChanged();
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(networkParticipantUpdater, new IntentFilter(BroadcastIntentTypes.participantStateUpdate));

		//broadcast receiving the participants
		electorateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Map<String,Participant> participants = (Map<String,Participant>)intent.getSerializableExtra("participants");
				npa.clear();
				npa.addAll(participants.values());
				npa.notifyDataSetChanged();

			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(electorateReceiver, new IntentFilter(BroadcastIntentTypes.electorate));

		//broadcast receiving the info to go to next view
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Poll poll = (Poll)intent.getSerializableExtra("poll");
				//Poll is not in the DB, so reset the id
				poll.setId(-1);
				Intent i = new Intent(CheckElectorateActivity.this, ReviewPollVoterActivity.class);
				i.putExtra("poll", (Serializable) poll);
				i.putExtra("sender", intent.getStringExtra("sender"));
				startActivity(i);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(this);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(electorateReceiver);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(networkParticipantUpdater);
			}
		}, new IntentFilter(BroadcastIntentTypes.pollToReview));

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setVoteRunning(true);
		AndroidApplication.getInstance().setCurrentActivity(this);
	}

	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				startActivity(new Intent(CheckElectorateActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
			}
		});

		builder.setTitle(R.string.dialog_title_back);
		builder.setMessage(this.getString(R.string.dialog_back));

		// Create the AlertDialog
		dialogBack = builder.create();
		dialogBack.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_electorate, menu);
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
			NetworkDialogFragment ndf = NetworkDialogFragment.newInstance();
			ndf.show( getFragmentManager( ), "networkInfo" );
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_electorate), getString(R.string.help_text_electorate) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
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
