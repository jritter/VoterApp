package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.bfh.evoting.voterapp.adapters.NetworkParticipantListAdapter;
import ch.bfh.evoting.votinglib.entities.Participant;
import ch.bfh.evoting.votinglib.entities.Poll;
import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

		//broadcast receiving the participants
		final BroadcastReceiver electorateReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				@SuppressWarnings("unchecked")
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
				Intent i = new Intent(CheckElectorateActivity.this, ReviewPollActivity.class);
				i.putExtra("poll", (Serializable) poll);
				startActivity(i);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(this);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).unregisterReceiver(electorateReceiver);

			}
		}, new IntentFilter(BroadcastIntentTypes.pollToReview));
	}
}
