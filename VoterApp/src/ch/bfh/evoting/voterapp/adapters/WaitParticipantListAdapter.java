package ch.bfh.evoting.voterapp.adapters;

import java.util.List;

import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Participant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Adapter listing the participants that already have voted and those who are still voting
 * This class is used in the Android ListView
 * @author Philémon von Bergen
 *
 */
public class WaitParticipantListAdapter extends ArrayAdapter<Participant> {

	private Context context;
	private List<Participant> values;
	
	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of participants that have to be displayed
	 */
	public WaitParticipantListAdapter(Context context, int textViewResourceId, List<Participant> objects) {
		super(context, textViewResourceId, objects);
		this.context=context;
		this.values=objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);

		View view;
		if (null == convertView) {
			//when view is created
			view =  inflater.inflate(R.layout.list_item_participant_wait, parent, false);
		} else {
			view = convertView;
		}
		
		ImageView ivCastImage = (ImageView)view.findViewById(R.id.imageview_cast_img);
		ProgressBar pgWaitForCast = (ProgressBar)view.findViewById(R.id.progress_bar_waitforcast);
		
		//set the correct image
		if(this.values.get(position).hasVoted()==true){
			pgWaitForCast.setVisibility(View.GONE);
			ivCastImage.setVisibility(View.VISIBLE);
		} else {
			pgWaitForCast.setVisibility(View.VISIBLE);
			ivCastImage.setVisibility(View.GONE);
		}

		//set the participant identification
		TextView tvParticipant =  (TextView)view.findViewById(R.id.textview_participant_identification);
		tvParticipant.setText(this.values.get(position).getIdentification());
		
		return view;
	}
	
	@Override
	public Participant getItem (int position)
	{
		return super.getItem (position);
	}

}
