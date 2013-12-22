package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialRing;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.classes.ModuloFunction;
import ch.bfh.unicrypt.math.helper.Alphabet;

public class CGS97Protocol extends ProtocolInterface {

	private Context context;

	private final BigInteger P = new BigInteger(
			"71335773423288727070606722553314169530543478953625816702224545043719133047539");

	private final int THRESHOLD = 2;

	private BroadcastReceiver coefficientCommitReceiver;

	private BroadcastReceiver keyShareReceiver;
	
	private ProtocolPoll protocolPoll;
	
	private GStarModSafePrime gQ = GStarModSafePrime.getInstance(P);
	
	private GStarModElement publicKey = gQ.getIdentityElement(); 

	private ZModPrime zQ = (ZModPrime) gQ.getZModOrder();

	private int receivedShares = 0;

	private int receivedCommitments = 0;
	


	public CGS97Protocol(Context context) {
		super(context);
		this.context = context;

		// Register a BroadcastReceiver for receiving the commitments to the
		// coefficients during the key generation phase
		coefficientCommitReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				handleReceiveCommitments(
						(GStarModElement[]) intent
						.getSerializableExtra("coefficientCommitments"),
						intent.getStringExtra("sender"));
			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(
				coefficientCommitReceiver,
				new IntentFilter(BroadcastIntentTypes.coefficientCommitment));

		// Register a BroadcastReceiver for receiving the commitments to the
		// coefficients during the key generation phase
		keyShareReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				handleReceiveShare(
						(ZModElement) intent.getSerializableExtra("keyShare"),
						intent.getStringExtra("sender"));
			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(
				keyShareReceiver,
				new IntentFilter(BroadcastIntentTypes.keyShare));

	}

