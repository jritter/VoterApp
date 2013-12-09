package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.ElGamalEncryptionValidityProofGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageEqualityProofGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import ch.bfh.unicrypt.math.function.classes.PartiallyAppliedFunction;
import ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.math.helper.Alphabet;

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
public class TallyingAction extends AbstractAction {


	public TallyingAction(Context context, String messageTypeToListenTo,
			ProtocolPoll poll) {
		super(context, messageTypeToListenTo, poll, 0);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doAction(Event message, Entity entity, Transition transition,
			int actionType) throws TransitionRollbackException,
			TransitionFailureException {

		Log.d(TAG,"Tally started");

		long time0 = System.currentTimeMillis();

		//verify proofs
		//		for(int i=0; i<numberOfParticipants;i++){
		//			if(missing.contains(i)) continue;
		//
		//			//
		//			System.out.println("Verifying proofs for participant "+ i);
		//
		//			//Proof of knowledge of xi
		//
		//			Function f = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(Z_q, 1), PartiallyAppliedFunction.getInstance(SelfApplyFunction.getInstance(G_q, Z_q), generator, 0));
		//			StandardPreimageProofGenerator spg = StandardPreimageProofGenerator.getInstance(f);
		//
		//			//Generator and index of the participant has also to be hashed in the proof
		//			Element index = N.getInstance().getElement(BigInteger.valueOf(i));
		//		StringElement proverId = StringMonoid.getInstance(Alphabet.ALPHANUMERIC).getElement(me.getUniqueId().replace(":", "").replace(".", ""));
		//			Tuple otherInput = Tuple.getInstance(generator, index, proverId);
		//
		//			if(!spg.verify(proofOfKnowledge[i], ai[i], otherInput).getBoolean()){
		//				System.err.println("Knowledge proof for participant "+i+" is wrong");
		//				return;
		//			} else {
		//				System.out.println("Knowledge proof for participant "+i+" is correct");
		//			}
		//		
		//
		//
		//
		//			//verify validity proof
		//
		//			
		//			//if(validityProof[i]!=null){
		//				ElGamalEncryptionScheme<GStarMod, Element> ees = ElGamalEncryptionScheme.getInstance(generator);
		//				ElGamalEncryptionValidityProofGenerator vpg = ElGamalEncryptionValidityProofGenerator.getInstance(
		//						ees, hi[i], Tuple.getInstance(possibleVotes));
		//
		//				//simulate the ElGamal cipher text (a,b) = (ai,bi);
		//				Tuple publicInput = Tuple.getInstance(ai[i], bi[i]);
		//				
		//				if(!vpg.verify(validityProof[i], publicInput, proverId2).getBoolean()){
		//					System.err.println("Validity proof for participant "+i+" is wrong");
		//					return;
		//				} else {
		//					System.out.println("Validity proof for participant "+i+" is correct");
		//				}
		//			//}
		//
		//
		//			//Proof of equality between discrete logs
		//			if(recoveryNeeded){
		//				//Function g^r
		//				Function f1 = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(Z_q, 1),
		//						PartiallyAppliedFunction.getInstance(SelfApplyFunction.getInstance(G_q,Z_q), generator, 0));
		//
		//				//Function h_hat^r
		//				Function f2 = CompositeFunction.getInstance(MultiIdentityFunction.getInstance(Z_q, 1),
		//						PartiallyAppliedFunction.getInstance(SelfApplyFunction.getInstance(G_q,Z_q), hiHat[i], 0));
		//
		//
		//				PreimageEqualityProofGenerator piepg = PreimageEqualityProofGenerator.getInstance(f1,f2);
		//
		//				Tuple publicInput3 = Tuple.getInstance(ai[i], hiHatPowXi[i]);
		//				
		//				if(!piepg.verify(proofEquality[i], publicInput3, proverId3).getBoolean()){
		//					System.err.println("Validity proof for participant "+i+" is wrong");
		//					return;
		//				} else {
		//					System.out.println("Validity proof for participant "+i+" is correct");
		//				}
		//			}
		//
		//			
		//		}


		//compute result
		Element product = poll.getG_q().getElement(BigInteger.valueOf(1));
		Collection<Participant> activeParticipants = new ArrayList<Participant>();
		for(Participant p : poll.getParticipants().values()){
			activeParticipants.add(p);
		}
		activeParticipants.removeAll(poll.getExcludedParticipants().values());
		for(Participant p : activeParticipants){
			ProtocolParticipant p2 = (ProtocolParticipant)p;
			if(poll.getExcludedParticipants().isEmpty()){
				product=product.apply(p2.getBi());
			} else {
				product=product.apply(p2.getBi());
				product=product.apply(p2.getHiHatPowXi());
			}
		}

		//try to find combination corresponding to the computed result
		int[] result = computePossibleResults(activeParticipants.size(), product);
		if(result!=null){
			int i=0;
			for(Option op : poll.getOptions()){
				op.setVotes(result[i]);
				i++;
			}
		} else {
			Log.e(TAG, "Result not found");
		}
		long time1 = System.currentTimeMillis();
		
		for(Participant p : poll.getParticipants().values()){
			ProtocolParticipant p2 = (ProtocolParticipant)p;
			Log.e(TAG, "Participant "+ p2.getIdentification());
			Log.e(TAG, "xi "+ p2.getXi());
			Log.e(TAG, "ai "+ p2.getAi());
			Log.e(TAG, "hi "+ p2.getHi());
			Log.e(TAG, "bi "+ p2.getBi());
			Log.e(TAG, "hi hat "+ p2.getHiHat());
			Log.e(TAG, "hi hat pow xi "+ p2.getHiHatPowXi());
		}

		Log.e(TAG,"Time for tally round: "+(time1-time0)+" ms");

		goToNextState();
	}

	@Override
	protected void processMessage(ProtocolMessageContainer message,
			ProtocolParticipant senderParticipant) {
		// nothing to do here
	}

	@Override
	protected void goToNextState() {
		poll.setTerminated(true);

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		try {
			((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine().applyEvent(new ResultComputedEvent(null));
		} catch (FiniteStateException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Compute all combination of divide up MAX votes between Array.length candidate 
	 * Optimized for most of the votes in first columns
	 * @param array Array containing the possible combination
	 * @param resting Resting number of vote to divide up
	 * @param max Number of vote to divide up in total
	 * @param idx Index of the column (start with 0)
	 * @param result Obtained result of votes to find
	 */	
	private int[] computePossibleResults(int numberOfVotes, Element searchedResult) {

		//initialize array containing possible combination and the array receiving the result
		int[] voteForCandidates = new int[poll.getOptions().size()];
		int[] result = null;
		for(int i=0; i<voteForCandidates.length;i++){
			voteForCandidates[i]=0;
		}

		return computePossibleResultsRecursion(voteForCandidates,numberOfVotes,numberOfVotes,0, searchedResult, result);

	}

	/**
	 * Compute all combination of divide up MAX votes between Array.length candidate 
	 * Optimized for most of the votes in first columns
	 * @param array Array containing the possible combination
	 * @param resting Resting number of vote to divide up
	 * @param max Number of vote to divide up in total
	 * @param idx Index of the column (start with 0)
	 * @param result Obtained result of votes to find
	 */	
	private int[] computePossibleResultsRecursion(int[] array, int resting, int max, int idx, Element searchedResult, int[] result) {

		//stop condition for the recursion
		//we have reached the last column
		if (idx == array.length) {
			//if the number of votes attributed < max => not interesting for us
			if(arraySum(array)<max)return result;
			System.out.println("Possible combination "+Arrays.toString(array));
			//compare combination and result of tally
			Element tempResult = poll.getZ_q().getElement(BigInteger.ZERO);
			for(int j=0;j<array.length;j++){
				tempResult = tempResult.apply(((ProtocolOption)poll.getOptions().get(j)).getRepresentation().selfApply(poll.getZ_q().getElement(BigInteger.valueOf(array[j]))));
			}
			if(poll.getG_q().areEqual(searchedResult, poll.getGenerator().selfApply(tempResult))){
				result=array;
				System.out.println("Result is "+Arrays.toString(array));
			}
			return result;
		}
		//else put a value at the index and call recursion for the other columns
		for (int i = resting; i >= 0; i--) { 
			array[idx] = i;
			result = computePossibleResultsRecursion(array, resting-i, max, idx+1, searchedResult, result);
			//if result was already found, we don't try other combinations
			if(result!=null) return result;
		}
		return result;
	}

	/**
	 * Make the sum of each element of the array
	 * Warning: this method is sensible to integer overflow if the number a great enough
	 * @param array the array to sum up
	 * @return the sum
	 */
	private int arraySum(int[] array){
		int sum=0;
		for(int i=0;i<array.length;i++){
			sum+=array[i];
		}
		return sum;
	}


}
