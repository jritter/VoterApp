package ch.bfh.evoting.voterapp.protocol;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

public class HKRS12ProtocolInterface extends ProtocolInterface {

	private Context context;
	private StateMachineManager stateMachineManager;

	public HKRS12ProtocolInterface(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void showReview(final Poll poll) {

		//send broadcast to dismiss the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		new AsyncTask<Object, Object, Object>(){

			@Override
			protected Object doInBackground(Object... arg0) {
				//do some protocol specific stuff to the poll
				//transform poll in protocol poll
				ProtocolPoll newPoll = new ProtocolPoll(poll);

				//compute Baudron et al
				Element two = newPoll.getZ_q().getElement(BigInteger.valueOf(2));
				BigInteger pow2i;
				int m=-1;
				do{
					m++;
					pow2i = two.getValue().pow(m);
				}while(pow2i.compareTo(BigInteger.valueOf(poll.getNumberOfParticipants()))<1);

				//transform options in protocol options and create a "generator" for each option
				int j=0;
				List<Option> options = new ArrayList<Option>();
				for(Option op : poll.getOptions()){
					ProtocolOption protocolOption = new ProtocolOption(op);
					protocolOption.setRepresentation(newPoll.getZ_q().getElement(two.getValue().pow(j*m)));
					options.add(protocolOption);
					j++;
				}
				newPoll.setOptions(options);

				//transform participants in protocol participants
				Map<String, Participant> participants = new TreeMap<String,Participant>();
				int i=0;
				for(Participant p : poll.getParticipants().values()){
					ProtocolParticipant protocolParticipant = new ProtocolParticipant(p);
					protocolParticipant.setProtocolParticipantIndex(i);
					i++;
					participants.put(p.getUniqueId(), protocolParticipant);
				}
				newPoll.setParticipants(participants);

				//Send poll to other participants
				VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)newPoll);
				AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);


//				//send broadcast to dismiss the wait dialog
//				Intent intent1 = new Intent(BroadcastIntentTypes.dismissWaitDialog);
//				LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

				//Send a broadcast to start the next activity
				Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
				intent.putExtra("poll", (Serializable)newPoll);
				intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	


				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	@Override
	public void beginVotingPeriod(final Poll poll) {

		//Send a broadcast to start the vote activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		//send broadcast to dismiss the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		if(AndroidApplication.getInstance().isAdmin()){
			//called when admin want to begin voting period

			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);	


		} 

		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {
				//Do some protocol specific stuff
				stateMachineManager = new StateMachineManager(context, (ProtocolPoll)poll);
				new Thread(stateMachineManager).start();
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
	public void vote(final Option selectedOption, final Poll poll) {

		

//		new AsyncTask<Object, Object, Object>(){
//			@Override
//			protected Object doInBackground(Object... arg0) {
//				//do some protocol specific stuff
//				int index = -1;
//				int j = 0;
//				for(Option op : poll.getOptions()){
//					if(op.equals(selectedOption)){
//						index = j;
//						break;
//					}
//					j++;
//				}
//				Intent i = new Intent(BroadcastIntentTypes.vote);
//				i.putExtra("option", (Serializable)selectedOption);
//				i.putExtra("index", index);
//				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
//
//
//				return null;
//			}
//		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		//Send a broadcast to start the wait for vote activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		
		Log.e("HKRS12","UI thread: "+Thread.currentThread().getId());
		
		new Thread(){

			@Override
			public void run() {
				Log.e("HKRS12","Vote processing thread: "+Thread.currentThread().getId());

				
				
				//do some protocol specific stuff
				int index = -1;
				int j = 0;
				for(Option op : poll.getOptions()){
					if(op.equals(selectedOption)){
						index = j;
						break;
					}
					j++;
				}
				Intent i = new Intent(BroadcastIntentTypes.vote);
				i.putExtra("option", (Serializable)selectedOption);
				i.putExtra("index", index);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);

				super.run();
			}
			
		}.start();
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


	public StateMachineManager getStateMachineManager(){
		return this.stateMachineManager;
	}

	public void reset(){
		this.stateMachineManager = null;
	}

}
