package ch.bfh.evoting.voterapp.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.adapters.ReviewPollAdapter;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

/**
 * Fragment displaying the review of a poll
 * 
 */
public class PollReviewFragment extends Fragment {

	private Poll poll;

	private BroadcastReceiver reviewAcceptsReceiver;
	private ReviewPollAdapter adapter;


	private String sender;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.poll = (Poll)this.getArguments().getSerializable("poll");
		this.sender = this.getArguments().getString("sender");
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if(poll.getParticipants().containsKey(sender))
			poll.getParticipants().get(sender).setHasAcceptedReview(true);

		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_poll_review, container,
				false);

		adapter = new ReviewPollAdapter(getActivity(), poll);

		ListView lv = (ListView) v.findViewById(android.R.id.list);
		lv.setAdapter(adapter);

		adapter.notifyDataSetChanged();

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
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(PollReviewFragment.this.getActivity()).unregisterReceiver(reviewAcceptsReceiver);
		super.onDestroy();
	}

}
