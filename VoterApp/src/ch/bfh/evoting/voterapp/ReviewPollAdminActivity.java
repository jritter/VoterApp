package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.PollReviewFragment;
import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Class displaying the activity that allows the user to check if the poll is
 * correct
 * 
 * @author Philémon von Bergen
 * 
 */
public class ReviewPollAdminActivity extends Activity implements OnClickListener {

	private Poll poll;

	private Button btnStartPollPeriod;

	private String sender;

	private PollReviewFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getBoolean(R.bool.portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_review_poll);
		setupActionBar();

		if (getResources().getBoolean(R.bool.display_bottom_bar) == false) {
			findViewById(R.id.layout_bottom_bar).setVisibility(View.GONE);
		}

		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().getNetworkInterface().lockGroup();

		btnStartPollPeriod = (Button) findViewById(R.id.button_start_poll_period);
		btnStartPollPeriod.setOnClickListener(this);

		if(savedInstanceState!=null){
			poll = (Poll) savedInstanceState.getSerializable("poll");
			sender = savedInstanceState.getString("sender");
		}
		
		Poll intentPoll = (Poll) getIntent().getSerializableExtra("poll");
		if (intentPoll != null) {
			poll = intentPoll;
			sender = getIntent().getStringExtra("sender");
		}
		
		FragmentManager fm = getFragmentManager();
		fragment = new PollReviewFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("poll", poll);
		bundle.putString("sender", sender);
		fragment.setArguments(bundle);

		fm.beginTransaction().replace(R.id.fragment_container, fragment, "review").commit();

	}
	
	@Override
	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
		savedInstanceState.putString("sender", sender);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		poll = (Poll) savedInstanceState.getSerializable("poll");
		sender = savedInstanceState.getString("sender");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review_poll, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, ElectorateActivity.class);
			i.putExtra("poll", (Serializable) poll);
			NavUtils.navigateUpTo(this, i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
					getString(R.string.help_title_review),
					getString(R.string.help_text_review_admin));
			hdf.show(getFragmentManager(), "help");
			return true;
		case R.id.action_start_voteperiod:
			startVotePeriod();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		if (view == btnStartPollPeriod) {
			startVotePeriod();
		}
	}

	
	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/
	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * Indicate if the peer identified with the given string is contained in the list of participants
	 * @param uniqueId identifier of the peer
	 * @return true if it is contained in the list of participants, false otherwise
	 */
	private boolean isContainedInParticipants(String uniqueId) {
		for (Participant p : poll.getParticipants().values()) {
			if (p.getUniqueId().equals(uniqueId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Start the vote phase
	 */
	private void startVotePeriod() {
		for (Participant p : poll.getParticipants().values()) {
			if (!p.hasAcceptedReview()) {
				for(int i=0; i < 2; i++)
					Toast.makeText(this, R.string.toast_not_everybody_accepted,	Toast.LENGTH_LONG).show();
				return;
			}
		}
		// Send start poll signal over the network
		VoteMessage vm = new VoteMessage(
				VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		poll.setStartTime(System.currentTimeMillis());
		poll.setNumberOfParticipants(poll.getParticipants().values().size());

		if (isContainedInParticipants(AndroidApplication.getInstance()
				.getNetworkInterface().getMyUniqueId())) {
			Intent intent = new Intent(this, VoteActivity.class);
			intent.putExtra("poll", (Serializable) poll);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, WaitForVotesAdminActivity.class);
			intent.putExtra("poll", (Serializable) poll);
			startActivity(intent);
		}
	}
}
