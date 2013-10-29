package ch.bfh.evoting.voterapp;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
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

/**
 * Activity displaying the results of a poll
 * @author Philémon von Bergen
 *
 */
public class DisplayResultActivity extends ListActivity {

	private int pollId;
	private boolean saveToDbNeeded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_result);
		setupActionBar();

		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		if(AndroidApplication.getInstance().isAdmin()){
			AndroidApplication.getInstance().getNetworkInterface().unlockGroup();
		}

		ListView lv = (ListView)findViewById(android.R.id.list);
		
		//Get the data in the intent
		final Poll poll = (Poll)this.getIntent().getSerializableExtra("poll");
		saveToDbNeeded = this.getIntent().getBooleanExtra("saveToDb", false);
		if(poll.getId()>=0){
			pollId = poll.getId();
		} else {
			pollId = -1;
		}
		
		if(!AndroidApplication.getInstance().isAdmin() && saveToDbNeeded){
			LinearLayout ll = (LinearLayout)findViewById(R.id.layout_action_bar);
			((LinearLayout)ll.getParent()).removeView(ll);
			
			//register a listener of messages of the admin sending the electorate
			//TODO is that a good idea? when to unregister it
//			redoPollReceiver = new BroadcastReceiver(){
//
//				@Override
//				public void onReceive(Context context, Intent intent) {
//					LocalBroadcastManager.getInstance(DisplayResultActivity.this).unregisterReceiver(this);
//					Intent i = new Intent(DisplayResultActivity.this, CheckElectorateActivity.class);
//					i.putExtra("participants", intent.getSerializableExtra("participants"));
//					startActivity(i);
//				}
//			};
//			LocalBroadcastManager.getInstance(this).registerReceiver(redoPollReceiver, new IntentFilter(BroadcastIntentTypes.electorate));
		} else {

			
			//Set a listener on the redo button
			Button btnRedo = (Button)findViewById(R.id.button_redo_poll);
			btnRedo.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Poll newPoll = new Poll();
					newPoll.setQuestion(poll.getQuestion());
					List<Option> newOptions = new ArrayList<Option>();
					for(Option op : poll.getOptions()){
						Option newOp = new Option();
						newOp.setText(op.getText());
						newOptions.add(newOp);
					}
					newPoll.setOptions(newOptions);

					PollDbHelper pollDbHelper = PollDbHelper.getInstance(DisplayResultActivity.this);
					try {
						int pollId = (int)pollDbHelper.savePoll(newPoll);
						Intent i = new Intent(DisplayResultActivity.this, PollDetailActivity.class);
						i.putExtra("pollid", pollId);
						startActivity(i);
					} catch (DatabaseException e) {
						Toast.makeText(DisplayResultActivity.this, getString(R.string.redo_impossible), Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
				}
			});
			
			//Poll is just finished
			if(saveToDbNeeded){
				btnRedo.setText(R.string.redo_poll);
			} else {
				btnRedo.setText(R.string.clone_poll);				
			}
		}
		
		lv.setAdapter(new ResultAdapter(this, poll));

		//Save the poll to the DB if needed
		if(saveToDbNeeded){
			try {
				if(pollId>=0){
					PollDbHelper.getInstance(this).updatePoll(pollId,poll);
				} else {
					int newPollId = (int)PollDbHelper.getInstance(this).savePoll(poll);
					this.pollId = newPollId;
					poll.setId(newPollId);
				}
			} catch (DatabaseException e) {
				Log.e(this.getClass().getSimpleName(), "DB error: "+e.getMessage());
				e.printStackTrace();
			}
		}

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}


		@Override
		public void onBackPressed() {
			if(!this.saveToDbNeeded){
				super.onBackPressed();
			} else {
				Intent i = new Intent(this, MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
						Intent.FLAG_ACTIVITY_CLEAR_TASK |
						Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home){
//			LocalBroadcastManager.getInstance(this).registerReceiver(redoPollReceiver, new IntentFilter(BroadcastIntentTypes.electorate));
			//if ending a poll
			if(saveToDbNeeded){
				Intent i = new Intent(DisplayResultActivity.this, MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
						Intent.FLAG_ACTIVITY_CLEAR_TASK |
						Intent.FLAG_ACTIVITY_NEW_TASK);

				startActivity(i);


			} else {
				//if consulting an archive
				startActivity(new Intent(this, ListTerminatedPollsActivity.class));
			}
			return true;
		} else if (item.getItemId() == R.id.help){
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_display_results), getString(R.string.help_text_display_results) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return true;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_results, menu);
		return true;
	}
	
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setVoteRunning(false);
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	

}
