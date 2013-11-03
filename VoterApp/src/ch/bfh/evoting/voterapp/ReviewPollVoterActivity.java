package ch.bfh.evoting.voterapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;

/**
 * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollVoterActivity extends Activity {

	private BroadcastReceiver pollReceiver;
	private AlertDialog dialogBack;

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
		
		View view = getLayoutInflater().inflate(R.layout.activity_review_poll_voter, overlayFramelayout,false);
		overlayFramelayout.addView(view);
		
		final SharedPreferences settings = getSharedPreferences(AndroidApplication.PREFS_NAME, MODE_PRIVATE);
		
		if(settings.getBoolean("first_run_"+this.getClass().getSimpleName(), true)){
			final View overlay_view = getLayoutInflater().inflate(R.layout.overlay_review_poll, null,false);
			overlayFramelayout.addView(overlay_view);
			
			overlay_view.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					overlayFramelayout.removeView(overlay_view);
					settings.edit().putBoolean("first_run_"+ReviewPollVoterActivity.this.getClass().getSimpleName(), false).commit();					
				}
			});
		}
		setContentView(overlayFramelayout);
		
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(pollReceiver);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				ReviewPollVoterActivity.super.onBackPressed();
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
		dialogBack.show();
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
