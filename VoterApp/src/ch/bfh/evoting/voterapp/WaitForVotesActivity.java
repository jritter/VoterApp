package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.WaitForVotesFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity show when the participant has already submitted her vote but other voters are still voting
 * @author Philémon von Bergen
 *
 */
public class WaitForVotesActivity extends Activity {

	private AlertDialog dialogBack;
	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		AndroidApplication.getInstance().setCurrentActivity(this);

		setContentView(R.layout.activity_wait_for_votes);
		
		if(savedInstanceState!=null){
			poll = (Poll) savedInstanceState.getSerializable("poll");
		}
		Poll intentPoll = (Poll)this.getIntent().getSerializableExtra("poll");
		if(intentPoll!=null){
			poll = intentPoll;
		}
		
		FragmentManager fm = getFragmentManager();
		WaitForVotesFragment fragment = new WaitForVotesFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("poll", poll);
		fragment.setArguments(bundle);

		fm.beginTransaction().replace(R.id.fragment_container, fragment, "wait").commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		AndroidApplication.getInstance().setCurrentActivity(this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("poll", poll);
	}

	@Override
	public void onBackPressed() {
		//Show a dialog to ask confirmation to quit vote 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogBack.dismiss();
				startActivity(new Intent(WaitForVotesActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.help){
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_wait), getString(R.string.help_text_wait) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wait_for_votes, menu);
		return true;
	}

}
