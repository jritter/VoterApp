package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.WaitForVotesFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity show when the participant has already submitted her vote but other voters are still voting
 * @author Philémon von Bergen
 *
 */
public class WaitForVotesAdminActivity extends Activity implements OnClickListener {

	private Button btnStopPoll;
	private AlertDialog dialogBack;
	private AlertDialog stopPollDialog;
	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_admin_wait_for_votes);

		if(getResources().getBoolean(R.bool.display_bottom_bar) == false){
			findViewById(R.id.layout_bottom_bar).setVisibility(View.GONE);
		}

		AndroidApplication.getInstance().setCurrentActivity(this);

		btnStopPoll = (Button) findViewById(R.id.button_stop_poll);
		btnStopPoll.setOnClickListener(this);

		if(savedInstanceState!=null){
			poll = (Poll) savedInstanceState.getSerializable("poll");
		}
		Poll intentPoll = (Poll)this.getIntent().getSerializableExtra("poll");
		if(intentPoll!=null){
			poll = intentPoll;
		}

		//If participant was not included in the electorate, the VoteActivity was not displayed
		// and thus VoteService was not started, so we start it here
		if(!isVoteServiceRunning()){
			this.startService(new Intent(this, VoteService.class).putExtra("poll", poll));
		}

		FragmentManager fm = getFragmentManager();
		WaitForVotesFragment fragment = new WaitForVotesFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("poll", poll);
		fragment.setArguments(bundle);

		fm.beginTransaction().replace(R.id.fragment_container, fragment, "wait").commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("poll", poll);
	}

	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				startActivity(new Intent(WaitForVotesAdminActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
			}
		});

		builder.setTitle(R.string.dialog_title_back);
		builder.setMessage(this.getString(R.string.dialog_back_result));

		// Create the AlertDialog
		dialogBack = builder.create();

		dialogBack.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
				dialogBack.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
				dialogBack.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
			}
		});

		dialogBack.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.admin_wait_for_votes, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_wait), getString(R.string.help_text_wait_admin) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		case R.id.action_finish_vote:
			finishVote();
			return true;
		}
		return super.onOptionsItemSelected(item); 
	}

	@Override
	public void onClick(View view) {
		if (view == btnStopPoll){
			finishVote();
		}

	}


	/*--------------------------------------------------------------------------------------------
	 * Helper Methods
	--------------------------------------------------------------------------------------------*/

	private void finishVote(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent i = new Intent(BroadcastIntentTypes.stopVote);
				LocalBroadcastManager.getInstance(WaitForVotesAdminActivity.this).sendBroadcast(i);

				//Send stop signal over the network
				VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_STOP_POLL, null);
				AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		});

		builder.setTitle(R.string.dialog_title_stop_poll);
		builder.setMessage(R.string.dialog_stop_poll);

		// Create the AlertDialog
		stopPollDialog = builder.create();

		stopPollDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
				stopPollDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
				stopPollDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
			}
		});

		stopPollDialog.show();
	}

	/**
	 * Helper method checking if the vote service is running
	 * @return true if the service is running, false otherwise
	 */
	private boolean isVoteServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (VoteService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
