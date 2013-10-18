package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;

import ch.bfh.evoting.voterapp.adapters.PollOptionAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.DatabaseException;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.HelpDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
/**
 * Class displaying the activity that show the details of a poll
 *
 */
public class PollDetailActivity extends Activity implements OnClickListener {

	private static final int REQUEST_CODE_SETUPNETWORK = 0;
	private ListView lv;
	private PollOptionAdapter adapter;
	ArrayList<Option> options;

	private ImageButton btnAddOption;
	private Button btnSavePoll;
	private Button btnStartPoll;
	private EditText etOption;
	private EditText etQuestion;
	private CheckBox cbEmptyVote;

	private Poll poll;
	private Poll savedPoll;

	private PollDbHelper pollDbHelper;

	private boolean updatePoll = false;

	private AlertDialog dialogSave;
	private AlertDialog dialogAddOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll_detail);
		// Show the Up button in the action bar.
		setupActionBar();

		pollDbHelper = PollDbHelper.getInstance(this);


		if (getIntent().getIntExtra("pollid", -1) == -1 && savedPoll == null){
			// we didn't get a poll id, so let's create a new poll.
			poll = new Poll();
			options = new ArrayList<Option>();
			poll.setOptions(options);
			updatePoll = false;
		} else if (savedPoll != null) {
			poll = savedPoll;
			options = (ArrayList<Option>) poll.getOptions();
			updatePoll = true;
		}
		else {
			poll = pollDbHelper.getPoll(getIntent().getIntExtra("pollid", -1));
			options = (ArrayList<Option>) poll.getOptions();
			updatePoll = true;
		}

		lv = (ListView) findViewById(R.id.listview_pollquestions);
		btnAddOption = (ImageButton) findViewById(R.id.button_addoption);
		//		btnSavePoll = (Button) findViewById(R.id.button_save_poll);
		btnStartPoll = (Button) findViewById(R.id.button_start_poll);
		etOption = (EditText) findViewById(R.id.edittext_option);
		etQuestion = (EditText) findViewById(R.id.edittext_question);

		etQuestion.setText(poll.getQuestion());

		btnAddOption.setOnClickListener(this);
		//		btnSavePoll.setOnClickListener(this);
		btnStartPoll.setOnClickListener(this);

		adapter = new PollOptionAdapter(this, R.id.listview_pollquestions, poll);

		lv.setAdapter(adapter);

		cbEmptyVote = (CheckBox)findViewById(R.id.checkbox_emptyvote);
		cbEmptyVote.setText(R.string.allow_empty_vote);
		cbEmptyVote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//cbEmptyVote.toggle();
				if(cbEmptyVote.isChecked()){
					poll.getOptions().add(new Option(getString(R.string.empty_vote),0,0,0,0));
				} else {
					for(Option o:poll.getOptions()){
						if(o.getText().equals(getString(R.string.empty_vote))){
							poll.getOptions().remove(o);
							break;
						}
					}
				}

				adapter.notifyDataSetChanged();
				lv.invalidate();
			}
		});

		cbEmptyVote.setChecked(false);
		for(Option o:poll.getOptions()){
			if(o.getText().equals(getString(R.string.empty_vote))){
				cbEmptyVote.setChecked(true);
				break;
			}
		}

		etOption.setOnEditorActionListener(new OnEditorActionListener() {


			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
					btnAddOption.performClick();
					btnAddOption.animate().start();
				}    
				return false;
			}
		});

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.poll_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			askToSave();
			return true;
		case R.id.action_network_info:
			Intent i = new Intent(this, NetworkInformationsActivity.class);
			startActivity(i);
			return true;
		case R.id.help:
			HelpDialogFragment hdf = HelpDialogFragment.newInstance( getString(R.string.help_title_poll_details), getString(R.string.help_text_poll_details) );
			hdf.show( getFragmentManager( ), "help" );
			return true;
		}
		return super.onOptionsItemSelected(item); 
	}
	
	private void askToSave(){
		if(!etOption.getText().toString().equals("")){
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			// Add the buttons
			builder1.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					btnAddOption.performClick();
					dialogAddOption.dismiss();
				}
			});
			builder1.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					etOption.setText("");
					dialogAddOption.dismiss();
				}
			});

			builder1.setTitle(R.string.dialog_title_add_option);
			builder1.setMessage(R.string.dialog_add_option);

			// Create the AlertDialog
			dialogAddOption = builder1.create();
			dialogAddOption.show();
			return;
		}
		// This ID represents the Home or Up button. In the case of this
		// activity, the Up button is shown. Use NavUtils to allow users
		// to navigate up one level in the application structure. For
		// more details, see the Navigation pattern on Android Design:
		//
		// http://developer.android.com/design/patterns/navigation.html#up-vs-back
		//
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (poll.getId()>-1){
					updatePoll();
				} else {
					savePoll();
				}
				dialogSave.dismiss();
				NavUtils.navigateUpFromSameTask(PollDetailActivity.this);
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialogSave.dismiss();
				NavUtils.navigateUpFromSameTask(PollDetailActivity.this);
			}
		});

		builder.setTitle(R.string.dialog_title_save_poll);
		builder.setMessage(R.string.dialog_save_poll);

		// Create the AlertDialog
		dialogSave = builder.create();
		dialogSave.show();
	}

	@Override
	public void onClick(View view) {	
		if (view == btnAddOption){ 
			if (!etOption.getText().toString().equals("")){
				Option option = new Option();
				option.setText(etOption.getText().toString());
				if(cbEmptyVote.isChecked()){
					options.add(options.size()-1,option);
				} else {
					options.add(option);
				}
				poll.setOptions(options);
				adapter.notifyDataSetChanged();
				etOption.setText("");
			}
		}

		if (view == btnStartPoll){

			if(!etOption.getText().toString().equals("")){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// Add the buttons
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						btnAddOption.performClick();
						btnStartPoll.performClick();
						dialogAddOption.dismiss();
						return;
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						etOption.setText("");
						btnStartPoll.performClick();
						dialogAddOption.dismiss();
						return;
					}
				});

				builder.setTitle(R.string.dialog_title_add_option);
				builder.setMessage(R.string.dialog_add_option);

				// Create the AlertDialog
				dialogAddOption = builder.create();
				dialogAddOption.show();
				return;
			}

			//save the poll
			if (poll.getId()>-1){
				updatePoll();
			}
			else {
				savePoll();
			}

			//Check if it is complete
			if(poll.getQuestion()==null || poll.getQuestion().equals("")){
				Toast.makeText(this, getString(R.string.toast_question_empty), Toast.LENGTH_SHORT).show();
				return;
			}
			if(poll.getOptions().size()<2){
				Toast.makeText(this, getString(R.string.toast_not_enough_options), Toast.LENGTH_SHORT).show();
				return;
			}
			for(Option o : poll.getOptions()){
				if(o.getText()==null || o.getText().equals("")){
					Toast.makeText(this, getString(R.string.toast_option_empty), Toast.LENGTH_SHORT).show();
					return;
				}
			}

			//then start next activity
			if(AndroidApplication.getInstance().getNetworkInterface().getConversationPassword()==null){
				Intent i = new Intent(this, NetworkConfigActivity.class);
				i.putExtra("poll", (Serializable)this.poll);
				startActivity(i);
			} else {
				Intent i = new Intent(this, NetworkInformationsActivity.class);
				i.putExtra("poll", (Serializable)this.poll);
				startActivity(i);
			}


		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("poll", poll);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		savedPoll = (Poll)savedInstanceState.getSerializable("poll");
	}

	private void savePoll() {
		poll.setQuestion(etQuestion.getText().toString());
		poll.setOptions(options);
		try {
			long id = pollDbHelper.savePoll(poll);
			poll.setId((int)id);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	private void updatePoll() {
		poll.setQuestion(etQuestion.getText().toString());
		poll.setOptions(options);
		try {
			pollDbHelper.updatePoll(poll.getId(), poll);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		askToSave();
	}
}
