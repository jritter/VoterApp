package ch.bfh.evoting.voterapp.cgs97.fragment;

import ch.bfh.evoting.voterapp.cgs97.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;

public class ResultChartDialogFragment extends DialogFragment {
	
	private static ResultChartDialogFragment instance = null;
	private AlertDialog dialog;

    public static ResultChartDialogFragment newInstance() {
    	
    	if (instance == null) {
    		instance = new ResultChartDialogFragment();
    	}
    	return instance;
    }

    // Set title and default text
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {
        

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_chart, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setIcon( android.R.drawable.ic_dialog_info )
        .setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FragmentTransaction ft = ResultChartDialogFragment.this.getFragmentManager().beginTransaction();
				ft.remove(getFragmentManager().findFragmentByTag("resultChart"));
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