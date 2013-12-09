package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;

import com.continuent.tungsten.fsm.core.Action;
import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;

/**
 * Abstract class representing the action done at a step of the protocol
 * This class implement the logic of receiving and sending a message
 * @author Philémon von Bergen
 *
 */
public abstract class AbstractAction implements Action {

	protected String TAG;

	protected Map<String,ProtocolMessageContainer> messagesReceived;

	private boolean timerIsRunning = false;
	private Timer timer;
	private TaskTimer timerTask;

	protected ProtocolPoll poll;

	protected Context context;

	protected boolean actionTerminated;

	protected ProtocolParticipant me;

	private long timeOut;

	/**
	 * Create an  Action object
	 * 
	 * @param messageTypeToListenTo the type of message that concern this action
	 * @param 
	 */
	public AbstractAction(Context context, String messageTypeToListenTo, ProtocolPoll poll, long timeOut) {

		TAG = this.getClass().getSimpleName();

		//Map of message received
		this.messagesReceived = new HashMap<String,ProtocolMessageContainer>();

		//Store some important entities that will be used in the actions
		this.context = context;
		this.poll = poll;
		this.timeOut = timeOut;
		
		for(Participant p:poll.getParticipants().values()){
			if(p.getUniqueId().equals(AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId())){
				this.me = (ProtocolParticipant)p;
			}
		}
		// Subscribing to the messageArrived events to update immediately
		LocalBroadcastManager.getInstance(context).registerReceiver(
				this.voteMessageReceiver, new IntentFilter(messageTypeToListenTo));

	}

	/**
	 * Method containing stuff to do in the current state 
	 */
	@Override
	public void doAction(Event arg0, Entity arg1, Transition arg2, int arg3)
			throws TransitionRollbackException, TransitionFailureException,
			InterruptedException {
		if(timeOut>0){
			this.startTimer(timeOut);
		}
	}


	/**
	 * Store the received messages if they are from the interesting type for this step 
	 */
	protected BroadcastReceiver voteMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			//Get the message
			ProtocolMessageContainer message = (ProtocolMessageContainer) intent.getSerializableExtra("message");
			String sender = intent.getStringExtra("sender");

			if(!messagesReceived.containsKey(sender)){
				messagesReceived.put(sender, message);
				Log.d(TAG, "Message received from "+sender);
			}
			else if (!messagesReceived.get(sender).equals(message)){
				//when resending message, check if message differs from one received previously
				Log.w(TAG, "Seems to receive a different message from same source !!!");
				return;
			}

			//When a new message arrives, we re-execute the action of the step
			if(actionTerminated){
				Log.w(TAG,"Action was called by an incoming message, but was already terminated");
				return;
			}
			
			ProtocolParticipant senderParticipant = findParticipant(sender);
			
			Log.e(TAG, "sender is "+senderParticipant + " while participants size in poll is "+poll.getParticipants().size() +" and unique id is "+sender);

			if(poll.getExcludedParticipants().containsKey(senderParticipant.getUniqueId())){
				Log.w(TAG, "Ignoring message from previously excluded participant!");
				return;
			}
			
			if(senderParticipant.equals(me)){
				Log.d(TAG, "Message received from myself, not needed to process it.");
				if(readyToGoToNextState()) goToNextState();
				return;
			}
			
			processMessage(message, senderParticipant);
		}
	};

	/**
	 * if the state of a participant has been
	 * changed and she has leaved the discussion, we can put her in the excluded participants
	 */
	protected BroadcastReceiver participantsLeaved = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//TODO
			
			//poll.getExcludedParticipants().put(key, value);
			if(readyToGoToNextState()){
				goToNextState();
			}
		}
	};

	/**
	 * Method called when a new message come in
	 * @param message message that comes in
	 * @param senderParticipant 
	 */
	protected abstract void processMessage(ProtocolMessageContainer message, ProtocolParticipant senderParticipant);

	/**
	 * Indicate if conditions to go to next step are fulfilled
	 * @return true if conditions are fulfilled, false otherwise
	 */
	protected boolean readyToGoToNextState(){
		Collection<Participant> activeParticipants = new ArrayList<Participant>();
		for(Participant p : poll.getParticipants().values()){
			activeParticipants.add(p);
		}
		activeParticipants.removeAll(poll.getExcludedParticipants().values());
		for(Participant p: activeParticipants){
			if(!this.messagesReceived.containsKey(p.getUniqueId())){
				Log.w(TAG, "Message from "+p.getUniqueId()+" ("+p.getIdentification()+") not received");
				return false;
			}
		}
		return true;
	}

	/**
	 * Implement logic before going and requesting to go to next state
	 */
	protected void goToNextState(){
		this.stopTimer();
		this.actionTerminated = true;
		LocalBroadcastManager.getInstance(context).unregisterReceiver(voteMessageReceiver);
	}


	/**
	 * Helper method used to send a message
	 * 
	 * @param messageContent content of the message to send
	 * @param type message type
	 * @param IP address to sent the message to if it has to be sent as unicast
	 */
	protected void sendMessage(ProtocolMessageContainer message, Type type) {
		VoteMessage m = new VoteMessage(type, (Serializable)message);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(m);
	}
	
	protected ProtocolParticipant findParticipant(String uniqueId){
		ProtocolParticipant senderParticipant = null;
		for(Participant p:poll.getParticipants().values()){
			if(p.getUniqueId().equals(uniqueId)){
				senderParticipant = (ProtocolParticipant)p;
				return senderParticipant;
			}
		}
		return null;
	}

	/* Timer methods */

	/**
	 * Start the timer used as time out
	 * @param time time out time in milliseconds
	 * @param numberOfResend number of resend request to send before forcing transition to next step
	 */
	protected void startTimer(long time) {
		if (!timerIsRunning) {
			timer = new Timer();

			timerTask = new TaskTimer();
			timer.schedule(timerTask, time);
			timerIsRunning = true;
		}
	}

	/**
	 * Stop the timer used as time out
	 */
	private void stopTimer() {
		if(timer!=null){
			timerTask.cancel();
			timer.cancel();
			timer.purge();
			timerIsRunning = false;
		}
	}

	/**
	 * Task run on timer tick
	 * 
	 */
	private class TaskTimer extends TimerTask {

		@Override
		public void run() {
			Log.e(TAG, "Time out !");
			stopTimer();
			for(Participant p:poll.getParticipants().values()){
				if(!messagesReceived.containsKey(p.getUniqueId())){
					poll.getExcludedParticipants().put(p.getUniqueId(), p);
				}
			}
			
			goToNextState();
		}
	};


	/**
	 * Unregister LocalBoradcastReceivers
	 */
	public void reset(){
		LocalBroadcastManager.getInstance(context).unregisterReceiver(voteMessageReceiver);
		//		LocalBroadcastManager.getInstance(context).unregisterReceiver(participantsLeaved);
	}

	

}
