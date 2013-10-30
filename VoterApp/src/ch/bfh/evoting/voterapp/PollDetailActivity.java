package ch.bfh.evoting.voterapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import ch.bfh.evoting.voterapp.adapters.PollOptionAdapter;
import ch.bfh.evoting.voterapp.db.PollDbHelper;
import ch.bfh.evoting.voterapp.entities.DatabaseException;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.fragment.HelpDialogFragment;
import ch.bfh.evoting.voterapp.fragment.NetworkDialogFragment;
import ch.bfh.evoting.voterapp.util.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
	private Button btnStartPoll;
	private EditText etOption;
	private EditText etQuestion;
	private CheckBox cbEmptyVote;

	private Poll poll;
	private Poll savedPoll;

	private PollDbHelper pollDbHelper;

	private AlertDialog dialogSave;
	private AlertDialog dialogAddOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll_detail);
		// Show the Up button in the action bar.
		setupActionBar();

		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		AndroidApplication.getInstance().setIsAdmin(true);

		pollDbHelper = PollDbHelper.getInstance(this);


		if (getIntent().getIntExtra("pollid", -1) == -1 && savedPoll == null){
			// we didn't get a poll id, so let's create a new poll.
			poll = new Poll();
			options = new ArrayList<Option>();
			poll.setOptions(options);
		} else if (savedPoll != null) {
			poll = savedPoll;
			options = (ArrayList<Option>) poll.getOptions();
		}
		else {
			poll = pollDbHelper.getPoll(getIntent().getIntExtra("pollid", -1));
			options = (ArrayList<Option>) poll.getOptions();
		}

		lv = (ListView) findViewById(R.id.listview_pollquestions);
		btnAddOption = (ImageButton) findViewById(R.id.button_addoption);
		btnStartPoll = (Button) findViewById(R.id.button_start_poll);
		etOption = (EditText) findViewById(R.id.edittext_option);
		etQuestion = (EditText) findViewById(R.id.edittext_question);

		etQuestion.setText(poll.getQuestion());

		btnAddOption.setOnClickListener(this);
		btnStartPoll.setOnClickListener(this);

		adapter = new PollOptionAdapter(this, R.id.listview_pollquestions, poll);

		lv.setAdapter(adapter);

		cbEmptyVote = (CheckBox)findViewById(R.id.checkbox_emptyvote);
		cbEmptyVote.setText(R.string.allow_empty_vote);
		cbEmptyVote.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
				//toogle the checkbox to add the empty vote at the end of the list
				cbEmptyVote.performClick();
				cbEmptyVote.performClick();
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
			//Network interface can be null since it is created in an async task, so we wait until the task is completed
			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return showNetworkInfoDialog();
				}
			});
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

			dialogAddOption.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
					dialogAddOption.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
							R.drawable.selectable_background_votebartheme);
					dialogAddOption.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
							R.drawable.selectable_background_votebartheme);

				}
			});

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

		dialogSave.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Utility.setTextColor(dialog, getResources().getColor(R.color.theme_color));
				dialogSave.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
				dialogSave.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
						R.drawable.selectable_background_votebartheme);
			}
		});

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

			//Network interface can be null since it is created in an async task, so we wait until the task is completed
			this.waitForNetworkInterface(new Callable<Void>() {
				public Void call() {
					return goToNetworkConfig();
				}
			});
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

	@Override
	protected void onResume() {
		AndroidApplication.getInstance().setCurrentActivity(this);
		AndroidApplication.getInstance().setVoteRunning(false);
		super.onResume();
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
			Intent i = new Intent(this, NetworkConfigActivity.class);
			i.putExtra("poll", (Serializable)this.poll);
			startActivity(i);
		} else {
			Intent i = new Intent(this, NetworkInformationActivity.class);
			i.putExtra("poll", (Serializable)this.poll);
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
