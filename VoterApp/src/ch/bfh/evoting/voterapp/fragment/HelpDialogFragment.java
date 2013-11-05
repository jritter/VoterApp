package ch.bfh.evoting.voterapp.fragment;

import ch.bfh.evoting.voterapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

public class HelpDialogFragment extends DialogFragment {

	private AlertDialog dialog;

	// Factory method to create a new EditTextDialogFragment 
	public static HelpDialogFragment newInstance( String subtitle, String text ) {
		HelpDialogFragment frag = new HelpDialogFragment( );
		Bundle args = new Bundle( );
		args.putString( "subtitle", subtitle );
		args.putString( "text", text );
		frag.setArguments( args );
		return frag;
	}

	// Set title and default text
	@Override
	public Dialog onCreateDialog( Bundle savedInstanceState ) {

		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_help, null);
		TextView tv_subtitle = (TextView)view.findViewById(R.id.subtitle);
		tv_subtitle.setText(getArguments( ).getString( "subtitle" ));
//		TextView tv_text = (TextView)view.findViewById(R.id.text);
//		tv_text.setText(Html.fromHtml(getArguments( ).getString( "text" )));
//		tv_text.setMovementMethod(LinkMovementMethod.getInstance());
		WebView webView = (WebView) view.findViewById(R.id.help_webview);
		webView.loadDataWithBaseURL(null, getArguments( ).getString( "text" ), "text/html", "utf-8", null);
		webView.setBackgroundColor(0x00000000);
		webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity( ) )
		.setView(view)
		.setIcon( android.R.drawable.ic_dialog_info )
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
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