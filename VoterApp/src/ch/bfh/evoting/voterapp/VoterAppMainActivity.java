package ch.bfh.evoting.voterapp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class VoterAppMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voter_app_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voter_app_main, menu);
		return true;
	}

}
