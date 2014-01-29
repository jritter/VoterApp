package ch.bfh.evoting.voterapp.hkrs12.fragment;

import ch.bfh.evoting.voterapp.hkrs12.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;

public class NetworkDialogFragment extends DialogFragment {
	
	private AlertDialog dialog;
	
	// Factory method to create a new EditTextDialogFragment 
    public static NetworkDialogFragment newInstance() {
    	NetworkDialogFragment frag = new NetworkDialogFragment();
    	return frag;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		
		
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_network_information, null);

		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity( ) )
        .setView(view)
        .setIcon( android.R.drawable.ic_dialog_info )
        .setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FragmentTransaction ft = NetworkDialogFragment.this.getFragmentManager().beginTransaction();
				ft.remove(getFragmentManager().findFragmentByTag("networkInformation"));
                ft.commit();
				dismiss();
			}
        	
        });
		
		dialog = builder.create();

		dialog.setOnShowListener(new OnShowListener() {

			public void onShow(DialogInterface dialog) {
				((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL)
						.setBackgroundResource(
								R.drawable.selectable_background_votebartheme);
			}
		});

        return dialog;
    }
	
}
