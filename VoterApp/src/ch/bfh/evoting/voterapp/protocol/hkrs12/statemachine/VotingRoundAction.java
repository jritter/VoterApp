package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.io.Serializable;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.protocol.DummyProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.VoteService;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

import com.continuent.tungsten.fsm.core.Action;
import com.continuent.tungsten.fsm.core.Entity;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.FiniteStateException;
import com.continuent.tungsten.fsm.core.Transition;
import com.continuent.tungsten.fsm.core.TransitionFailureException;
import com.continuent.tungsten.fsm.core.TransitionRollbackException;

/**
 * Action executed in Commit step
 * @author Philémon von Bergen
 * 
 */
public class VotingRoundAction extends AbstractAction {


	private BroadcastReceiver stopReceiver;
	private AsyncTask<Object, Object, Object> sendVotesTask;

	public VotingRoundAction(final Context context, String messageTypeToListenTo,
			final ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 20000);		
	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException, InterruptedException {
		super.doAction(message, entity, transition, actionType);
		Log.d(TAG,"Voting round started");

		ProtocolMessageContainer m = new ProtocolMessageContainer(me.getBi(), null);
		sendMessage(m, Type.VOTE_MESSAGE_VOTE);

		if(this.readyToGoToNextState()){
			goToNextState();
		}
	}

	@Override
	protected void processMessage(ProtocolMessageContainer message,
			ProtocolParticipant senderParticipant) {

		Log.d(TAG,"Voting message received from "+senderParticipant.getIdentification());

		senderParticipant.setBi(message.getValue());

		if(this.readyToGoToNextState()){
			goToNextState();
		}
	}

	@Override
	protected void goToNextState() {
		super.goToNextState();
		try {
			if(poll.getExcludedParticipants().isEmpty()){
				((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new AllVotingMessagesReceivedEvent(null));
			} else {
				((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new NotAllMessageReceivedEvent(null));
			}
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}





}
