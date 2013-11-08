package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ch.bfh.evoting.voterapp.adapters.VoteOptionListAdapter;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity displaying the vote options in the voting phase
 * @author Philémon von Bergen
 *
 */
public class VoteActivity extends Activity {
	
	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;

	private Poll poll;

	private VoteOptionListAdapter volAdapter;
	private boolean scrolled = false;
	private boolean demoScrollDone = false;

	private ListView lvChoices;
	private BroadcastReceiver stopReceiver;
	private PendingIntent pendingIntent;

	private AlertDialog dialogBack;
	private BroadcastReceiver updateVoteReceiver;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getResources().getBoolean(R.bool.portrait_only)){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		AndroidApplication.getInstance().setCurrentActivity(this);

		setContentView(R.layout.activity_vote);
		
		lvChoices = (ListView)findViewById(R.id.listview_choices);

		//Get the data in the intent
		Intent intent = this.getIntent();
		Poll intentPoll = (Poll) intent.getSerializableExtra("poll");
		if(intentPoll!=null){
			poll = intentPoll;
		}

		//Set the question text
		TextView tvQuestion = (TextView)findViewById(R.id.textview_vote_poll_question);
		tvQuestion.setText(poll.getQuestion());

		//create the list of vote options
		volAdapter = new VoteOptionListAdapter(this, R.layout.list_item_vote, poll.getOptions());
		lvChoices.setAdapter(volAdapter);

		lvChoices.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				//Check if the last view is visible
				if (++firstVisibleItem + visibleItemCount > totalItemCount && demoScrollDone) {
					scrolled=true;
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}


		});

		lvChoices.post(new Runnable(){

			@Override
			public void run() {
				Log.e("VoteActivity", "Scroll: LastVisible item="+lvChoices.getLastVisiblePosition()+" lvChoices.getCount()-2="+(lvChoices.getCount()-2));
				if(lvChoices.getLastVisiblePosition() < lvChoices.getCount()-1){

					//animate scroll
					new AsyncTask<Object, Object, Object>(){

						@Override
						protected Object doInBackground(Object... params) {
							SystemClock.sleep(500);
							Log.d("VoteActivity", "Doing demo scroll");
							lvChoices.smoothScrollToPositionFromTop(lvChoices.getAdapter().getCount(), 0, 1500);
							SystemClock.sleep(1550);
							lvChoices.smoothScrollToPositionFromTop(0, 0, 1500);
							scrolled = false;
							SystemClock.sleep(1550);
							demoScrollDone = true;
							return null;
						}

					}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				} else {
					Log.d("VoteActivity", "Demo scroll not needed");
					scrolled = true;
				}
			}

		});


		//Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver(){

			private int numberOfVotes = 0;
			@Override
			public void onReceive(Context arg0, Intent intent) {
				stopService(new Intent(VoteActivity.this, VoteService.class));
				//go through compute result and set percentage result
				List<Option> options = poll.getOptions();
				for(Option option : options){
					numberOfVotes += option.getVotes();	
				}
				for(Option option : options){
					if(numberOfVotes!=0){
						option.setPercentage(option.getVotes()*100/numberOfVotes);
					} else {
						option.setPercentage(0);
					}
				}

				poll.setTerminated(true);

				//start to result activity
				Intent i = new Intent(VoteActivity.this, DisplayResultActivity.class);
				i.putExtra("poll", (Serializable)poll);
				i.putExtra("saveToDb", true);
				startActivity(i);
				LocalBroadcastManager.getInstance(VoteActivity.this).unregisterReceiver(this);
				LocalBroadcastManager.getInstance(VoteActivity.this).unregisterReceiver(updateVoteReceiver);
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));

		//Register a BroadcastReceiver on new incoming vote events
		//TODO see if needed after simulation
		updateVoteReceiver = new BroadcastReceiver(){
			@SuppressWarnings("unchecked")
			@Override
			public void onReceive(Context arg0, Intent intent) {
				poll.setOptions((List<Option>)intent.getSerializableExtra("options"));
				poll.setParticipants((Map<String,Participant>)intent.getSerializableExtra("participants"));
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(updateVoteReceiver, new IntentFilter(BroadcastIntentTypes.newIncomingVote));

		this.startService(new Intent(this, VoteService.class).putExtra("poll", poll));
		
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
	protected void onPause() {
		super.onPause();
		if (nfcAvailable) {
			nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		poll = (Poll)savedInstanceState.getSerializable("poll");
	}

	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				startActivity(new Intent(VoteActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
			}
		});

		builder.setTitle(R.string.dialog_title_back);
		builder.setMessage(this.getString(R.string.dialog_back));

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
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_vote), getString(R.string.help_text_vote) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vote, menu);
		return true;
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

	/**
	 * Method called when cast button is clicked
	 */
	public void castBallot(){

		Option selectedOption = volAdapter.getItemSelected();

		if(selectedOption!=null){
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, selectedOption));
		} else {
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, null));
		}

		//Start activity waiting for other participants to vote
		//If is admin, returns to admin app wait activity
		if(AndroidApplication.getInstance().isAdmin()){
			Intent i = new Intent(this, WaitForVotesAdminActivity.class);
			i.putExtra("poll", (Serializable)poll);
			i.putExtra("votes", VoteService.getInstance().getVotes());
			startActivity(i);
		} else {
			Intent intent = new Intent(this, WaitForVotesVoterActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			intent.putExtra("votes", VoteService.getInstance().getVotes());
			startActivity(intent);
		}
	}

	/**
	 * Indicate if the user has scrolled over all possible options
	 * @return true if user has scrolled, false otherwise
	 */
	public boolean getScrolled(){
		return scrolled;
	}


}
