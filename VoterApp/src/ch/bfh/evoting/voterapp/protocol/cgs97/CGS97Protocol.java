package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.VoteService;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialField;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialRing;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.classes.ModuloFunction;
import ch.bfh.unicrypt.math.helper.Alphabet;

public class CGS97Protocol extends ProtocolInterface {

	private Context context;
	
	private final BigInteger P = new BigInteger("71335773423288727070606722553314169530543478953625816702224545043719133047539");
	private final BigInteger Q = new BigInteger("35667886711644363535303361276657084765271739476812908351112272521859566523769");
	private final int THRESHOLD = 2;

	public CGS97Protocol(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void showReview(Poll poll) {
		//Add protocol specific stuff to the poll

		//Send poll to other participants
		VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)poll);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		//Send a broadcast to start the next activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	
	}

	@Override
	public void beginVotingPeriod(Poll poll) {
		
		
		// Distributed Key generation
		GStarModSafePrime gQ = GStarModSafePrime.getInstance(P);
		
		ZModPrime zQ = (ZModPrime) gQ.getZModOrder();
		
		PolynomialElement polynomial = PolynomialRing.getInstance(zQ).getRandomElement(THRESHOLD - 1);
		
		ZModElement[] shares = new ZModElement[poll.getParticipants().size()];
		
			
		int i = 0;
		for (Map.Entry<String, Participant> entry : poll.getParticipants().entrySet()) {    
			String key = entry.getKey();
		    Participant participant = entry.getValue();
		    
		    StringElement id = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII).getElement(participant.getUniqueId());
		    
		    
		    FiniteByteArrayElement trusteeId = id.getHashValue();
		    shares[i] = (ZModElement) polynomial.evaluate(ModuloFunction.getInstance(trusteeId.getSet(), zQ).apply(trusteeId));
		    VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_PRINT, shares[i]);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm, participant.getUniqueId());
			Log.d(this.getClass().getSimpleName(), "Share sent to " + participant.getUniqueId());
		    i++;
		}
		
//		for (ZModElement share : shares){
//			Log.d("SECRET SHARE", share.toString());
//		}
//		
		
		

		if(AndroidApplication.getInstance().isAdmin()){
			//called when admin want to begin voting period
			
			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

			//Do some protocol specific stuff

			//start service listening to incoming votes and stop voting period events
			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));

			//Send a broadcast to start the review activity
			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
			intent.putExtra("poll", (Serializable)poll);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} else {
			//called when start message received from admin
			
			//Do some protocol specific stuff
			
			//start service listening to incoming votes and stop voting period events
			context.startService(new Intent(context, VoteService.class).putExtra("poll", poll));
		}
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
	public void vote(Option selectedOption, Poll poll) {
		//do some protocol specific stuff

		//send the vote over the network
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE, selectedOption));

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * Method called when the result must be computed (all votes received or stop asked by the admin)
	 * @param poll poll object
	 */
	public void computeResult(Poll poll){

		context.stopService(new Intent(context, VoteService.class));

		//do some protocol specific stuff
		//go through compute result and set percentage result
		List<Option> options = poll.getOptions();
		int votesReceived = 0;
		for(Option option : options){
			votesReceived += option.getVotes();
		}
		for(Option option : options){
			if(votesReceived!=0){
				option.setPercentage(option.getVotes()*100/votesReceived);
			} else {
				option.setPercentage(0);
			}
		}

		poll.setTerminated(true);

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	protected void handleReceivedPoll(Poll poll, String sender) {
		//do some protocol specific stuff

		//Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable) poll);
		intent.putExtra("sender", sender);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}





}
