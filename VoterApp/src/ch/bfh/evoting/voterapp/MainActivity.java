package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.concurrent.Callable;

import ch.bfh.evoting.voterapp.network.wifi.WifiAPManager;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

	private static final String TAG = WifiAPManager.class.getName();

	private Button btnSetupNetwork;
	private Button btnPollArchive;
	private Button btnPolls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getBoolean(R.bool.portrait_only)){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    }
		
		setContentView(R.layout.activity_main);
		AndroidApplication.getInstance().setVoteRunning(false);
		AndroidApplication.getInstance().setCurrentActivity(this);

		AndroidApplication.getInstance().setIsAdmin(false);

		btnSetupNetwork = (Button) findViewById(R.id.button_joinnetwork);
		btnPolls = (Button) findViewById(R.id.button_polls);
		btnPollArchive = (Button) findViewById(R.id.button_archive);

		btnSetupNetwork.setOnClickListener(this);
		btnPolls.setOnClickListener(this);
		btnPollArchive.setOnClickListener(this);


	}

	@Override
	protected void onResume(){
		AndroidApplication.getInstance().setIsAdmin(false);
		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		if (view == btnSetupNetwork) {


			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return goToNetworkConfig();
				}
			});

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
			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return showNetworkInfoDialog();
				}
			});
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_main), getString(R.string.help_text_main) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void waitForNetworkInterface(final Callable<Void> methodToExecute){
		//Network interface can be null since it is created in an async task, so we wait until the task is completed
		if(AndroidApplication.getInstance().getNetworkInterface()==null){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_wait_wifi);
			final AlertDialog waitDialog = builder.create();
			waitDialog.show();

			new AsyncTask<Object, Object, Object>(){

				@Override
				protected Object doInBackground(Object... params) {
					while(AndroidApplication.getInstance().getNetworkInterface()==null){
						//wait
					}
					waitDialog.dismiss();
					try {
						methodToExecute.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			return;
		}
		//then start next activity
		try {
			methodToExecute.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Void goToNetworkConfig(){
		//then start next activity
		if(AndroidApplication.getInstance().getNetworkInterface().getGroupName()==null){
			Intent intent = new Intent(this, NetworkConfigActivity.class);
			intent.putExtra("hideCreateNetwork", true);
			startActivity(intent);
		} else {
			Intent i = new Intent(this, NetworkInformationActivity.class);
			startActivity(i);
		}
		return null;
	}

	private Void showNetworkInfoDialog(){
		NetworkDialogFragment ndf = NetworkDialogFragment.newInstance();			
		ndf.show( getFragmentManager( ), "networkInfo" );
		return null;
	}


}
