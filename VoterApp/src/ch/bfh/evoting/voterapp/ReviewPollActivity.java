package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.util.HelpDialogFragment;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.util.Log;
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
		setContentView(R.layout.activity_review_poll);
		setupActionBar();
		
		btnStartPollPeriod = (Button) findViewById(R.id.button_start_poll_period);
		btnStartPollPeriod.setOnClickListener(this);
		
		Intent intent = getIntent();
		poll = (Poll)intent.getSerializableExtra("poll");

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
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_review), getString(R.string.help_text_review) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
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

	
//	@Override
//	public void onBackPressed() {
//		//do nothing because we don't want that people access to an anterior activity
//	}
}
