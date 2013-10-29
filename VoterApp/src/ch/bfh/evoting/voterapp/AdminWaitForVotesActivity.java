package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
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
public class AdminWaitForVotesActivity extends ListActivity implements OnClickListener {

	private Button btnStopPoll;
	private AlertDialog dialogBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_wait_for_votes);
		
		AndroidApplication.getInstance().setCurrentActivity(this);


		btnStopPoll = (Button) findViewById(R.id.button_stop_poll);
		btnStopPoll.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				AdminWaitForVotesActivity.super.onBackPressed();
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
		dialogBack.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wait_for_votes, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_wait), getString(R.string.help_text_wait) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item); 
	}

	@Override
	public void onClick(View view) {
		if (view == btnStopPoll){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Add the buttons
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent i = new Intent(BroadcastIntentTypes.stopVote);
					LocalBroadcastManager.getInstance(AdminWaitForVotesActivity.this).sendBroadcast(i);

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
			AlertDialog dialog = builder.create();
			dialog.show();

		}

	}
	
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	

	
}
