package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.math.BigInteger;

import android.content.Context;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.VoteMessage.Type;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.classes.StandardNonInteractiveSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageProofGenerator;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import ch.bfh.unicrypt.math.function.classes.PartiallyAppliedFunction;
import ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
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
public class SetupRoundAction extends AbstractAction {


	public SetupRoundAction(Context context, String messageTypeToListenTo,
			ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 20000);
	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException, InterruptedException {
		super.doAction(message, entity, transition, actionType);
		Log.d(TAG,"Setup round started");

		me.setXi(poll.getZ_q().getRandomElement());

		me.setAi(poll.getGenerator().selfApply(me.getXi()));

		//Function g^r
		Function f = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(poll.getZ_q(), 1), PartiallyAppliedFunction.getInstance(SelfApplyFunction.getInstance(poll.getG_q(), poll.getZ_q()), poll.getGenerator(), 0));

		//Generator and index of the participant has also to be hashed in the proof
		Element index = N.getInstance().getElement(BigInteger.valueOf(me.getProtocolParticipantIndex()));
		StringElement proverId = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII).getElement(me.getUniqueId());
		Tuple otherInput = Tuple.getInstance(poll.getGenerator(), index, proverId);

		SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator.getInstance(
				f.getCoDomain(), (SemiGroup)f.getCoDomain(), ZMod.getInstance(f.getDomain().getMinimalOrder()), otherInput);

		PreimageProofGenerator spg = PreimageProofGenerator.getInstance(scg, f);

		me.setProofForXi(spg.generate(me.getXi(), me.getAi()));

		ProtocolMessageContainer m = new ProtocolMessageContainer(me.getAi(), me.getProofForXi());
		sendMessage(m, Type.VOTE_MESSAGE_SETUP);

		if(this.readyToGoToNextState()){
			goToNextState();
		}
	}

//	@Override
//	protected void processMessage(ProtocolMessageContainer message,  ProtocolParticipant senderParticipant) {
//
//		Log.d(TAG,"Setup message received from "+senderParticipant.getIdentification());
////		Intent intent = new Intent(context, ProcessingService.class);
////		intent.putExtra("round", (Serializable) Round.setup);
////		intent.putExtra("message", (Serializable) message);
////		intent.putExtra("sender", (Serializable) senderParticipant);
////		context.startService(intent);
//		senderParticipant.setAi(message.getValue());
//		senderParticipant.setProofForXi(message.getProof());
//		
//		//Verify proof of knowledge of xi
//
//		Function f = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(poll.getZ_q(), 1), PartiallyAppliedFunction.getInstance(SelfApplyFunction.getInstance(poll.getG_q(), poll.getZ_q()), poll.getGenerator(), 0));
//
//		//Generator and index of the participant has also to be hashed in the proof
//		Element index = N.getInstance().getElement(BigInteger.valueOf(senderParticipant.getProtocolParticipantIndex()));
//		StringElement proverId = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII).getElement(senderParticipant.getUniqueId());
//		Tuple otherInput = Tuple.getInstance(poll.getGenerator(), index, proverId);
//
//		SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator.getInstance(
//				f.getCoDomain(), (SemiGroup)f.getCoDomain(), ZMod.getInstance(f.getDomain().getMinimalOrder()), otherInput);
//
//		PreimageProofGenerator spg = PreimageProofGenerator.getInstance(scg, f);
//
//		//if proof is false, exclude participant
//		if(!spg.verify(message.getProof(), message.getValue()).getBoolean()){
//			poll.getExcludedParticipants().put(senderParticipant.getUniqueId(), senderParticipant);
//		}
//
//		if(this.readyToGoToNextState()){
//			goToNextState();
//		}
//	}
	
	@Override
	public void savedProcessedMessage(Round round, String sender, ProtocolMessageContainer message, boolean exclude){
		if(round!=Round.setup){
			Log.w(TAG, "Not saving value of processed message since they are value of a previous state.");
		}
		
		ProtocolParticipant senderParticipant = (ProtocolParticipant) poll.getParticipants().get(sender);
		senderParticipant.setAi(message.getValue());
		senderParticipant.setProofForXi(message.getProof());
		
		if(exclude){
			poll.getExcludedParticipants().put(sender, senderParticipant);
		}
		
		super.savedProcessedMessage(round, sender, message, exclude);
	}


	@Override
	protected void goToNextState() {
		super.goToNextState();
		try {
			((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new AllSetupMessagesReceivedEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}



}
