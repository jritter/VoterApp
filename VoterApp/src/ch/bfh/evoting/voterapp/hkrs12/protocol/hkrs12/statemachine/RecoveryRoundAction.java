package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.statemachine;

import java.math.BigInteger;

import android.content.Context;
import android.util.Log;
import ch.bfh.evoting.voterapp.hkrs12.AndroidApplication;
import ch.bfh.evoting.voterapp.hkrs12.entities.Participant;
import ch.bfh.evoting.voterapp.hkrs12.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.hkrs12.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.classes.StandardNonInteractiveSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageEqualityProofGenerator;
import ch.bfh.unicrypt.crypto.schemes.commitment.classes.StandardCommitmentScheme;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;

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
public class RecoveryRoundAction extends AbstractAction {

	public RecoveryRoundAction(Context context, String messageTypeToListenTo,
			ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 20000);
	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException, InterruptedException {
		super.doAction(message, entity, transition, actionType);
		Log.d(TAG,"Recovery round started");

		if(/*poll.getExcludedParticipants().size()==1 &&*/ poll.getExcludedParticipants().containsKey(me.getUniqueId())){
			//this means that I was excluded
			//so I have nothing to do here
			return;
		}

		Element productNumerator = poll.getG_q().getElement(BigInteger.valueOf(1));
		Element productDenominator = poll.getG_q().getElement(BigInteger.valueOf(1));

		for(Participant p : poll.getExcludedParticipants().values()){
			ProtocolParticipant p2 = (ProtocolParticipant) p;
			if(p2.getProtocolParticipantIndex()<me.getProtocolParticipantIndex()){
				productDenominator = productDenominator.apply(p2.getAi());
			} else if(me.getProtocolParticipantIndex()<p2.getProtocolParticipantIndex()){
				productNumerator = productNumerator.apply(p2.getAi());
			}
		}

		me.setHiHat(productNumerator.applyInverse(productDenominator));
		
		StandardCommitmentScheme csRecovery = StandardCommitmentScheme.getInstance(me.getHiHat());	
		me.setHiHatPowXi(csRecovery.commit(me.getXi()));

		//compute proof of equality between discrete logs
		
		//Function g^r
		StandardCommitmentScheme csSetup = StandardCommitmentScheme.getInstance(poll.getGenerator());	
		Function f1 = csSetup.getCommitmentFunction();

		//Function h_hat^r
		Function f2 = csRecovery.getCommitmentFunction();

		ProductFunction f = ProductFunction.getInstance(f1, f2);
		
		Tuple otherInput3 = Tuple.getInstance(me.getDataToHash(), poll.getDataToHash());
		
		SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator.getInstance(f, otherInput3);

		PreimageEqualityProofGenerator piepg = PreimageEqualityProofGenerator.getInstance(scg, f1,f2);

		Tuple publicInput = Tuple.getInstance(me.getAi(), me.getHiHatPowXi());
		me.setProofForHiHat(piepg.generate(me.getXi(), publicInput));
		
		numberMessagesReceived++;
		ProtocolMessageContainer m = new ProtocolMessageContainer(me.getHiHatPowXi(), me.getProofForHiHat(), me.getHiHat());
		sendMessage(m, Type.VOTE_MESSAGE_RECOVERY);

		if(this.readyToGoToNextState()){
			goToNextState();
		}
	}
	
	@Override
	public void savedProcessedMessage(Round round, String sender, ProtocolMessageContainer message, boolean exclude){
		if(round!=Round.recovery){
			Log.w(TAG, "Not saving value of processed message since they are value of a previous state.");
		}
		
		ProtocolParticipant senderParticipant = (ProtocolParticipant) poll.getParticipants().get(sender);
		senderParticipant.setHiHatPowXi(message.getValue());
		senderParticipant.setHiHat(message.getComplementaryValue());
		senderParticipant.setProofForHiHat(message.getProof());
		
		if(exclude){
			Log.w(TAG, "Excluding participant "+senderParticipant.getIdentification()+" ("+sender+") because of a message processing problem.");
			poll.getExcludedParticipants().put(sender, senderParticipant);
		}
		
		super.savedProcessedMessage(round, sender, message, exclude);
	}

	@Override
	protected void goToNextState() {
		super.goToNextState();
		try {
			((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new AllRecoveringMessagesReceivedEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}



}
