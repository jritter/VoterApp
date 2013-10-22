package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.bfh.evoting.voterapp.adapters.AdminNetworkParticipantListAdapter;
import ch.bfh.evoting.voterapp.adapters.NetworkParticipantListAdapter;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.IPAddressComparator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Class displaying the activity that allows the administrator to select which participants to include in the electorate
 * @author Philémon von Bergen
 *
 */
public class ElectorateActivity extends Activity implements OnClickListener {

	private Poll poll;
	private Map<String,Participant> participants;
	private AdminNetworkParticipantListAdapter npa;
	private boolean active;

	private Button btnNext;
	private ListView lvElectorate;

	private AsyncTask<Object, Object, Object> resendElectorate;
	private BroadcastReceiver participantsDiscoverer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_electorate);
		setupActionBar();
		
		btnNext = (Button) findViewById(R.id.button_next);
		btnNext.setOnClickListener(this);

		lvElectorate = (ListView) findViewById(R.id.listview_electorate);

		//if extra is present, it has priority
		Intent intent = getIntent();
		Poll serializedPoll = (Poll)intent.getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}

		participants = AndroidApplication.getInstance().getNetworkInterface().getConversationParticipants();
		npa = new AdminNetworkParticipantListAdapter(this, R.layout.list_item_participant_network_admin, new ArrayList<Participant>(participants.values()));
		lvElectorate.setAdapter(npa);

		// Subscribing to the participantStateUpdate events
		participantsDiscoverer = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateFromNetwork();

				//Send the updated list of participants in the network over the network
				VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ELECTORATE, (Serializable)participants);
				AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(participantsDiscoverer, new IntentFilter(BroadcastIntentTypes.participantStateUpdate));

		startPeriodicSend();


		//Send the list of participants in the network over the network
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ELECTORATE, (Serializable)participants);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

	}

	@Override
	protected void onNewIntent(Intent intent) {

		//if extra is present, it has priority on the saved poll
		Poll serializedPoll = (Poll)intent.getSerializableExtra("poll");
		if(serializedPoll!=null){
			poll = serializedPoll;
		}
		super.onNewIntent(intent);
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
			Intent i = new Intent(this, NetworkInformationsActivity.class);
			startActivity(i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_electorate), getString(R.string.help_text_electorate) );
			hdf.show( getFragmentManager( ), "help" );
			return true;

		}
		return super.onOptionsItemSelected(item); 
	}



	@Override
	protected void onResume() {

		AndroidApplication.getInstance().setCurrentActivity(this);

		LocalBroadcastManager.getInstance(this).registerReceiver(participantsDiscoverer, new IntentFilter(BroadcastIntentTypes.participantStateUpdate));

		updateFromNetwork();

		//Send the updated list of participants in the network over the network
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ELECTORATE, (Serializable)participants);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		startPeriodicSend();

		super.onResume();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.electorate, menu);
		return true;
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onClick(View view) {
		if (view == btnNext){
			Map<String,Participant> finalParticipants = new TreeMap<String,Participant>(new IPAddressComparator());
			for(Participant p: participants.values()){
				if(p.isSelected()){
					finalParticipants.put(p.getIpAddress(),p);
				}
			}
			if(finalParticipants.size()<2){
				Toast.makeText(this, R.string.toast_not_enough_participant_selected, Toast.LENGTH_SHORT).show();
				return;
			}
			poll.setParticipants(finalParticipants);

			//if this is a modification of the poll, reset all the acceptations received
			for(Participant p: poll.getParticipants().values()){
				p.setHasAcceptedReview(false);
			}

			active = false;
			resendElectorate.cancel(true);

			//Send poll to other participants
			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)poll);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

			Intent intent = new Intent(this, ReviewPollActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyIpAddress());
			startActivity(intent);
			LocalBroadcastManager.getInstance(this).unregisterReceiver(participantsDiscoverer);
		}	
	}

	//	@Override
	//	public void onBackPressed() {
	//		//do nothing because we don't want that people access to an anterior activity
	//	}


	private void updateFromNetwork(){
		Map<String,Participant> newReceivedMapOfParticipants = AndroidApplication.getInstance().getNetworkInterface().getConversationParticipants();
		for(String ip : newReceivedMapOfParticipants.keySet()){
			if(!participants.containsKey(ip)){
				//Participant is not already know
				//we add it
				participants.put(ip, newReceivedMapOfParticipants.get(ip));
			} else if (!participants.get(ip).getIdentification().equals(newReceivedMapOfParticipants.get(ip).getIdentification())) {
				//There is already a participant registered with this ip,
				//but the identification in the new set is not the same
				//so we delete the old and put the new
				participants.remove(ip);
				participants.put(ip, newReceivedMapOfParticipants.get(ip));
			}
		}

		List<String> toRemove = new ArrayList<String>();
		for(String ip : participants.keySet()){
			if(!newReceivedMapOfParticipants.containsKey(ip)){
				//participant is no more in the new set
				//we delete it
				toRemove.add(ip);
			}
		}
		for(String ip : toRemove){
			participants.remove(ip);
		}

		npa.clear();
		npa.addAll(participants.values());
		npa.notifyDataSetChanged();
	}

	private void startPeriodicSend(){
		active = true;

		resendElectorate = new AsyncTask<Object, Object, Object>(){

			@Override
			protected Object doInBackground(Object... arg0) {

				while(active){
					//Send the list of participants in the network over the network
					VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ELECTORATE, (Serializable)participants);
					AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
					SystemClock.sleep(5000);

				}
				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

}

