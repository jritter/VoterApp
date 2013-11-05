package ch.bfh.evoting.voterapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import ch.bfh.evoting.voterapp.adapters.ResultAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.DatabaseException;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;

/**
 * Activity displaying the results of a poll
 * 
 * @author Philémon von Bergen
 * 
 */
public class DisplayResultActivity extends Activity implements OnClickListener {

	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;
	private PendingIntent pendingIntent;

	private int pollId;
	private boolean saveToDbNeeded;
	private Poll poll;

	private Button btnRedo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getResources().getBoolean(R.bool.portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_display_result);
		setupActionBar();

		if (getResources().getBoolean(R.bool.display_bottom_bar) == false) {
			findViewById(R.id.layout_bottom_bar).setVisibility(View.GONE);
		}

		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		if (AndroidApplication.getInstance().isAdmin()) {
			AndroidApplication.getInstance().getNetworkInterface()
					.unlockGroup();
		}

		ListView lv = (ListView) findViewById(android.R.id.list);

		// Get the data in the intent
		Poll intentPoll = (Poll) this.getIntent().getSerializableExtra("poll");
		if (intentPoll != null) {
			this.poll = intentPoll;
		}

		saveToDbNeeded = this.getIntent().getBooleanExtra("saveToDb", false);
		if (poll.getId() >= 0) {
			pollId = poll.getId();
		} else {
			pollId = -1;
		}

		if (!AndroidApplication.getInstance().isAdmin() && saveToDbNeeded) {
			LinearLayout ll = (LinearLayout) findViewById(R.id.layout_bottom_bar);
			ll.setVisibility(View.GONE);

		} else {

			// Set a listener on the redo button
			btnRedo = (Button) findViewById(R.id.button_redo_poll);
			btnRedo.setOnClickListener(this);
			// Poll is just finished
			if (saveToDbNeeded) {
				btnRedo.setText(R.string.redo_poll);
			} else {
				btnRedo.setText(R.string.clone_poll);
			}
		}

		lv.setAdapter(new ResultAdapter(this, poll));

		// Save the poll to the DB if needed
		if (saveToDbNeeded) {
			try {
				if (pollId >= 0) {
					PollDbHelper.getInstance(this).updatePoll(pollId, poll);
				} else {
					int newPollId = (int) PollDbHelper.getInstance(this)
							.savePoll(poll);
					this.pollId = newPollId;
					poll.setId(newPollId);
				}
			} catch (DatabaseException e) {
				Log.e(this.getClass().getSimpleName(),
						"DB error: " + e.getMessage());
				e.printStackTrace();
			}
		}

		// Is NFC available on this device?
		nfcAvailable = this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_NFC);

		if (nfcAvailable) {

			nfcAdapter = NfcAdapter.getDefaultAdapter(this);

			if (nfcAdapter.isEnabled()) {

				// Setting up a pending intent that is invoked when an NFC tag
				// is tapped on the back
				pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
			} else {
				nfcAvailable = false;
			}
		}

	}

	@Override
	public void onBackPressed() {
		if (!this.saveToDbNeeded) {
			super.onBackPressed();
		} else {
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (nfcAvailable) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setVoteRunning(false);
		AndroidApplication.getInstance().setCurrentActivity(this);
		
		if (nfcAdapter != null && nfcAdapter.isEnabled()) {
			nfcAvailable = true;
		}

		// make sure that this activity is the first one which can handle the
		// NFC tags
		if (nfcAvailable) {
			nfcAdapter.enableForegroundDispatch(this, pendingIntent,
					Utility.getNFCIntentFilters(), null);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		try {
			super.onSaveInstanceState(savedInstanceState);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		savedInstanceState.putSerializable("poll", poll);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		poll = (Poll) savedInstanceState.getSerializable("poll");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			if (saveToDbNeeded) {
				Intent i = new Intent(DisplayResultActivity.this,
						MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TASK
						| Intent.FLAG_ACTIVITY_NEW_TASK);

				startActivity(i);

			} else {
				// if consulting an archive
				startActivity(new Intent(this,
						ListTerminatedPollsActivity.class));
			}
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance(
					getString(R.string.help_title_display_results),
					getString(R.string.help_text_display_results));
			hdf.show(getFragmentManager(), "help");
			return true;
		case R.id.action_redo_vote:
			redoVote();
			return true;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_results, menu);

		if (saveToDbNeeded) {
			menu.findItem(R.id.action_redo_vote).setTitle(R.string.redo_poll);
			menu.findItem(R.id.action_redo_vote).setVisible(
					AndroidApplication.getInstance().isAdmin());
		} else {
			menu.findItem(R.id.action_redo_vote).setTitle(R.string.clone_poll);
		}

		if (getResources().getBoolean(R.bool.display_bottom_bar)) {
			menu.findItem(R.id.action_redo_vote).setVisible(false);
		}

		return true;
	}

	@Override
	public void onClick(View view) {
		if (view == btnRedo) {
			redoVote();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	public void onNewIntent(Intent intent) {
		Intent broadcastIntent = new Intent(BroadcastIntentTypes.nfcTagTapped);
		broadcastIntent.putExtra(NfcAdapter.EXTRA_TAG, intent.getParcelableExtra(NfcAdapter.EXTRA_TAG));
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
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
	 * Method called when the admin want to repeat the poll directly after its
	 * finition or if someone want to clone a poll
	 */
	private void redoVote() {
		Poll newPoll = new Poll();
		newPoll.setQuestion(poll.getQuestion());
		List<Option> newOptions = new ArrayList<Option>();
		for (Option op : poll.getOptions()) {
			Option newOp = new Option();
			newOp.setText(op.getText());
			newOptions.add(newOp);
		}
		newPoll.setOptions(newOptions);

		PollDbHelper pollDbHelper = PollDbHelper
				.getInstance(DisplayResultActivity.this);
		try {
			int pollId = (int) pollDbHelper.savePoll(newPoll);
			Intent i = new Intent(DisplayResultActivity.this,
					PollDetailActivity.class);
			i.putExtra("pollid", pollId);
			startActivity(i);
		} catch (DatabaseException e) {
			for (int i = 0; i < 2; i++)
				Toast.makeText(DisplayResultActivity.this,
						getString(R.string.redo_impossible), Toast.LENGTH_LONG)
						.show();
			e.printStackTrace();
		}
	}

}
