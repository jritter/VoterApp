

package ch.bfh.evoting.voterapp.cgs97.fragment;

import ch.bfh.evoting.voterapp.cgs97.R;
import ch.bfh.evoting.voterapp.cgs97.AndroidApplication;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Fragment which is included in the the Dialog which is shown after clicking on
 * a network in the main screen.
 * 
 * @author Juerg Ritter (rittj1@bfh.ch)
 * 
 */
@SuppressLint("ValidFragment")
public class ConnectNetworkDialogFragment extends DialogFragment implements
OnClickListener, TextWatcher {

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	private EditText txtPassword;
	private EditText txtGroupName;
	private EditText txtNetworkKey;

	private String password;
	private String networkKey;
	private String groupName;
	
	private Button buttonJoin;
	private Button buttonCancel;


	private boolean showNetworkKeyField;

	private AlertDialog dialog;


	/**
	 * @param showNetworkKeyField
	 *            this boolean defines whether the network key field should be
	 *            displayed or not
	 */
	public ConnectNetworkDialogFragment(boolean showNetworkKeyField) {
		this.showNetworkKeyField = showNetworkKeyField;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		// applying the layout
		View view = inflater.inflate(R.layout.dialog_join_network, null);

		// extract the controls of the layout
		txtGroupName = (EditText) view.findViewById(R.id.edittext_group_name);
		txtGroupName.addTextChangedListener(this);

		txtPassword = (EditText) view.findViewById(R.id.edittext_password);
		txtPassword.addTextChangedListener(this);

		txtNetworkKey = (EditText) view.findViewById(R.id.edittext_networkkey);
		txtNetworkKey.addTextChangedListener(this);
		
		

		if (!showNetworkKeyField) {
			txtNetworkKey.setVisibility(View.INVISIBLE);
		}

		if(AndroidApplication.getInstance().isAdmin()){
			txtPassword.setVisibility(View.INVISIBLE);
			txtGroupName.setVisibility(View.INVISIBLE);
		}
		
		if(txtPassword.getVisibility() == View.INVISIBLE &&
				txtGroupName.getVisibility() == View.INVISIBLE &&
				txtNetworkKey.getVisibility() == View.INVISIBLE){
			saveData();
		}

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view);
		// Add action buttons
		builder.setPositiveButton(R.string.join,
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				saveData();

			}
		});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				password = txtPassword.getText().toString();
				groupName = txtGroupName.getText().toString();
				networkKey = txtNetworkKey.getText().toString();
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
			}
		});

		//builder.setTitle(R.string.network_password);



		dialog = builder.create();

		

		// always disable the Join button since the key is always empty and
		// therefore we are not ready to connect yet
		dialog.setOnShowListener(new OnShowListener() {

			public void onShow(DialogInterface dialog) {
					
					buttonJoin = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
					buttonCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
					
					buttonJoin.setBackgroundResource(R.drawable.selectable_background_votebartheme);
					buttonCancel.setBackgroundResource(R.drawable.selectable_background_votebartheme);
				
					buttonJoin.setEnabled(false);
			}
		});

		return dialog;
	}
	

	private void saveData(){
		password = txtPassword.getText().toString();
		groupName = "group"+txtGroupName.getText().toString();
		networkKey = txtNetworkKey.getText().toString();
		
		if(!AndroidApplication.getInstance().isAdmin()){
			
			AndroidApplication.getInstance().getNetworkInterface().setGroupName(groupName);
			AndroidApplication.getInstance().getNetworkInterface().setGroupPassword(password);
		}
		
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
	}

	/**
	 * Returns the network key which is defined in the textfield
	 * 
	 * @return the network key
	 */
	public String getNetworkKey() {
		return networkKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	public void afterTextChanged(Editable s) {

		// handling the activation of the buttons
		if (showNetworkKeyField) {
			// activate only if there is at least one character in the password
			// field and 8 characters in the network key field
			if (txtPassword.getText().toString().length() < 1
					|| txtNetworkKey.getText().toString().length() < 8) {
				buttonJoin.setEnabled(false);
			} else {
				buttonJoin.setEnabled(true);
			}
		} else {
			if(AndroidApplication.getInstance().isAdmin()){
				buttonJoin.setEnabled(true);
			} else {
				// activate only if there is at least one character in the password
				// field
				if (txtPassword.getText().toString().length() < 1) {
					buttonJoin.setEnabled(false);
				} else {
					buttonJoin.setEnabled(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence,
	 * int, int, int)
	 */
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int,
	 * int, int)
	 */
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View view) {

	}
}
