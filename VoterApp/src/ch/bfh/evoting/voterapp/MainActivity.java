package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.voterapp.util.HelpDialogFragment;
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
public class MainActivity extends Activity implements OnClickListener {
	
	private Button btnSetupNetwork;
	private Button btnPollArchive;
	private Button btnPolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AndroidApplication.getInstance().setIsAdmin(false);
		
		btnSetupNetwork = (Button) findViewById(R.id.button_joinnetwork);
		btnPolls = (Button) findViewById(R.id.button_polls);
		btnPollArchive = (Button) findViewById(R.id.button_archive);
		
		btnSetupNetwork.setOnClickListener(this);
		btnPolls.setOnClickListener(this);
		btnPollArchive.setOnClickListener(this);
		
		
	}

	@Override
	public void onClick(View view) {
		if (view == btnSetupNetwork) {
			//then start next activity
			if(AndroidApplication.getInstance().getNetworkInterface().getConversationPassword()==null){
				Intent intent = new Intent(this, NetworkConfigActivity.class);
				intent.putExtra("hideCreateNetwork", true);
		        startActivity(intent);
			} else {
				Intent i = new Intent(this, NetworkInformationsActivity.class);
				startActivity(i);
			}
		} else if (view == btnPolls){
			Intent intent = new Intent(this, PollActivity.class);
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
			Intent i = new Intent(this, ch.bfh.evoting.voterapp.NetworkInformationsActivity.class);
			startActivity(i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_main), getString(R.string.help_text_main) );
	        hdf.show( getFragmentManager( ), "help" );
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	@Override
//	public void onBackPressed() {
//		//do nothing because we don't want that people access to an anterior activity
//	}

}