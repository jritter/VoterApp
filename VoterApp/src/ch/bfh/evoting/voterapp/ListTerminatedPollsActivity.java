package ch.bfh.evoting.voterapp;

import java.util.List;

import ch.bfh.evoting.voterapp.adapters.PollArchiveAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Activity listing all terminated polls
 * @author Philémon von Bergen
 *
 */
public class ListTerminatedPollsActivity extends Activity {

	private NfcAdapter nfcAdapter;
	private boolean nfcAvailable;
	private PendingIntent pendingIntent;
	
	private ListView lvPolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		final FrameLayout overlayFramelayout = new FrameLayout(this);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0, getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin), 0);
		overlayFramelayout.setLayoutParams(layoutParams);
		
		View view = getLayoutInflater().inflate(R.layout.activity_list_terminated_polls, overlayFramelayout,false);
		overlayFramelayout.addView(view);
		
		final SharedPreferences settings = getSharedPreferences(AndroidApplication.PREFS_NAME, MODE_PRIVATE);
		
		if(settings.getBoolean("first_run", true)){
			final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_parent_button, null,false);
			overlayFramelayout.addView(overlay_view);
			
			overlay_view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					overlayFramelayout.removeView(overlay_view);
					settings.edit().putBoolean("first_run", false).commit();					
				}
			});
		}
		setContentView(overlayFramelayout);

		setupActionBar();
		
		AndroidApplication.getInstance().setCurrentActivity(this);


		//get the poll and generate the list
		List<Poll> polls = PollDbHelper.getInstance(this).getAllTerminatedPolls();

		lvPolls = (ListView) findViewById(R.id.listview_polls);
		lvPolls.setAdapter(new PollArchiveAdapter(this, R.layout.list_item_poll, polls));
		lvPolls.setEmptyView(findViewById(R.id.textview_empty));

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
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(i);
			return true;
		case R.id.help:
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

	public void onItemClick(AdapterView<?> listview, View view, int position, long id) {

		// extract the object assigned to the position which has been clicked
		Poll poll = (Poll) listview.getAdapter().getItem(position);

		//Start the activity displaying the result of the given poll
		Intent intent = new Intent(this, DisplayResultActivity.class);
		intent.putExtra("poll", poll);
		intent.putExtra("saveToDb", false);
		startActivity(intent);
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
	
}
