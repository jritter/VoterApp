package ch.bfh.evoting.voterapp.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.bfh.evoting.voterapp.DisplayResultActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.adapters.WaitParticipantListAdapter;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment displaying the progress of the votes received
 * 
 */
public class WaitForVotesFragment extends ListFragment {

	private Poll poll;
	private int progressBarMaxValue = 0;
	private Map<String,Participant> participants;
	private ProgressBar pb;
	private WaitParticipantListAdapter wpAdapter;
	private TextView tvCastVotes;

	private BroadcastReceiver updateVoteReceiver;
	private BroadcastReceiver showNextActivityListener;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		this.poll = (Poll)this.getArguments().getSerializable("poll");
		//Register a BroadcastReceiver on new incoming vote events
		updateVoteReceiver = new BroadcastReceiver(){
			@SuppressWarnings("unchecked")
			@Override
			public void onReceive(Context arg0, Intent intent) {
				poll.setOptions((List<Option>)intent.getSerializableExtra("options"));
				poll.setParticipants((Map<String,Participant>)intent.getSerializableExtra("participants"));
				updateStatus(intent.getIntExtra("votes", 0));
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(updateVoteReceiver, new IntentFilter(BroadcastIntentTypes.newIncomingVote));

		// Subscribing to the showNextActivity request to show ResultActivity
		showNextActivityListener = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this);

				//start Review activity
				Intent i = new Intent(context, DisplayResultActivity.class);
				i.putExtras(intent.getExtras());
				i.putExtra("saveToDb", true);
				startActivity(i);
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(showNextActivityListener, new IntentFilter(BroadcastIntentTypes.showResultActivity));

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(updateVoteReceiver);
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(showNextActivityListener);
		super.onDetach();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_wait_for_votes, container,
				false);

		Intent intent = getActivity().getIntent();
		Poll intentPoll = (Poll)intent.getSerializableExtra("poll");
		if(intentPoll!=null){
			poll = intentPoll;
		}

		participants = poll.getParticipants();

		//Create the adapter for the ListView
		wpAdapter = new WaitParticipantListAdapter(this.getActivity(), R.layout.list_item_participant_wait, new ArrayList<Participant>(participants.values()));
		this.setListAdapter(wpAdapter);

		//Initialize the progress bar 
		pb=(ProgressBar)v.findViewById(R.id.progress_bar_votes);
		progressBarMaxValue = pb.getMax();

		tvCastVotes = (TextView)v.findViewById(R.id.textview_cast_votes);
		tvCastVotes.setText(getString(R.string.cast_votes, 0, participants.size()));

		return v;
	}


	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/

	/**
	 * Update the state of the progress bar, change the image of the participants when they have voted
	 * and start the activity which displays the results
	 * @param progress
	 */
	private void updateStatus(int numberOfReceivedVotes){
		//update progress bar and participants list
		int progress = 0;
		if(poll.getParticipants().size()!=0){
			progress = numberOfReceivedVotes*progressBarMaxValue/poll.getParticipants().size();
		}

		pb.setProgress(progress);
		wpAdapter.clear();
		wpAdapter.addAll(poll.getParticipants().values());
		wpAdapter.notifyDataSetChanged();
		tvCastVotes.setText(getString(R.string.cast_votes, numberOfReceivedVotes, participants.size()));

	}
}
