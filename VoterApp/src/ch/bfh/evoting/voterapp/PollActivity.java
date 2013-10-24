package ch.bfh.evoting.voterapp;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ch.bfh.evoting.voterapp.adapters.PollAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;

/**
 * Class displaying all the available polls
 *
 */
public class PollActivity extends Activity implements OnItemClickListener {

	private ListView lvPolls;

	private PollDbHelper pollDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll);
		// Show the Up button in the action bar.
		setupActionBar();

		AndroidApplication.getInstance().setIsAdmin(true);
		
		pollDbHelper = PollDbHelper.getInstance(this);

		lvPolls = (ListView) findViewById(R.id.listview_polls);
		List<Poll> polls = pollDbHelper.getAllOpenPolls();
		Poll poll = new Poll();
		poll.setQuestion(getString(R.string.action_create_poll));
		polls.add(poll);
		lvPolls.setAdapter(new PollAdapter(this, R.layout.list_item_poll, polls));
		lvPolls.setOnItemClickListener(this);
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poll, menu);
		return true;
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
		case R.id.action_network_info:
			//Intent i = new Intent(this, NetworkInformationActivity.class);
			//startActivity(i);
			NetworkDialogFragment ndf = NetworkDialogFragment.newInstance();
			ndf.show( getFragmentManager( ), "networkInfo" );
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_poll), getString(R.string.help_text_poll) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> listview, View view, int position,
			long id) {

		if (listview.getAdapter().getCount() - 1 == position) {
			Intent intent = new Intent(this, PollDetailActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, PollDetailActivity.class);
			intent.putExtra("pollid", view.getId());
			startActivity(intent);
		}

	}

//	@Override
//	public void onBackPressed() {
//		//do nothing because we don't want that people access to an anterior activity
//	}
}
