package ch.bfh.evoting.voterapp.cgs97.adapters;

import java.util.List;

import ch.bfh.evoting.voterapp.cgs97.R;
import ch.bfh.evoting.voterapp.cgs97.db.PollDbHelper;
import ch.bfh.evoting.voterapp.cgs97.entities.Poll;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * List adapter showing a list of the open polls
 * 
 * @author von Bergen Philémon
 */
public class PollArchiveAdapter extends ArrayAdapter<Poll> {

	private Context context;
	private List<Poll> values;

	/**
	 * Create an adapter object
	 * 
	 * @param context
	 *            android context
	 * @param textViewResourceId
	 *            id of the layout that must be inflated
	 * @param objects
	 *            list of options that have to be listed
	 */
	public PollArchiveAdapter(Context context, int textViewResourceId,
			List<Poll> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.values = objects;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			view = inflater.inflate(R.layout.list_item_poll, parent,
					false);
		} else {
			view = convertView;
		}

		TextView tvContent = (TextView) view
				.findViewById(R.id.textview_content);
		tvContent.setText(this.values.get(position).getQuestion());
		view.setId(values.get(position).getId());

		ImageButton btnDelete = (ImageButton) view
				.findViewById(R.id.button_deleteoption);

		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PollDbHelper.getInstance(context).deletePoll(values.get(position).getId());
				values.remove(position);
				notifyDataSetChanged();
			}
		});

		return view;
	}
}
