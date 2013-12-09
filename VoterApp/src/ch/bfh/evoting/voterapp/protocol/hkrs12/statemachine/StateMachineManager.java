package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ch.bfh.evoting.voterapp.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.*;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;

import com.continuent.tungsten.fsm.core.EntityAdapter;
import com.continuent.tungsten.fsm.core.Event;
import com.continuent.tungsten.fsm.core.EventTypeGuard;
import com.continuent.tungsten.fsm.core.FiniteStateException;
import com.continuent.tungsten.fsm.core.Guard;
import com.continuent.tungsten.fsm.core.State;
import com.continuent.tungsten.fsm.core.StateMachine;
import com.continuent.tungsten.fsm.core.StateTransitionMap;
import com.continuent.tungsten.fsm.core.StateType;

/**
 * Creation of the State Machine managing the flow of the protocol
 * @author Philémon von Bergen
 *
 */
public class StateMachineManager implements Runnable {

	private static final String TAG = StateMachineManager.class.getSimpleName();
	private StateMachine sm;
	private SetupRoundAction setupRoundAction;
	private CommitmentRoundAction commitmentRoundAction;
	private VotingRoundAction votingRoundAction;
	private TallyingAction tallyingAction;
	private RecoveryRoundAction recoveryRoundAction;
	private Context context;
	private ProtocolPoll poll;

	/**
	 * Create an object managing the state machine
	 * @param 
	 */
	public StateMachineManager(Context context, ProtocolPoll poll) {
		this.context = context;
		this.poll = poll;
		LocalBroadcastManager.getInstance(context).registerReceiver(applyTransition, new IntentFilter("applyTransition"));
	}

	/**
	 * Create and run the state machine
	 */
	@Override
	public void run() {

		/*Create the state machine*/
		StateTransitionMap stmap = new StateTransitionMap();
		
		/*Define actions*/
		setupRoundAction = new SetupRoundAction(context, BroadcastIntentTypes.setupMessage, poll);
		commitmentRoundAction = new CommitmentRoundAction(context, BroadcastIntentTypes.commitMessage, poll);
		votingRoundAction = new VotingRoundAction(context, BroadcastIntentTypes.newVote, poll);
		tallyingAction = new TallyingAction(context, "nothing", poll);
		recoveryRoundAction = new RecoveryRoundAction(context, BroadcastIntentTypes.recoveryMessage, poll);

		/*Define states*/
		State begin = new State("begin", StateType.START, null, null);
		State setup = new State("setup", StateType.ACTIVE, setupRoundAction, null);
		State commit = new State("commit", StateType.ACTIVE, commitmentRoundAction, null);
		State vote = new State("vote", StateType.ACTIVE, votingRoundAction, null);
		State tally = new State("tally", StateType.ACTIVE, tallyingAction, null);
		State recovery = new State("recover", StateType.ACTIVE, recoveryRoundAction, null);
		State exit = new State("exit", StateType.END, null, null);

		
		/*Define Guards (=conditions) for transitions*/
		Guard startProtocol = new EventTypeGuard(StartProtocolEvent.class);
		Guard allSetupMessagesReceived = new EventTypeGuard(AllSetupMessagesReceivedEvent.class);
		Guard allCommitMessagesReceived = new EventTypeGuard(AllCommitMessagesReceivedEvent.class);
		Guard allVotingMessagesReceived = new EventTypeGuard(AllVotingMessagesReceivedEvent.class);
		Guard allRecoveryMessagesReceived = new EventTypeGuard(AllRecoveringMessagesReceivedEvent.class);
		Guard notAllMessageReceived = new EventTypeGuard(NotAllMessageReceivedEvent.class);
		Guard resultComputed = new EventTypeGuard(ResultComputedEvent.class);
		
		try {
			/*Add states*/
			stmap.addState(begin);
			stmap.addState(setup);
			stmap.addState(commit);
			stmap.addState(vote);
			stmap.addState(tally);
			stmap.addState(recovery);			
			stmap.addState(exit);
		
			/*Add transitions*/

			//Transition of state begin
			stmap.addTransition("begin-setup", startProtocol, begin, null, setup);
			//Transition of state setup
			stmap.addTransition("setup-commit", allSetupMessagesReceived, setup, null, commit);
			//Transition of state commit
			stmap.addTransition("commit-vote", allCommitMessagesReceived, commit, null, vote);
			//Transition of state vote
			stmap.addTransition("vote-tally", allVotingMessagesReceived, vote, null, tally);
			stmap.addTransition("vote-recovery", notAllMessageReceived, vote, null, recovery);
			//Transition of state recovery
			stmap.addTransition("recovery-tally", allRecoveryMessagesReceived, recovery, null, tally);
			//Transition of state tally
			stmap.addTransition("tally-exit", resultComputed, tally, null, exit);
		
			/*Build map*/
			stmap.build();

		} catch (FiniteStateException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}

		/*Start state machine*/
		sm = new StateMachine(stmap, new EntityAdapter(this));
		try {
			sm.applyEvent(new StartProtocolEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the state machine
	 * @return
	 */
	public StateMachine getStateMachine(){
		return sm;
	}
	
	
	
	/**
	 * Listen for broadcasts asking to apply a transition
	 */
	private BroadcastReceiver applyTransition = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Event event = (Event)intent.getSerializableExtra("event");
			try {
				sm.applyEvent(event);
			} catch (FiniteStateException e) {
				Log.e(TAG, e.getMessage());
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	};
	
}
