package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.bfh.evoting.votinglib.entities.Option;
import ch.bfh.evoting.votinglib.entities.Participant;
import ch.bfh.evoting.votinglib.entities.Poll;
import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				List<Participant> participants = (List<Participant>)intent.getSerializableExtra("participants");
				npa.clear();
				npa.addAll(participants);
				npa.notifyDataSetChanged();


			}
		}, new IntentFilter(BroadcastIntentTypes.sendNetworkParticipants));

		//broadcast receiving the info to go to next view
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Poll poll = (Poll)intent.getSerializableExtra("poll");
				Intent i = new Intent(CheckElectorateActivity.this, ReviewPollActivity.class);
				i.putExtra("poll", (Serializable) poll);
				startActivity(i);

			}
		}, new IntentFilter(BroadcastIntentTypes.sendPollToReview));

		simulate();
	}


	//TODO remove, only for simulation
	private void simulate(){
		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {
				List<Participant> participants = new ArrayList<Participant>();
				Participant p1 = new Participant("p1", "192.168", false, false);
				Participant p2 = new Participant("p2", "192.168", false, false);
				Participant p3 = new Participant("p3", "192.168", true, false);
				Participant p4 = new Participant("p4", "192.168", false, false);
				Participant p5 = new Participant("p5", "192.168", true, false);
				Participant p6 = new Participant("p6", "192.168", false, false);
				Participant p7 = new Participant("p7", "192.168", true, false);
				Participant p8 = new Participant("p8", "192.168", false, false);
				Participant p9 = new Participant("p9", "192.168", false, false);
				Participant p10 = new Participant("p10", "192.168", true, false);
				Participant p11 = new Participant("p11", "192.168", false, false);
				participants.add(p1);
				participants.add(p2);
				participants.add(p3);
				participants.add(p4);
				participants.add(p5);
				participants.add(p6);
				participants.add(p7);
				participants.add(p8);
				participants.add(p9);
				participants.add(p10);
				participants.add(p11);

				SystemClock.sleep(2000);

				//Send participants
				Intent intent = new Intent(BroadcastIntentTypes.sendNetworkParticipants);
				intent.putExtra("participants", (Serializable)participants);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).sendBroadcast(intent);

				SystemClock.sleep(5000);

				//Send poll to review (=> goes to next view)
				Poll poll = new Poll();
				poll.setQuestion("This is the question");
				participants.remove(10);
				poll.setParticipants(participants);
				Option yes = new Option();
				yes.setText("Yes");
				Option no = new Option();
				no.setText("No");
				List<Option> options = new ArrayList<Option>();
				options.add(yes);
				options.add(no);
				poll.setOptions(options);

				Intent intentPoll = new Intent(BroadcastIntentTypes.sendPollToReview);
				intentPoll.putExtra("poll", (Serializable)poll);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).sendBroadcast(intentPoll);

				return null;
			}
		}.execute();
	}
}
