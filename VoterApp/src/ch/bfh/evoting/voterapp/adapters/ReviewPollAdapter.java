package ch.bfh.evoting.voterapp.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.util.Separator;

/**
 * List adapter showing a list of the options
 * 
 * @author von Bergen Philémon
 */
public class ReviewPollAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Object> data = new ArrayList<Object>();
	
	public ReviewPollAdapter(Context context, Poll poll) {
		this.context = context;
		data.add(new Separator("Question"));
		data.add(poll);
		data.add(new Separator("Options"));
		data.addAll(poll.getOptions());
		data.add(new Separator("Participants"));
		data.addAll(poll.getParticipants().values());
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		View view = null;
		LayoutInflater inflater = LayoutInflater.from(context);
		
		
		if (this.getItem(position) instanceof Poll){
			view = inflater.inflate(R.layout.list_item_question, parent, false);
			TextView tvSeparator = (TextView) view.findViewById(R.id.textview_question);
			tvSeparator.setText(((Poll)this.getItem(position)).getQuestion());
		}
		else if (this.getItem(position) instanceof Option){
			view = inflater.inflate(R.layout.list_item_option_poll, parent, false);
			TextView tvOption = (TextView)view.findViewById(R.id.textview_poll_option_review);
			tvOption.setText(((Option)this.getItem(position)).getText());
		}
		else if (this.getItem(position) instanceof Participant){
			
			Participant participant = (Participant)this.getItem(position);
			
			view = inflater.inflate(R.layout.list_item_participant_poll, parent, false);
			TextView tvParticipant = (TextView)view.findViewById(R.id.textview_participant_identification);
			tvParticipant.setText(participant.getIdentification());

			ImageView ivAcceptImage = (ImageView)view.findViewById(R.id.imageview_accepted_img);
			ProgressBar pgWaitForAccept = (ProgressBar)view.findViewById(R.id.progress_bar_waitforaccept);
			ImageView btnValidateReview = (ImageView)view.findViewById(R.id.button_validate_review);

			
			//set the correct image
			if(participant.hasAcceptedReview()){
				pgWaitForAccept.setVisibility(View.GONE);
				ivAcceptImage.setVisibility(View.VISIBLE);
				btnValidateReview.setVisibility(View.GONE);
			} else {
				if(participant.getUniqueId().equals(AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId())){
					pgWaitForAccept.setVisibility(View.GONE);
					ivAcceptImage.setVisibility(View.GONE);
					btnValidateReview.setVisibility(View.VISIBLE);
					btnValidateReview.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_ACCEPT_REVIEW, ""));
						}
					});
				} else {
					pgWaitForAccept.setVisibility(View.VISIBLE);
					ivAcceptImage.setVisibility(View.GONE);
					btnValidateReview.setVisibility(View.GONE);
				}
			}
		}
		else if (this.getItem(position) instanceof Separator){
			view = inflater.inflate(R.layout.list_item_separator, parent, false);
			TextView tvSeparator = (TextView) view.findViewById(R.id.textview_separator);
			tvSeparator.setText(((Separator)this.getItem(position)).getText());
		}
		else {
			view = inflater.inflate(R.layout.list_item_string, parent, false);
			TextView tvSeparator = (TextView) view.findViewById(R.id.textview_content);
			tvSeparator.setText(this.getItem(position).toString());
		}
		
		return view;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
