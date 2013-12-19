package ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine;

import java.math.BigInteger;

import com.continuent.tungsten.fsm.core.StateMachine;

import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.protocol.HKRS12ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolMessageContainer;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager.Round;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.classes.StandardNonInteractiveSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.ElGamalEncryptionValidityProofGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageEqualityProofGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageProofGenerator;
import ch.bfh.unicrypt.crypto.schemes.commitment.classes.StandardCommitmentScheme;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.N;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.general.classes.ProductSemiGroup;
import ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.general.interfaces.SemiGroup;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.function.classes.CompositeFunction;
import ch.bfh.unicrypt.math.function.classes.MultiIdentityFunction;
import ch.bfh.unicrypt.math.function.classes.PartiallyAppliedFunction;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.classes.SelfApplyFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.math.helper.Alphabet;
import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class ProcessingService extends IntentService {

	private static final String TAG = "ProcessingService";
	private StateMachine sm;

	public ProcessingService(){
		super(TAG);
		sm = ((HKRS12ProtocolInterface)AndroidApplication.getInstance().getProtocolInterface()).getStateMachineManager().getStateMachine();
	};


	@Override
	protected void onHandleIntent(Intent intent) {

		boolean exclude = false;
		Round round = (Round) intent.getSerializableExtra("round");
		String sender = intent.getStringExtra("sender");
		ProtocolMessageContainer message = (ProtocolMessageContainer) intent.getSerializableExtra("message");

		AbstractAction action = ((AbstractAction)sm.getState().getEntryAction());
		if(action==null || action instanceof ExitAction) return; //state machine was already terminated

		//Get the poll for read only actions
		ProtocolPoll poll = action.getPoll();

		ProtocolParticipant senderParticipant = (ProtocolParticipant)poll.getParticipants().get(sender);


		switch(round){
		case setup:

			//Verify proof of knowledge of xi

			StandardCommitmentScheme<GStarMod, Element> csSetup = StandardCommitmentScheme.getInstance(poll.getGenerator());

			//Generator and index of the participant has also to be hashed in the proof
			Tuple otherInput = Tuple.getInstance(senderParticipant.getDataToHash(), poll.getDataToHash());
			
			SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator.getInstance(csSetup.getCommitmentFunction(), otherInput);

			PreimageProofGenerator spg = PreimageProofGenerator.getInstance(scg, csSetup.getCommitmentFunction());

			//if proof is false, exclude participant
			if(!spg.verify(message.getProof(), message.getValue()).getBoolean()){
				exclude = true;
				Log.w(TAG, "Proof of knowledge for xi was false for participant "+senderParticipant.getIdentification()+" ("+sender+")");
			}

			break;
		case commit:
			//Nothing specific to do, only save values
			break;
		case voting:
			//processing of voting messages directly depend on values received in commitment round, so we have to wait until commitment round is finished
			while(!(action instanceof VotingRoundAction)){
				SystemClock.sleep(100);
			}

			//Verify validity proof

			Element[] possibleVotes = new Element[poll.getOptions().size()];
			int i=0;
			for(Option op:poll.getOptions()){
				possibleVotes[i] = poll.getGenerator().selfApply(((ProtocolOption)op).getRepresentation());
				i++;
			}

			ElGamalEncryptionScheme<GStarMod, Element> ees = ElGamalEncryptionScheme.getInstance(poll.getGenerator());

			Tuple otherInput2 = Tuple.getInstance(senderParticipant.getDataToHash(), poll.getDataToHash());

			SigmaChallengeGenerator scg2 = ElGamalEncryptionValidityProofGenerator.createNonInteractiveChallengeGenerator(ees, possibleVotes.length, otherInput2);
			Subset possibleVotesSet = Subset.getInstance(poll.getG_q(), possibleVotes);
			ElGamalEncryptionValidityProofGenerator vpg = ElGamalEncryptionValidityProofGenerator.getInstance(
					scg2, ees, message.getComplementaryValue(), possibleVotesSet);

			//simulate the ElGamal cipher text (a,b) = (ai,bi);
			Tuple publicInput = Tuple.getInstance(senderParticipant.getAi(), message.getValue());

			if(!vpg.verify(senderParticipant.getProofValidVote(), publicInput).getBoolean()){
				exclude = true;
				Log.w(TAG, "Proof of validity was false for participant "+senderParticipant.getIdentification()+" ("+sender+")");
			}


			break;
		case recovery:

			//verify proof

			//Function g^r
			StandardCommitmentScheme<GStarMod, Element> cs3 = StandardCommitmentScheme.getInstance(poll.getGenerator());
			Function f1 = cs3.getCommitmentFunction();

			//Function h_hat^r
			StandardCommitmentScheme<GStarMod, Element> cs4 = StandardCommitmentScheme.getInstance(message.getComplementaryValue());
			Function f2 = cs4.getCommitmentFunction();
			
			ProductFunction f3 = ProductFunction.getInstance(f1, f2);

			Tuple otherInput3 = Tuple.getInstance(senderParticipant.getDataToHash(), poll.getDataToHash());

			SigmaChallengeGenerator scg3 = StandardNonInteractiveSigmaChallengeGenerator.getInstance(f3, otherInput3);

			PreimageEqualityProofGenerator piepg = PreimageEqualityProofGenerator.getInstance(scg3, f1,f2);

			Tuple publicInput3 = Tuple.getInstance(senderParticipant.getAi(), message.getValue());

			if(!piepg.verify(message.getProof(), publicInput3).getBoolean()){
				Log.w(TAG, "Proof of equality between discrete logs was false for participant "+senderParticipant.getIdentification()+" ("+sender+")");
				exclude = true;
			}
			break;
		default:
			break;
		}


		action.savedProcessedMessage(round, sender, message, exclude);

		if(action.readyToGoToNextState()){
			action.goToNextState();
		}


	}

}
