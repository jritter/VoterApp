package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import android.os.Bundle;
import android.app.Activity;
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
 * Class displaying the activity that allows the user to check if the poll is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollActivity extends Activity implements OnClickListener {

	private Poll poll;
	
	private Button btnStartPollPeriod;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		setContentView(R.layout.activity_review_poll);
		setupActionBar();
		
		if(getResources().getBoolean(R.bool.display_bottom_bar) == false){
	        findViewById(R.id.layout_bottom_bar).setVisibility(View.GONE);
	    }
		
		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().getNetworkInterface().lockGroup();

		
		btnStartPollPeriod = (Button) findViewById(R.id.button_start_poll_period);
		btnStartPollPeriod.setOnClickListener(this);
		
		Intent intent = getIntent();
		Poll intentPoll = (Poll)intent.getSerializableExtra("poll");
		if(intentPoll!=null){
			poll = intentPoll;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review_poll, menu);
		return true;
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_review), getString(R.string.help_text_review) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
		case R.id.action_start_voteperiod:
			startVotePeriod();
		}
		return super.onOptionsItemSelected(item); 
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
	public void onClick(View view) {
		if (view == btnStartPollPeriod){
			startVotePeriod();			
		}
	}

	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
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
	
//	@Override
//	public void onBackPressed() {
//		//do nothing because we don't want that people access to an anterior activity
//	}
	
	private void startVotePeriod(){
		for(Participant p:poll.getParticipants().values()){
			if(!p.hasAcceptedReview()){
				Toast.makeText(this, R.string.toast_not_everybody_accepted, Toast.LENGTH_LONG).show();
				return;
			}
		}
		//Send start poll signal over the network
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		poll.setStartTime(System.currentTimeMillis());
		poll.setNumberOfParticipants(poll.getParticipants().values().size());
		
		if(isContainedInParticipants(AndroidApplication.getInstance().getNetworkInterface().getMyIpAddress())){
			Intent intent = new Intent(this, VoteActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, AdminWaitForVotesActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			startActivity(intent);
		}
	}
}
