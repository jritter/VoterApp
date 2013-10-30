package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.List;

import ch.bfh.evoting.voterapp.adapters.VoteOptionListAdapter;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

	//TODO remove static when no more needed
	static private Poll poll;
	private VoteOptionListAdapter volAdapter;
	private boolean scrolled = false;
	private boolean demoScrollDone = false;

	private ListView lvChoices;
	private BroadcastReceiver stopReceiver;

	private AlertDialog dialogBack;

	static Context ctx;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplication.getInstance().setCurrentActivity(this);

		setContentView(R.layout.activity_vote);

		ctx=this;

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
				Log.e("VoteActivity", "Scroll: firstVisible item="+firstVisibleItem+" visibleItemCount="+visibleItemCount+" totalItemCount="+totalItemCount);
				Log.e("VoteActivity", "DemoScrollDone: "+demoScrollDone +" Scrolled: "+ scrolled);
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
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));


		this.startService(new Intent(this, VoteService.class));
	}


	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				VoteActivity.super.onBackPressed();
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

	/**
	 * Method called when cast button is clicked
	 */
	public void castBallot(){

		Option selectedOption = volAdapter.getItemSelected();

		if(selectedOption!=null){
			Log.e("vote", "Voted "+selectedOption.getText());
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, selectedOption));
		} else {
			Log.e("vote", "Voted null");
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, null));
		}

		//Start activity waiting for other participants to vote
		//If is admin, returns to admin app wait activity
		if(AndroidApplication.getInstance().isAdmin()){
			Intent i = new Intent(this, AdminWaitForVotesActivity.class);
			i.putExtra("poll", (Serializable)poll);
			i.putExtra("votes", VoteService.getInstance().getVotes());
			startActivity(i);
		} else {
			Intent intent = new Intent(this, WaitForVotesActivity.class);
			intent.putExtra("poll", (Serializable)poll);
			intent.putExtra("votes", VoteService.getInstance().getVotes());
			startActivity(intent);
		}
	}

	public boolean getScrolled(){
		return scrolled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.help){
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


	//TODO remove: only for simulation
	public static class VoteService extends Service{


		boolean doWork = true;
		BroadcastReceiver voteReceiver;
		AsyncTask<Object, Object, Object> sendVotesTask;
		private int votesReceived = 0;
		private static VoteService instance;

		@Override
		public void onCreate() {
			instance = this;
			super.onCreate();
		}

		@Override
		public void onDestroy() {
			Log.e("VoteService", "Destroyed");
			reset();
			super.onDestroy();
		}

		private void reset(){
			LocalBroadcastManager.getInstance(ctx).unregisterReceiver(voteReceiver);
			votesReceived = 0;
			doWork=false;
			if(sendVotesTask!=null)
				sendVotesTask.cancel(true);
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {

			voteReceiver = new BroadcastReceiver(){

				@Override
				public void onReceive(Context arg0, Intent intent) {
					Option vote = (Option)intent.getSerializableExtra("vote");
					for(Option op : poll.getOptions()){
						if(op.equals(vote)){
							op.setVotes(op.getVotes()+1);
						}
					}
					String voter = intent.getStringExtra("voter");
					if(poll.getParticipants().containsKey(voter)){
						votesReceived++;
						poll.getParticipants().get(voter).setHasVoted(true);
					}

					sendVotesTask = new AsyncTask<Object, Object, Object>(){

						@Override
						protected Object doInBackground(Object... arg0) {
							while(doWork){
								Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
								i.putExtra("votes", votesReceived);
								i.putExtra("options", (Serializable)poll.getOptions());
								i.putExtra("participants", (Serializable)poll.getParticipants());
								LocalBroadcastManager.getInstance(ctx).sendBroadcast(i);
								SystemClock.sleep(1000);
							}
							return null;
						}

					}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			};
			LocalBroadcastManager.getInstance(ctx).registerReceiver(voteReceiver, new IntentFilter(BroadcastIntentTypes.newVote));
			return super.onStartCommand(intent, flags, startId);
		}

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}

		public static VoteService getInstance(){
			return instance;
		}

		public int getVotes(){
			return this.votesReceived;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}

}
