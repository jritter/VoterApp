package ch.bfh.evoting.voterapp;

import java.io.Serializable;

import ch.bfh.evoting.votinglib.VoteActivity;
import ch.bfh.evoting.votinglib.entities.Option;
import ch.bfh.evoting.votinglib.entities.Participant;
import ch.bfh.evoting.votinglib.entities.Poll;
import ch.bfh.evoting.votinglib.util.BroadcastIntentTypes;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Class displaying the activity showing the entire poll, in order to allow the user to check if it is correct
 * @author Philémon von Bergen
 *
 */
public class ReviewPollActivity extends Activity {

	private Poll poll;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_poll);

		ListView lv = (ListView)findViewById(android.R.id.list);
		LayoutInflater inflater = this.getLayoutInflater();

		View header = inflater.inflate(R.layout.review_header, null, false);
		lv.addHeaderView(header);
		View footer = inflater.inflate(R.layout.review_footer, null, false);
		lv.addFooterView(footer);

		String[] array = {};
		int[] toViews = {android.R.id.text1};
		lv.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, array, toViews, 0));

		poll = (Poll)getIntent().getSerializableExtra("poll");

		TextView tv_question = (TextView) header.findViewById(R.id.textview_poll_question);
		tv_question.setText(poll.getQuestion());

		//Create options table
		TableLayout optionsTable = (TableLayout)header.findViewById(R.id.layout_options);

		for(Option op : poll.getOptions()){
			TableRow tableRow= new TableRow(this);
			tableRow.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

			View vItemOption = inflater.inflate(R.layout.list_item_option_poll, null);
			TextView tv_option = (TextView)vItemOption.findViewById(R.id.textview_poll_option_review);
			tv_option.setText(op.getText());

			tableRow.addView(vItemOption);

			optionsTable.addView(tableRow);
		}

		//Create participants table
		TableLayout participantsTable = (TableLayout)footer.findViewById(R.id.layout_participants);

		for(Participant part : poll.getParticipants()){
			TableRow tableRow= new TableRow(this);
			tableRow.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

			View vItemParticipant = inflater.inflate(R.layout.list_item_participant_poll, null);
			TextView tv_option = (TextView)vItemParticipant.findViewById(R.id.textview_participant_identification);
			tv_option.setText(part.getIdentification());

			tableRow.addView(vItemParticipant);

			participantsTable.addView(tableRow);
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				Intent i = new Intent(ReviewPollActivity.this, VoteActivity.class);
				i.putExtra("poll", (Serializable) poll);
				i.putExtra("isAdmin", false);
				startActivity(i);

			}
		}, new IntentFilter(BroadcastIntentTypes.goToVote));

		simulate();
	}

	//TODO remove, only for simulation
	private void simulate(){
		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {


				SystemClock.sleep(5000);

				//Send participants
				Intent intent = new Intent(BroadcastIntentTypes.goToVote);
				LocalBroadcastManager.getInstance(ReviewPollActivity.this).sendBroadcast(intent);


				return null;
			}
		}.execute();
	}

}
