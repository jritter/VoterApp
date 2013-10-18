
package ch.bfh.evoting.voterapp.adapters;

import java.util.List;

import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Option;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter showing a list of the options with the corresponding result
 * @author von Bergen Philémon
 */
public class OptionListAdapter extends ArrayAdapter<Option> {

	private Context context;
	private List<Option> values;

	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of options that have to be listed
	 */
	public OptionListAdapter(Context context, int textViewResourceId, List<Option> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
		this.values=objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			view =  inflater.inflate(R.layout.list_item_result, parent, false);
		} else {
			view = convertView;
		}

		TextView tvOption =  (TextView)view.findViewById(R.id.textview_result_option);
		tvOption.setText(this.values.get(position).getText());

		TextView tvVotesReceived =  (TextView)view.findViewById(R.id.textview_result_received_votes);
		String text = ""+this.values.get(position).getVotes();
		tvVotesReceived.setText(text);

		TextView tvPercentage =  (TextView)view.findViewById(R.id.textview_result_percentage);
		String textPercentage = ""+this.values.get(position).getPercentage();
		tvPercentage.setText(textPercentage);

		return view;
	}


}
