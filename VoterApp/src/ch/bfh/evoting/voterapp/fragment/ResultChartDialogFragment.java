package ch.bfh.evoting.voterapp.fragment;

import ch.bfh.evoting.voterapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class ResultChartDialogFragment extends DialogFragment {
	
	private static ResultChartDialogFragment instance = null;

    public static ResultChartDialogFragment newInstance() {
    	
    	if (instance == null) {
    		instance = new ResultChartDialogFragment();
    	}
//        Bundle args = new Bundle( );
//        args.putString( "subtitle", subtitle );
//        args.putString( "text", text );
//        frag.setArguments( args );
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

        return builder.create( );
    }
}