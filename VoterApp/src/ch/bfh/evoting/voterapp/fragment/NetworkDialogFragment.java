package ch.bfh.evoting.voterapp.fragment;

import ch.bfh.evoting.voterapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class NetworkDialogFragment extends DialogFragment {
	
	// Factory method to create a new EditTextDialogFragment 
    public static NetworkDialogFragment newInstance() {
    	NetworkDialogFragment frag = new NetworkDialogFragment();
        //Bundle args = new Bundle( );
        //args.putString( "subtitle", subtitle );
        //args.putString( "text", text );
        //frag.setArguments( args );
        return frag;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		
		
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_network_information, null);

		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity( ) )
        .setView(view)
       // .setCustomTitle(customTitleView)
        .setIcon( android.R.drawable.ic_dialog_info )
        //.setTitle( getArguments( ).getString( "title" ) )
        .setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				FragmentTransaction ft = NetworkDialogFragment.this.getFragmentManager().beginTransaction();
				ft.remove(getFragmentManager().findFragmentByTag("networkInformation"));
                ft.commit();
				dismiss();
			}
        	
        });
		
		

        return builder.create();
    }
	
}
