

package ch.bfh.evoting.voterapp.fragment;

import ch.bfh.evoting.voterapp.R;
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
public class IdentificationWlanKeyDialogFragment extends DialogFragment implements
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
	
	private NoticeDialogListener mListener;

	private EditText etIdentification;
	private EditText etWlanKey;

	private String identification;
	private String wlanKey;
	
	private Button btnOk;
	private Button btnCancel;


	private boolean showIdentificationField;
	private boolean showWlanKeyField;

	private AlertDialog dialog;


	/**
	 * @param showNetworkKeyField
	 *            this boolean defines whether the network key field should be
	 *            displayed or not
	 */
	public IdentificationWlanKeyDialogFragment(boolean showIdentificationField, boolean showWlanKeyField) {
		this.showIdentificationField = showIdentificationField;
		this.showWlanKeyField = showWlanKeyField;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (NoticeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
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
		View view = inflater.inflate(R.layout.dialog_identification_wlankey, null);

		// extract the controls of the layout
		etIdentification = (EditText) view.findViewById(R.id.edittext_identification);
		etIdentification.addTextChangedListener(this);

		etWlanKey = (EditText) view.findViewById(R.id.edittext_networkkey);
		etWlanKey.addTextChangedListener(this);

		if (!showIdentificationField) {
			etIdentification.setVisibility(View.GONE);
		}
		
		if (!showWlanKeyField){
			etWlanKey.setVisibility(View.GONE);
		}

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(view);
		// Add action buttons
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				identification = etIdentification.getText().toString();
				wlanKey = etWlanKey.getText().toString();
				mListener.onDialogPositiveClick(IdentificationWlanKeyDialogFragment.this);
			}
		});

		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				identification = etIdentification.getText().toString();
				wlanKey = etWlanKey.getText().toString();
				mListener.onDialogNegativeClick(IdentificationWlanKeyDialogFragment.this);
			}
		});

		//builder.setTitle(R.string.network_password);



		dialog = builder.create();

		

		// always disable the Join button since the key is always empty and
		// therefore we are not ready to connect yet
		dialog.setOnShowListener(new OnShowListener() {

			public void onShow(DialogInterface dialog) {
					btnOk = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
					btnCancel = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
					btnOk.setBackgroundResource(R.drawable.selectable_background_votebartheme);
					btnCancel.setBackgroundResource(R.drawable.selectable_background_votebartheme);
					btnOk.setEnabled(false);
			}
		});

		return dialog;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	public void afterTextChanged(Editable s) {

		// handling the activation of the buttons
		if (showIdentificationField && showWlanKeyField) {
			if (etIdentification.getText().toString().length() < 1
					|| etWlanKey.getText().toString().length() < 8) {
				btnOk.setEnabled(false);
			} else {
				btnOk.setEnabled(true);
			}
		}
		else if (showIdentificationField){
			if (etIdentification.getText().toString().length() < 1) {
				btnOk.setEnabled(false);
			} else {
				btnOk.setEnabled(true);
			}
		}
		else if (showWlanKeyField){
			if (etWlanKey.getText().toString().length() < 8) {
				btnOk.setEnabled(false);
			} else {
				btnOk.setEnabled(true);
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
	
	public String getWlanKey() {
		return wlanKey;
	}
	
	public String getIdentification(){
		return identification;
	}
}
