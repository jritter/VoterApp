package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.bfh.evoting.voterapp.adapters.NetworkParticipantListAdapter;
import ch.bfh.evoting.votinglib.AndroidApplication;
import ch.bfh.evoting.votinglib.entities.Participant;
import ch.bfh.evoting.votinglib.entities.Poll;
import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Class displaying the activity where the user can see which persons are present in the network and if they are included
 * in the electorate
 * @author Philémon von Bergen
 *
 */
public class CheckElectorateActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_electorate);

		List<Participant> participants = new ArrayList<Participant>();
		participants.add(new Participant("Please wait...", "", false, false));

		final NetworkParticipantListAdapter npa = new NetworkParticipantListAdapter(CheckElectorateActivity.this, R.layout.list_item_participant_network, participants);
		setListAdapter(npa);

		//Until the electorate is received from the administrator, the list is filled 
		//with the participant in the network
		final BroadcastReceiver networkParticipantUpdater = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Map<String,Participant> participants = AndroidApplication.getInstance().getNetworkInterface().getConversationParticipants();
				npa.clear();
				npa.addAll(participants.values());
				npa.notifyDataSetChanged();
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(networkParticipantUpdater, new IntentFilter(BroadcastIntentTypes.participantStateUpdate));

		//broadcast receiving the participants
		final BroadcastReceiver electorateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				@SuppressWarnings("unchecked")
				Map<String,Participant> participants = (Map<String,Participant>)intent.getSerializableExtra("participants");
				npa.clear();
				npa.addAll(participants.values());
				npa.notifyDataSetChanged();
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(networkParticipantUpdater);

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
				Intent i = new Intent(CheckElectorateActivity.this, ReviewPollActivity.class);
				i.putExtra("poll", (Serializable) poll);
				startActivity(i);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(this);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(electorateReceiver);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(networkParticipantUpdater);
			}
		}, new IntentFilter(BroadcastIntentTypes.pollToReview));

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
		case R.id.network_info:
			Intent i = new Intent(this, ch.bfh.evoting.votinglib.NetworkInformationsActivity.class);
			startActivity(i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_electorate), getString(R.string.help_text_electorate) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
