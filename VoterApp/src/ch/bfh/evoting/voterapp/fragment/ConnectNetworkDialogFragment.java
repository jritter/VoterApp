

package ch.bfh.evoting.voterapp.fragment;

import ch.bfh.evoting.voterapp.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
	private EditText txtNetworkKey;

	private String password;
	private String networkKey;

	private boolean showNetworkKeyField;

	private AlertDialog dialog;

	private static final String PREFS_NAME = "network_preferences";


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
		txtPassword = (EditText) view.findViewById(R.id.edittext_password);
		txtPassword.addTextChangedListener(this);

		txtNetworkKey = (EditText) view.findViewById(R.id.edittext_networkkey);
		txtNetworkKey.addTextChangedListener(this);

		if (!showNetworkKeyField) {
			txtNetworkKey.setVisibility(View.INVISIBLE);
		}

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view);
		// Add action buttons
		builder.setPositiveButton(R.string.join,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				password = txtPassword.getText().toString();
				networkKey = txtNetworkKey.getText().toString();
				if(Character.isLetter(password.charAt(0))){
					SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
					SharedPreferences.Editor editor = preferences.edit();
//											editor.putString("SSID", selectedResult.SSID);
					editor.putString("password",password);
					//								((ConnectNetworkDialogFragment) dialog).getPassword());
					editor.commit();
					getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
				} else {
					Toast.makeText(ConnectNetworkDialogFragment.this.getActivity(), R.string.taost_password_letter, Toast.LENGTH_LONG).show();
				}
			}
		});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				password = txtPassword.getText().toString();
				networkKey = txtNetworkKey.getText().toString();
				getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
			}
		});

		builder.setTitle(R.string.network_password);



		dialog = builder.create();



		// always disable the Join button since the key is always empty and
		// therefore we are not ready to connect yet
		dialog.setOnShowListener(new OnShowListener() {

			public void onShow(DialogInterface dialog) {
				((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE)
				.setEnabled(false);
			}
		});

		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		//		try {
		//			// Instantiate the NoticeDialogListener so we can send events to the
		//			// host
		//			mListener = (NoticeDialogListener) activity;
		//		} catch (ClassCastException e) {
		//			// The activity doesn't implement the interface, throw exception
		//			throw new ClassCastException(activity.toString()
		//					+ " must implement NoticeDialogListener");
		//		}
	}

	/**
	 * Returns the password which is defined in the textfield
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
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

		Button joinButton = ((AlertDialog) this.getDialog())
				.getButton(AlertDialog.BUTTON_POSITIVE);

		// handling the activation of the buttons
		if (showNetworkKeyField) {
			// activate only if there is at least one character in the password
			// field and 8 characters in the network key field
			if (txtPassword.getText().toString().length() < 1
					|| txtNetworkKey.getText().toString().length() < 8) {
				joinButton.setEnabled(false);
			} else {
				joinButton.setEnabled(true);
			}
		} else {
			// activate only if there is at least one character in the password
			// field
			if (txtPassword.getText().toString().length() < 1) {
				joinButton.setEnabled(false);
			} else {
				joinButton.setEnabled(true);
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
