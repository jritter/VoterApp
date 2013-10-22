package ch.bfh.evoting.voterapp;

import java.util.List;

import ch.bfh.evoting.voterapp.adapters.PollAdapter;
import ch.bfh.evoting.voterapp.adapters.PollArchiveAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.util.HelpDialogFragment;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Activity listing all terminated polls
 * @author Philémon von Bergen
 *
 */
public class ListTerminatedPollsActivity extends Activity {

	private ListView lvPolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_terminated_polls);
		setupActionBar();

		//get the poll and generate the list
		List<Poll> polls = PollDbHelper.getInstance(this).getAllTerminatedPolls();

		lvPolls = (ListView) findViewById(R.id.listview_polls);
		lvPolls.setAdapter(new PollArchiveAdapter(this, R.layout.list_item_poll, polls));

		//create a listener on each line
		final Context ctx = this;
		lvPolls.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
				Poll poll = (Poll) (lvPolls.getItemAtPosition(position));

				Intent intent = new Intent(ctx, DisplayResultActivity.class);
				intent.putExtra("poll", poll);
				intent.putExtra("saveToDb", false);
				startActivity(intent);
			}                 
		});

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(i);
			return true;
		} else if (item.getItemId() == R.id.help){
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_archive), getString(R.string.help_text_archive) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.archive, menu);
		return true;
	}

//	@Override
//	public void onBackPressed() {
//		//People can go back to calling activity
//		super.onBackPressed();
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> listview, View view, int position, long id) {

		// extract the object assigned to the position which has been clicked
		Poll poll = (Poll) listview.getAdapter().getItem(position);

		//Start the activity displaying the result of the given poll
		Intent intent = new Intent(this, DisplayResultActivity.class);
		intent.putExtra("poll", poll);
		intent.putExtra("saveToDb", false);
		startActivity(intent);
	}

}
