package ch.bfh.evoting.voterapp.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.bfh.evoting.voterapp.DisplayResultActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.VoteActivity.VoteService;
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
	private int votesReceived = 0;

	private BroadcastReceiver stopReceiver;
	private BroadcastReceiver updateVoteReceiver;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		if(savedInstanceState!=null){
			poll = (Poll)savedInstanceState.getSerializable("poll");
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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

		//TODO for demo only
		votesReceived = 0;

		//Register a BroadcastReceiver on new incoming vote events
		//TODO see if needed after simulation
		updateVoteReceiver = new BroadcastReceiver(){
			@SuppressWarnings("unchecked")
			@Override
			public void onReceive(Context arg0, Intent intent) {
				poll.setOptions((List<Option>)intent.getSerializableExtra("options"));
				poll.setParticipants((Map<String,Participant>)intent.getSerializableExtra("participants"));
				votesReceived=intent.getIntExtra("votes", 0);
				updateStatus(intent.getIntExtra("votes", 0), false);
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(updateVoteReceiver, new IntentFilter(BroadcastIntentTypes.newIncomingVote));

		//Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent intent) {
				updateStatus(votesReceived, true);
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));

		return v;
	}

	/**
	 * Update the state of the progress bar, change the image of the participants when they have voted
	 * and start the activity which displays the results
	 * @param progress
	 * @param stopOrder order sent from admin to stop the poll
	 */
	private void updateStatus(int numberOfReceivedVotes, boolean stopOrder){
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


		if(progress>=100 || stopOrder){
			this.getActivity().stopService(new Intent(this.getActivity(), VoteService.class));
			//go through compute result and set percentage result
			List<Option> options = poll.getOptions();
			for(Option option : options){
				Log.e("WaitForVotesFRAGMENT","Number of votes is "+numberOfReceivedVotes);
				if(numberOfReceivedVotes!=0){
					option.setPercentage(option.getVotes()*100/numberOfReceivedVotes);
				} else {
					option.setPercentage(0);
				}
			}

			poll.setTerminated(true);

			//start to result activity
			Intent intent = new Intent(this.getActivity(), DisplayResultActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			intent.putExtra("saveToDb", true);
			startActivity(intent);
			LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(stopReceiver);
			LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(updateVoteReceiver);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

}
