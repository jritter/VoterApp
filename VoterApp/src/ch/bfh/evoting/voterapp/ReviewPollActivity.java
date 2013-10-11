package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_poll);
	}

	@Override
	public void onBackPressed() {
		//do nothing because we don't want that people access to an anterior activity
	}
	
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
}
