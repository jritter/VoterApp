package ch.bfh.evoting.voterapp.fragment;

import java.io.Serializable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.MainActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.VoteActivity;
import ch.bfh.evoting.voterapp.adapters.ReviewPollAdapter;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

/**
 * Fragment displaying the review of a poll
 * 
 */
public class PollReviewFragment extends ListFragment {

	private Poll poll;

	private BroadcastReceiver pollReceiver;
	private BroadcastReceiver reviewAcceptsReceiver;
	private ReviewPollAdapter adapter;

	private BroadcastReceiver startVoteReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState !=null){
			Poll intentPoll = (Poll)savedInstanceState.getSerializable("poll");
			if(intentPoll!=null){
				this.poll = intentPoll;
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Poll intentPoll = (Poll)getActivity().getIntent().getSerializableExtra("poll");
		if(intentPoll!=null){
			this.poll = intentPoll;
		}
		String sender = getActivity().getIntent().getStringExtra("sender");
		poll.getParticipants().get(sender).setHasAcceptedReview(true);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_poll_review, container,
				false);

		adapter = new ReviewPollAdapter(getActivity(), poll);

		ListView lv = (ListView) v.findViewById(android.R.id.list);
		lv.setAdapter(adapter);

		adapter.notifyDataSetChanged();

		if(!AndroidApplication.getInstance().isAdmin()){
			//register the startvote signal receiver
			startVoteReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {

					if(isContainedInParticipants(AndroidApplication.getInstance().getNetworkInterface().getMyIpAddress())){
						Intent i = new Intent(PollReviewFragment.this.getActivity(), VoteActivity.class);
						poll.setStartTime(System.currentTimeMillis());
						poll.setNumberOfParticipants(poll.getParticipants().values().size());
						i.putExtra("poll", (Serializable) poll);
						startActivity(i);
						LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(this);
						LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(pollReceiver);
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						// Add the buttons
						builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AndroidApplication.getInstance().getNetworkInterface().disconnect();
								startActivity(new Intent(PollReviewFragment.this.getActivity(), MainActivity.class));
							}
						});

						builder.setTitle(R.string.not_included_title);
						builder.setMessage(R.string.not_included);

						// Create the AlertDialog
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
			};
			LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(startVoteReceiver, new IntentFilter(BroadcastIntentTypes.startVote));

			//broadcast receiving the poll if it was modified
			pollReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {

					poll = (Poll)intent.getSerializableExtra("poll");
					//Poll is not in the DB, so reset the id
					poll.setId(-1);
					String sender = intent.getStringExtra("sender");
					Log.e("PollReviewFragment", sender);
					poll.getParticipants().get(sender).setHasAcceptedReview(true);
					adapter.notifyDataSetChanged();
				}
			};
			LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(pollReceiver, new IntentFilter(BroadcastIntentTypes.pollToReview));
		}



		//broadcast receiving the poll review acceptations
		reviewAcceptsReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String participantAccept = intent.getStringExtra("participant");
				if(poll.getParticipants().get(participantAccept)!=null)
					poll.getParticipants().get(participantAccept).setHasAcceptedReview(true);
				adapter.notifyDataSetChanged();
			}
		};
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(reviewAcceptsReceiver, new IntentFilter(BroadcastIntentTypes.acceptReview));

		return v;

		//		ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
		//        co.hideOnClickOutside = true;
		//
		//        // The following code will reposition the OK button to the left.
		//// RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		//// lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		//// lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		//// int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
		//// lps.setMargins(margin, margin, margin, margin);
		//// co.buttonLayoutParams = lps;
		//
		//        sv = ShowcaseView.insertShowcaseView(R.id.buttonBlocked, this, R.string.showcase_main_title, R.string.showcase_main_message, co);
		//        sv.setOnShowcaseEventListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(startVoteReceiver);
		LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(pollReceiver);
		LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(reviewAcceptsReceiver);
		super.onDestroy();
	}

	private boolean isContainedInParticipants(String ipAddress){
		for(Participant p : poll.getParticipants().values()){
			if(p.getIpAddress().equals(ipAddress)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

}
