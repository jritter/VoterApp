package ch.bfh.evoting.voterapp;


import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.HelpDialogFragment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollVoterActivity extends Activity {

	private BroadcastReceiver pollReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplication.getInstance().setCurrentActivity(this);
		setContentView(R.layout.activity_review_poll_voter);

//		final Button btn_validate_review = (Button) findViewById(R.id.button_validate_review);
//
//		btn_validate_review.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ACCEPT_REVIEW, ""));
//				((LinearLayout)btn_validate_review.getParent()).setVisibility(View.GONE);
//			}
//		});

		pollReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
//				((LinearLayout)btn_validate_review.getParent()).setVisibility(View.VISIBLE);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(pollReceiver, new IntentFilter(BroadcastIntentTypes.pollToReview));
	}
	
	@Override
	protected void onDestroy() {
		AndroidApplication.getInstance().setCurrentActivity(null);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(pollReceiver);
		super.onDestroy();
	}

//	@Override
//	public void onBackPressed() {
//		//do nothing because we don't want that people access to an anterior activity
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_review), getString(R.string.help_text_review) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	protected void onPause() {
		AndroidApplication.getInstance().setCurrentActivity(null);
		super.onPause();
	}
	
}
