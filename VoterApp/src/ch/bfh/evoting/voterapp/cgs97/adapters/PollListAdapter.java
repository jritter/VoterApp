
package ch.bfh.evoting.voterapp.cgs97.adapters;

import java.util.List;

import ch.bfh.evoting.voterapp.cgs97.R;
import ch.bfh.evoting.voterapp.cgs97.DisplayResultActivity;
import ch.bfh.evoting.voterapp.cgs97.entities.Poll;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter showing the terminated polls as a list
 * This class is used in the Android ListView
 * @author von Bergen Philémon
 */
public class PollListAdapter extends ArrayAdapter<Poll> {

	private Context context;
	private List<Poll> values;

	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of polls that have to be listed
	 */
	public PollListAdapter(Context context, int textViewResourceId, List<Poll> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
		this.values=objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			view =  inflater.inflate(R.layout.list_item_poll, parent, false);
		} else {
			view = convertView;
		}

		TextView tvPollQuestion =  (TextView)view.findViewById(R.id.textview_content);
		tvPollQuestion.setText(this.values.get(position).getQuestion());
		
		return view;
	}

	public void onItemClick(AdapterView<?> listview, View view, int position, long id) {

		// extract the object assigned to the position which has been clicked
		Poll poll = (Poll) listview.getAdapter().getItem(position);

		Intent intent = new Intent(context, DisplayResultActivity.class);
		intent.putExtra("poll", poll);
		intent.putExtra("saveToDb", false);
		context.startActivity(intent);


	}

}