	@Override
	public void showReview(Poll poll) {
		// Add protocol specific stuff to the poll

		ProtocolPoll protocolPoll = new ProtocolPoll(poll);
		List<Option> options = new ArrayList<Option>();
		for(Option op : poll.getOptions()){
			ProtocolOption protocolOption = new ProtocolOption(op);
			options.add(protocolOption);
		}
		protocolPoll.setOptions(options);
		
		Map<String, Participant> participants = new TreeMap<String,Participant>();

		for(Participant p : poll.getParticipants().values()){
			ProtocolParticipant protocolParticipant = new ProtocolParticipant(p);
			participants.put(p.getUniqueId(), protocolParticipant);
		}
		protocolPoll.setParticipants(participants);

		// Send poll to other participants
		VoteMessage vm = new VoteMessage(
				VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW,
				(Serializable) protocolPoll);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		// Send a broadcast to start the next activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable) protocolPoll);
		intent.putExtra("sender", AndroidApplication.getInstance()
				.getNetworkInterface().getMyUniqueId());
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	public void beginVotingPeriod(Poll poll) {
		
		this.protocolPoll = (ProtocolPoll) poll;
		
		if (AndroidApplication.getInstance().isAdmin()) {
			// called when admin want to begin voting period

			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(
					VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface()
			.sendMessage(vm);

			// Do some protocol specific stuff

			// start service listening to incoming votes and stop voting period
			// events
			context.startService(new Intent(context, VoteService.class)
			.putExtra("poll", poll));

			// Send a broadcast to start the review activity
			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
			intent.putExtra("poll", (Serializable) poll);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} else {
			// called when start message received from admin

			// Do some protocol specific stuff

			// start service listening to incoming votes and stop voting period
			// events
			context.startService(new Intent(context, VoteService.class)
			.putExtra("poll", poll));
		}

		// Distributed Key generation
		

		PolynomialElement polynomial = PolynomialRing.getInstance(zQ)
				.getRandomElement(THRESHOLD - 1);

		// Generate an array of commitments to the coefficients and broadcast it

		GStarModElement[] coefficientCommitments = new GStarModElement[polynomial
		                                                               .getDegree() + 1];

		for (int i = 0; i <= polynomial.getDegree(); i++) {
			coefficientCommitments[i] = gQ.getDefaultGenerator().power(
					polynomial.getCoefficient(i));
		}

		Log.d(this.getClass().getSimpleName(), "I AM: "
				+ AndroidApplication.getInstance().getNetworkInterface()
				.getMyUniqueId());
		Log.d(this.getClass().getSimpleName(), "broadcasting commitments... ");
		AndroidApplication
		.getInstance()
		.getNetworkInterface()
		.sendMessage(
				new VoteMessage(
						VoteMessage.Type.VOTE_MESSAGE_COEFFICIENT_COMMITMENT,
						coefficientCommitments));

		// Create a share for each participant and distribute it using unicast

		ZModElement[] shares = new ZModElement[poll.getParticipants().size()];

		int i = 0;
		for (Map.Entry<String, Participant> entry : poll.getParticipants()
				.entrySet()) {
			Participant participant = entry.getValue();

			StringElement id = StringMonoid.getInstance(
					Alphabet.PRINTABLE_ASCII).getElement(
							participant.getUniqueId());
			FiniteByteArrayElement trusteeId = id.getHashValue();
			Log.d(this.getClass().getSimpleName(), "generate: trusteeID for " + entry.getKey() + ": " + ModuloFunction
					.getInstance(trusteeId.getSet(), zQ).apply(trusteeId));
			
			shares[i] = (ZModElement) polynomial.evaluate(ModuloFunction
					.getInstance(trusteeId.getSet(), zQ).apply(trusteeId));
			AndroidApplication
			.getInstance()
			.getNetworkInterface()
			.sendMessage(
					new VoteMessage(
							VoteMessage.Type.VOTE_MESSAGE_KEY_SHARE,
							shares[i]), participant.getUniqueId());
			i++;
		}
	}

	@Override
	public void endVotingPeriod() {
		// Send stop signal over the network
		VoteMessage vm = new VoteMessage(
				VoteMessage.Type.VOTE_MESSAGE_STOP_POLL, null);
		AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);

		// send broadcast containing the stop voting period event
		Intent i = new Intent(BroadcastIntentTypes.stopVote);
		LocalBroadcastManager.getInstance(context).sendBroadcast(i);
		// The VoteService listens to this broadcast and a calls the
		// computeResult method
	}

	@Override
	public void vote(Option selectedOption, Poll poll) {
		// do some protocol specific stuff
		
		

		// send the vote over the network
		AndroidApplication
		.getInstance()
		.getNetworkInterface()
		.sendMessage(
				new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_VOTE,
						selectedOption));

		// Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable) poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * Method called when the result must be computed (all votes received or
	 * stop asked by the admin)
	 * 
	 * @param poll
	 *            poll object
	 */
	public void computeResult(Poll poll) {

		context.stopService(new Intent(context, VoteService.class));

		// do some protocol specific stuff
		// go through compute result and set percentage result
		List<Option> options = poll.getOptions();
		int votesReceived = 0;
		for (Option option : options) {
			votesReceived += option.getVotes();
		}
		for (Option option : options) {
			if (votesReceived != 0) {
				option.setPercentage(option.getVotes() * 100 / votesReceived);
			} else {
				option.setPercentage(0);
			}
		}

		poll.setTerminated(true);

		// Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showResultActivity);
		intent.putExtra("poll", (Serializable) poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	protected void handleReceivedPoll(Poll poll, String sender) {
		// do some protocol specific stuff

		// Send a broadcast to start the review activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable) poll);
		intent.putExtra("sender", sender);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	@Override
	public void exportToXML(File file, Poll poll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelVotingPeriod() {
		// TODO Auto-generated method stub

	}

	public void handleReceiveCommitments(
			GStarModElement[] coefficientCommitments, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got commitments from " + sender);
		ProtocolParticipant senderParticipant = (ProtocolParticipant) protocolPoll.getParticipants().get(sender);
		if (senderParticipant.getCoefficientCommitments() == null){
			receivedCommitments++;
		}
		senderParticipant.setCoefficientCommitments(coefficientCommitments);
		
	}

	public void handleReceiveShare(ZModElement keyShare, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got a keyshare from " + sender);
		ProtocolParticipant senderParticipant = (ProtocolParticipant) protocolPoll.getParticipants().get(sender);
		if (senderParticipant.getCoefficientCommitments() == null){
			receivedShares++;
		}
		senderParticipant.setKeyShareFrom(keyShare);
		
		
		// Check the keyshares using the commitments sent earlier
		
		GStarModElement[] coefficientCommitments = senderParticipant.getCoefficientCommitments();
		
		StringElement id = StringMonoid.getInstance(
				Alphabet.PRINTABLE_ASCII).getElement(
						AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());

		FiniteByteArrayElement trusteeId = id.getHashValue();
		
		Log.d(this.getClass().getSimpleName(), "verify: trusteeID for " + sender + ": " + ModuloFunction
				.getInstance(trusteeId.getSet(), zQ).apply(trusteeId));
		
		
		GStarModElement product = gQ.getIdentityElement();
		
		Log.d(this.getClass().getSimpleName(), "Generator: " + gQ.getDefaultGenerator());
		
		for (int i = 0; i < coefficientCommitments.length; i++){
			Log.d(this.getClass().getSimpleName(), "Coeff: " + coefficientCommitments[i]);
			
			// coefficientCommitments[i] ^ trusteeId ^ degree
			product = product.apply(coefficientCommitments[i].power(ModuloFunction
					.getInstance(trusteeId.getSet(), zQ).apply(trusteeId).power(i)));
		}
		
		Log.d(this.getClass().getSimpleName(), "Key Share from " + sender + ": " + keyShare);
		
		if (gQ.getDefaultGenerator().power(keyShare).isEqual(product)){
			publicKey = publicKey.apply(coefficientCommitments[0]);
			protocolPoll.setPublicKey(publicKey);
			Log.d(this.getClass().getSimpleName(), "Key Share of " + sender + " ok");
		}
		else {
			Log.d(this.getClass().getSimpleName(), "Key Share of " + sender + " NOT ok");
		}
	}
}
