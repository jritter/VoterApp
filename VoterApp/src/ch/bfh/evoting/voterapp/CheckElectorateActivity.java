package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.bfh.evoting.voterapp.adapters.NetworkParticipantListAdapter;
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

				@SuppressWarnings("unchecked")
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
				poll.setStartTime(System.currentTimeMillis());

				poll = createPoll();

				Intent intentPoll = new Intent(BroadcastIntentTypes.sendPollToReview);
				intentPoll.putExtra("poll", (Serializable)poll);
				LocalBroadcastManager.getInstance(CheckElectorateActivity.this).sendBroadcast(intentPoll);

				return null;
			}
		}.execute();
	}

	private Poll createPoll(){
		//Create the option
		String question = "What do you think very very very long question very very very long question very very very long question very very very long question?";
		Option yes = new Option();
		yes.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no = new Option();
		no.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes1 = new Option();
		yes1.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no1 = new Option();
		no1.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes2 = new Option();
		yes2.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no2 = new Option();
		no2.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes3 = new Option();
		yes3.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no3 = new Option();
		no3.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes4 = new Option();
		yes4.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no4 = new Option();
		no4.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes5 = new Option();
		yes5.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no5 = new Option();
		no5.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes6 = new Option();
		yes6.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no6 = new Option();
		no6.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");
		Option yes7 = new Option();
		yes7.setText("Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes Yes");
		Option no7 = new Option();
		no7.setText("No No No No No No No No No No No No No No No No No No No NoNo No No No No No No No No No No No");

		List<Option> options = new ArrayList<Option>();
		options.add(yes);
		options.add(no);
		options.add(yes1);
		options.add(no1);
		options.add(yes2);
		options.add(no2);
		options.add(yes3);
		options.add(no3);
		options.add(yes4);
		options.add(no4);
		options.add(yes5);
		options.add(no5);
		options.add(yes6);
		options.add(no6);
		options.add(yes7);
		options.add(no7);

		List<Participant> participants = new ArrayList<Participant>();
		Participant p1 = new Participant("Participant 1 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p1);
		Participant p2 = new Participant("Participant 2 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p2);
		Participant p3 = new Participant("Participant 3 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p3);
		Participant p4 = new Participant("Participant 4 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p4);
		Participant p5 = new Participant("Participant 5 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p5);
		Participant p6 = new Participant("Participant 6 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p6);
		Participant p7 = new Participant("Participant 7 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p7);
		Participant p8 = new Participant("Participant 8 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p8);
		Participant p9 = new Participant("Participant 9 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p9);
		Participant p10 = new Participant("Participant 10 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p10);
		Participant p11 = new Participant("Participant 11 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p11);
		Participant p12 = new Participant("Participant 12 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p12);
		Participant p13 = new Participant("Participant 13 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p13);
		Participant p14 = new Participant("Participant 14 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p14);
		Participant p15 = new Participant("Participant 15 with very very very very very very very very very very very very long name", "", false, false);
		participants.add(p15);

		Poll poll = new Poll();
		poll.setOptions(options);
		poll.setParticipants(participants);
		poll.setQuestion(question);
		poll.setTerminated(false);
		poll.setStartTime(System.currentTimeMillis());

		return poll;
	}
}
