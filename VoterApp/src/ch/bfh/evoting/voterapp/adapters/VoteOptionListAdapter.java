package ch.bfh.evoting.voterapp.adapters;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.VoteActivity;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.util.Utility;

/**
 * Adapter listing the different vote options that can be chosen in the vote
 * This class is used in the Android ListView
 * @author Philémon von Bergen
 *
 */
public class VoteOptionListAdapter extends ArrayAdapter<Option> {

	private Context context;
	private List<Option> values;
	private int selected = -1;

	private AlertDialog dialogConfirmVote = null;


	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of options that can be chosen in the vote
	 */
	public VoteOptionListAdapter(Context context, int textViewResourceId, List<Option> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
		this.values=objects;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			//when view is created
			view =  inflater.inflate(R.layout.list_item_vote, parent, false);			
		} else {
			view = convertView;
		}

		final RadioButton rbChoice = (RadioButton)view.findViewById(R.id.radiobutton_choice);
		final TextView tvContent = (TextView) view.findViewById(R.id.textview_content);

		tvContent.setText(this.values.get(position).getText());
		view.setTag(position);
		rbChoice.setTag(position);

		if (position == selected) {
			rbChoice.setChecked(true);
		} else {
			rbChoice.setChecked(false);
		}

		// set the click listener
		OnClickListener click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				selected = position;
				VoteOptionListAdapter.this.notifyDataSetChanged();

				final VoteActivity activity = (VoteActivity)context;
				
				if(!activity.getScrolled()){
					Toast.makeText(context, context.getString(R.string.scroll), Toast.LENGTH_SHORT).show();
				} else if (getSelectedPosition() == -1){
					Toast.makeText(context, context.getString(R.string.choose_one_option), Toast.LENGTH_SHORT).show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					// Add the buttons
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							activity.castBallot();
							dialogConfirmVote.dismiss();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialogConfirmVote.dismiss();
						}
					});

					Option op = getItemSelected();

					builder.setTitle(R.string.dialog_title_confirm_vote);
					builder.setMessage(context.getString(R.string.dialog_confirm_vote, op.getText()));

					// Create the AlertDialog
					dialogConfirmVote = builder.create();
					
					dialogConfirmVote.setOnShowListener(new DialogInterface.OnShowListener() {
						@Override
						public void onShow(DialogInterface dialog) {
							Utility.setTextColor(dialog, context.getResources().getColor(R.color.theme_color));
							dialogConfirmVote.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundResource(
									R.drawable.selectable_background_votebartheme);
							dialogConfirmVote.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundResource(
									R.drawable.selectable_background_votebartheme);
							
						}
					});
					
					dialogConfirmVote.show();
				}
			}
		};

		view.setOnClickListener(click);

		return view;
	}

	@Override
	public Option getItem(int position) {
		if (position >= this.getCount() || position < 0)
			return null;
		return super.getItem(position);
	}

	public Option getItemSelected(){
		return this.values.get(selected);
	}

	public int getSelectedPosition () {
		return selected;
	}
}
