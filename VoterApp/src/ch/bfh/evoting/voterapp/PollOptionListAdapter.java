
package ch.bfh.evoting.voterapp;

import java.util.List;

import ch.bfh.evoting.votinglib.entities.Option;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter showing a list of the options of the poll
 * @author von Bergen Philémon
 */
public class PollOptionListAdapter extends ArrayAdapter<Option> {

	private Context context;
	private List<Option> values;

	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of options that have to be listed
	 */
	public PollOptionListAdapter(Context context, int textViewResourceId, List<Option> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
		this.values=objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			view =  inflater.inflate(R.layout.list_item_option_poll, parent, false);
		} else {
			view = convertView;
		}

		//set option text
		TextView optionText =  (TextView)view.findViewById(R.id.textview_poll_option_review);
		optionText.setText(this.values.get(position).getText());

		return view;
	}


} 
