package ch.bfh.evoting.voterapp.cgs97.adapters;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ch.bfh.evoting.voterapp.cgs97.R;
import ch.bfh.evoting.voterapp.cgs97.AndroidApplication;
import ch.bfh.evoting.voterapp.cgs97.ElectorateActivity;
import ch.bfh.evoting.voterapp.cgs97.entities.Participant;
import ch.bfh.evoting.voterapp.cgs97.entities.VoteMessage;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Adapter listing the participants that are present in the network and if they are included or not in the electorate
 * This class is used in the Android ListView
 * @author Philémon von Bergen
 *
 */
public class AdminNetworkParticipantListAdapter extends ArrayAdapter<Participant> {

	private Context context;
	private List<Participant> values;

	/**
	 * Create an adapter object
	 * @param context android context
	 * @param textViewResourceId id of the layout that must be inflated
	 * @param objects list of participants that have to be displayed
	 */
	public AdminNetworkParticipantListAdapter(Context context, int textViewResourceId, List<Participant> objects) {
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
			view =  inflater.inflate(R.layout.list_item_participant_network_admin, parent, false);
		} else {
			view = convertView;
		}

		final CheckBox cbInElectorate = (CheckBox) view.findViewById(R.id.checkbox_inelectorate);
		final TextView tvContent = (TextView) view.findViewById(R.id.textview_content);

		//set the participant identification
		tvContent.setText(this.values.get(position).getIdentification());

		view.setTag(position);
		cbInElectorate.setTag(position);
		//set the click listener
		OnClickListener click = new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(!(v instanceof CheckBox))
					cbInElectorate.toggle();

				if(cbInElectorate.isChecked()){
					values.get((Integer)v.getTag()).setSelected(true);
				} else {
					values.get((Integer)v.getTag()).setSelected(false);
				}

				//Send the updated list of participants in the network over the network
				int numberOfSelectedParticipants = 0;
				Map<String,Participant> map = new TreeMap<String,Participant>();
				for(Participant p : values){
					map.put(p.getUniqueId(), p);
					if (p.isSelected()){
						numberOfSelectedParticipants++;
					}
				}
				
				((ElectorateActivity) context).updateSeekBar(numberOfSelectedParticipants, numberOfSelectedParticipants - 1);
				
				VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ELECTORATE, (Serializable)map);
				AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);
			}
		};
		view.setOnClickListener(click);
		cbInElectorate.setOnClickListener(click);

		if(values.get(position).isSelected()){
			cbInElectorate.setChecked(true);
		} else {
			cbInElectorate.setChecked(false);
		}
		
		return view;
	}

	@Override
	public Participant getItem (int position)
	{
		return super.getItem (position);
	}

}
