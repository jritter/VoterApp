package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;


import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

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

	public VotingRoundAction(final Context context, String messageTypeToListenTo,
			final ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 30000);		

	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException, InterruptedException {
		super.doAction(message, entity, transition, actionType);
		Log.d(TAG,"Voting round started");

		//send broadcast to show the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		numberMessagesReceived++;
		ProtocolMessageContainer m = new ProtocolMessageContainer(me.getBi(), null, me.getHi());
		sendMessage(m, Type.VOTE_MESSAGE_VOTE);

		if(this.readyToGoToNextState()){
			goToNextState();
		}
	}

	@Override
	public void savedProcessedMessage(Round round, String sender, ProtocolMessageContainer message, boolean exclude){
		if(round!=Round.voting){
			Log.w(TAG, "Not saving value of processed message since they are value of a previous state.");
		}

		ProtocolParticipant senderParticipant = (ProtocolParticipant) poll.getParticipants().get(sender);
		senderParticipant.setHi(message.getComplementaryValue());
		senderParticipant.setBi(message.getValue());

		if(exclude){
			Log.w(TAG, "Excluding participant "+senderParticipant.getIdentification()+" ("+sender+") because of a message processing problem.");
			poll.getExcludedParticipants().put(sender, senderParticipant);
		}

		super.savedProcessedMessage(round, sender, message, exclude);
	}

	@Override
	protected void goToNextState() {
		super.goToNextState();

		StateMachineManager smm = ((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager();
		try {
			if(poll.getExcludedParticipants().isEmpty()){
				if(smm!=null)
					smm.getStateMachine().applyEvent(new AllVotingMessagesReceivedEvent(null));
			} else {
				if(smm!=null)
					smm.getStateMachine().applyEvent(new NotAllMessageReceivedEvent(null));
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
