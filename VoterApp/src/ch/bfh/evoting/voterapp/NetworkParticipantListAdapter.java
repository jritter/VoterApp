package ch.bfh.evoting.voterapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ch.bfh.evoting.votinglib.entities.Participant;

/**
 * Adapter listing the participants that are present in the network and if they are included or not in the electorate
 * This class is used in the Android ListView
 * @author Philémon von Bergen
 *
 */
public class NetworkParticipantListAdapter extends ArrayAdapter<Participant> {

	private Context context;
	private List<Participant> values;
	
	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of participants that have to be displayed
	 */
	public NetworkParticipantListAdapter(Context context, int textViewResourceId, List<Participant> objects) {
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
			view =  inflater.inflate(R.layout.list_item_participant_network, parent, false);
		} else {
			view = convertView;
		}
		
		//Set the corresponding if the administrator has selected the participant a part of the electorate
		ImageView ivSelected = (ImageView)view.findViewById(R.id.imageview_participant_selected);
		if(this.values.get(position).isSelected()){
			ivSelected.setImageResource(R.drawable.included);
		} else {
			ivSelected.setImageResource(R.drawable.excluded);
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
