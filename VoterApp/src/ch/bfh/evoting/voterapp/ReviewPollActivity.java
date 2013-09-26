package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.votinglib.VoteActivity;
import ch.bfh.evoting.votinglib.entities.Poll;
import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
import ch.bfh.evoting.votinglib.util.Utility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollActivity extends Activity {

	private ListView lvOptions = null;
	private ListView lvParticipants = null;
	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_poll);

		poll = (Poll)getIntent().getSerializableExtra("poll");

		TextView question = (TextView) findViewById (R.id.textview_poll_question);
		question.setText(poll.getQuestion());

		lvOptions = (ListView) findViewById (R.id.listview_list_options);
		lvParticipants = (ListView) findViewById (R.id.listview_list_participants);

		PollOptionListAdapter polAdapter = new PollOptionListAdapter(this, R.layout.list_item_option_poll, poll.getOptions());
		PollParticipantListAdapter pplAdapter = new PollParticipantListAdapter(this, R.layout.list_item_participant_poll, poll.getParticipants());

		lvOptions.setAdapter(polAdapter);
		lvParticipants.setAdapter(pplAdapter);
		
		//Adapt list view size
		Utility.setListViewHeightBasedOnChildren(lvOptions, false);
		Utility.setListViewHeightBasedOnChildren(lvParticipants, false);
		//Reposition other view below the new sized lists
		TextView tv = (TextView)this.findViewById(R.id.textview_poll_participants);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)tv.getLayoutParams();
		lp.addRule(RelativeLayout.BELOW, R.id.listview_list_options);
		tv.setLayoutParams(lp);
		((ScrollView)this.findViewById(R.id.scrollview_review_layout)).smoothScrollTo(0, 0);


		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Intent i = new Intent(ReviewPollActivity.this, VoteActivity.class);
				i.putExtra("poll", (Serializable) poll);
				i.putExtra("isAdmin", false);
				startActivity(i);

			}
		}, new IntentFilter(BroadcastIntentTypes.goToVote));

		simulate();
	}

	//TODO remove, only for simulation
	private void simulate(){
		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {


				SystemClock.sleep(8000);

				//Send participants
				Intent intent = new Intent(BroadcastIntentTypes.goToVote);
				LocalBroadcastManager.getInstance(ReviewPollActivity.this).sendBroadcast(intent);


				return null;
			}
		}.execute();
	}

}
