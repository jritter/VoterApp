package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.ElGamalEncryptionValidityProofGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.helper.Alphabet;

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
public class CommitmentRoundAction extends AbstractAction {



	private BroadcastReceiver voteDone;
	//	private AsyncTask<Object, Object, Object> sendVotesTask;
	private BroadcastReceiver stopReceiver;
	@SuppressWarnings("unused")
	private AsyncTask<Object, Object, Object> sendVotesTask;
	public boolean stopSendingUpdateVote = false;

	public CommitmentRoundAction(final Context context, String messageTypeToListenTo, final ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 120000);

		voteDone = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						executeCallback(intent);
						return null;
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(voteDone, new IntentFilter(BroadcastIntentTypes.vote));

		//Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent intent) {
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						//send broadcast to dismiss the wait dialog
						Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
						LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

						for(Participant p:poll.getParticipants().values()){
							if(!messagesReceived.containsKey(p.getUniqueId())){
								poll.getExcludedParticipants().put(p.getUniqueId(), p);
							}
						}
						goToNextState();
						return null;
					}
				}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));

	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException, InterruptedException {

		super.doAction(message, entity, transition, actionType);
		Log.d(TAG,"Commitment round started");

		//send broadcast to dismiss the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		Element productNumerator = poll.getG_q().getElement(BigInteger.valueOf(1));
		Element productDenominator = poll.getG_q().getElement(BigInteger.valueOf(1));

		for(Participant p : poll.getParticipants().values()){
			ProtocolParticipant p2 = (ProtocolParticipant) p;
			if(p2.getProtocolParticipantIndex()>me.getProtocolParticipantIndex()){
				productDenominator = productDenominator.apply(p2.getAi());
			} else if(me.getProtocolParticipantIndex()>p2.getProtocolParticipantIndex()){
				productNumerator = productNumerator.apply(p2.getAi());
			}
		}

		me.setHi(productNumerator.apply(productDenominator.invert()));

		//send broadcast to dismiss the wait dialog
		Intent intent2 = new Intent(BroadcastIntentTypes.dismissWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
	}

	@Override
	public void savedProcessedMessage(Round round, String sender, ProtocolMessageContainer message, boolean exclude){
		if(round!=Round.commit){
			Log.w(TAG, "Not saving value of processed message since they are value of a previous state.");
		}

		Log.e(TAG, "Saving proof of validity recvd "+sender);
		ProtocolParticipant senderParticipant = (ProtocolParticipant) poll.getParticipants().get(sender);
		senderParticipant.setProofValidVote(message.getProof());

		if(exclude){
			poll.getExcludedParticipants().put(sender, senderParticipant);
		}
		super.savedProcessedMessage(round, sender, message, exclude);

	}

	@Override
	protected void goToNextState() {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(voteDone);
		super.goToNextState();
		Log.e(TAG, "Going to next state");
		try {
			((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new AllCommitMessagesReceivedEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
		Timer timer = new Timer();
		TaskTimer timerTask = new TaskTimer();
		timer.schedule(timerTask, 5000);
	}
	
	/**
	 * Task run on timer tick
	 * 
	 */
	private class TaskTimer extends TimerTask {

		@Override
		public void run() {
			stopSendingUpdateVote = true;
		}
	};

	public void executeCallback(Intent data) {

		Log.e(TAG,"executeCallback thread id: "+Thread.currentThread().getId());

		//notify UI about new incomed vote
		Log.e(TAG, "Sending update vote broadcast");
		me.setHasVoted(true);
		numberMessagesReceived++;
		//inform GUI about new vote message received
		sendVotesTask = new AsyncTask<Object, Object, Object>(){

			@Override
			protected Object doInBackground(Object... arg0) {
				while(!stopSendingUpdateVote){

					//notify UI about new incomed vote
					Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
					i.putExtra("votes", numberMessagesReceived);
					i.putExtra("options", (Serializable)poll.getOptions());
					i.putExtra("participants", (Serializable)poll.getParticipants());
					LocalBroadcastManager.getInstance(context).sendBroadcast(i);
					SystemClock.sleep(1000);
				}
				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		ProtocolOption option = (ProtocolOption)data.getSerializableExtra("option");
		int index = data.getIntExtra("index", -1);

		me.setBi(me.getHi().selfApply(me.getXi()).apply(poll.getGenerator().selfApply(option.getRepresentation())));

		//compute validity proof
		Element[] possibleVotes = new Element[poll.getOptions().size()];
		int i=0;
		for(Option op:poll.getOptions()){
			possibleVotes[i] = poll.getGenerator().selfApply(((ProtocolOption)op).getRepresentation());
			i++;
		}

		ElGamalEncryptionScheme<GStarMod, Element> ees = ElGamalEncryptionScheme.getInstance(poll.getGenerator());
		StringElement proverId = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII).getElement(me.getUniqueId());
		SigmaChallengeGenerator scg = ElGamalEncryptionValidityProofGenerator.createNonInteractiveChallengeGenerator(ees, possibleVotes.length, proverId);
		ElGamalEncryptionValidityProofGenerator vpg = ElGamalEncryptionValidityProofGenerator.getInstance(
				scg, ees, me.getHi(), possibleVotes);

		//simulate the ElGamal cipher text (a,b) = (ai,bi);
		Tuple publicInput = Tuple.getInstance(me.getAi(), me.getBi());
		Tuple privateInput = vpg.createPrivateInput(me.getXi(), index);
		me.setProofValidVote(vpg.generate(privateInput, publicInput));

		ProtocolMessageContainer m = new ProtocolMessageContainer(null, me.getProofValidVote(), null);
		sendMessage(m, Type.VOTE_MESSAGE_COMMIT);

		if(readyToGoToNextState()){
			goToNextState();
		}

	}


}
