package ch.bfh.evoting.voterapp.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

public class HKRS12ProtocolInterface extends ProtocolInterface {

	private Context context;

	public HKRS12ProtocolInterface(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void showReview(Poll poll) {
		//do some protocol specific stuff to the poll
		//transform poll in protocol poll
		poll = new ProtocolPoll(poll);
		
		//transform options in protocol options
		List<Option> options = new ArrayList<Option>();
		for(Option op : poll.getOptions()){
			ProtocolOption protocolOption = new ProtocolOption(op);
			options.add(protocolOption);
		}
		poll.setOptions(options);
		
		//transform participants in protocol participants
		Map<String, Participant> participants = new TreeMap<String,Participant>();
		int i=0;
		for(Participant p : poll.getParticipants().values()){
			ProtocolParticipant protocolParticipant = new ProtocolParticipant(p);
			protocolParticipant.setProtocolParticipantIndex(i);
			i++;
			participants.put(p.getUniqueId(), protocolParticipant);
		}
		poll.setParticipants(participants);

		//Send poll to other participants
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)poll);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		//Send a broadcast to start the next activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	
	}

	@Override
	public void beginVotingPeriod(Poll poll) {

		if(AndroidApplication.getInstance().isAdmin()){
			//called when admin want to begin voting period
			
			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

			//Do some protocol specific stuff
			//TODO start state machine here
			//should probably wait before doing next stuff and display a wait dialog

			//start service listening to incoming votes and stop voting period events
			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));

			//Send a broadcast to start the review activity
			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
			intent.putExtra("poll", (Serializable)poll);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} else {
			//called when start message received from admin
			
			//Do some protocol specific stuff
			
			//start service listening to incoming votes and stop voting period events
			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));
		}
	}

	@Override
	public void endVotingPeriod() {
		//Send stop signal over the network
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_STOP_POLL, null);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		//send broadcast containing the stop voting period event
		Intent i = new Intent(BroadcastIntentTypes.stopVote);
		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
		//The VoteService listens to this broadcast and a calls the computeResult method
	}

	@Override
	public void vote(Option selectedOption, Poll poll) {
		//do some protocol specific stuff
		//TODO notify state machine

		//send the vote over the network
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, selectedOption));

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * Method called when the result must be computed (all votes received or stop asked by the admin)
	 * @param poll poll object
	 */
	public void computeResult(Poll poll){

		context.stopService(new Intent(context, VoteService.class));

		//do some protocol specific stuff
		//TODO 
		//go through compute result and set percentage result
		List<Option> options = poll.getOptions();
		int votesReceived = 0;
		for(Option option : options){
			votesReceived += option.getVotes();
		}
		for(Option option : options){
			if(votesReceived!=0){
				option.setPercentage(option.getVotes()*100/votesReceived);
			} else {
				option.setPercentage(0);
			}
		}

		poll.setTerminated(true);

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	protected void handleReceivedPoll(Poll poll, String sender) {
		//do some protocol specific stuff
		//check if speaking the same language
		if(!(poll instanceof ProtocolPoll)){
			//TODO broadcast
			throw new RuntimeException("Not using the same protocol!");
		}

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable) poll);
		intent.putExtra("sender", sender);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}





}
