package ch.bfh.evoting.voterapp;

import ch.bfh.evoting.votinglib.ListTerminatedPollsActivity;
import ch.bfh.evoting.votinglib.util.HelpDialogFragment;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * First activity, displaying the buttons for the different actions
 * @author Philémon von Bergen
 *
 */
public class VoterAppMainActivity extends Activity implements OnClickListener {
	
	private Button btnSetupNetwork;
	private Button btnPollArchive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnSetupNetwork = (Button) findViewById(R.id.button_joinnetwork);
		btnPollArchive = (Button) findViewById(R.id.button_archive);
		
		btnSetupNetwork.setOnClickListener(this);
		btnPollArchive.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view == btnSetupNetwork) {
			Intent intent = new Intent(this, NetworkConfigActivity.class);
	        startActivity(intent);
		} else if (view == btnPollArchive){
			Intent intent = new Intent(this, ListTerminatedPollsActivity.class);
	        startActivity(intent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.network_info:
			Intent i = new Intent(this, ch.bfh.evoting.votinglib.NetworkInformationsActivity.class);
			startActivity(i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_main), getString(R.string.help_text_main) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		//do nothing because we don't want that people access to an anterior activity
	}

}
