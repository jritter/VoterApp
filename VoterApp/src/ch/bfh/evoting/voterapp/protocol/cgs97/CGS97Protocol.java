package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.MainActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.Utility;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.classes.StandardNonInteractiveSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.ElGamalEncryptionValidityProofGenerator;
import ch.bfh.unicrypt.crypto.proofgenerator.classes.PreimageEqualityProofGenerator;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.PolynomialRing;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZMod;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModElement;
import ch.bfh.unicrypt.math.algebra.dualistic.classes.ZModPrime;
import ch.bfh.unicrypt.math.algebra.dualistic.interfaces.DualisticElement;
import ch.bfh.unicrypt.math.algebra.general.classes.BooleanElement;
import ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.ProductSemiGroup;
import ch.bfh.unicrypt.math.algebra.general.classes.Subset;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarMod;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import ch.bfh.unicrypt.math.function.classes.ModuloFunction;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.math.helper.Alphabet;

public class CGS97Protocol extends ProtocolInterface {

	private Context context;

	private final BigInteger P = new BigInteger(
			"24421817481307177449647246484681681783337829412862177682538435312071281569646025606745584903210775224457523457768723824442724616998787110952108654428565400454402598245210227144929556256697593550903247924055714937916514526166092438066936693296218391429342957961400667273342778842895486447440287639065428393782477303395870298962805975752198304889507138990179204870133839847367098792875574662446712567387387134946911523722735147628746206081844500879809860996360597720571611720620174658556850893276934140542331691801045622505813030592119908356317756153773900818965668280464355085745552657819811997912683349698802670648319");

	

	private ProtocolPoll protocolPoll;

	private GStarModSafePrime gQ = GStarModSafePrime.getInstance(P);

	private GStarModElement publicKey = gQ.getIdentityElement();

	private ZModPrime zQ = (ZModPrime) gQ.getZModOrder();
	
	private GStarModElement productLeft = gQ.getIdentityElement();
	private GStarModElement productRight = gQ.getIdentityElement();


	private ConcurrentHashMap<String, ZModElement> receivedShares = new ConcurrentHashMap<String, ZModElement>();
	
	private ConcurrentHashMap<String, GStarModElement> receivedShareCommitments = new ConcurrentHashMap<String, GStarModElement>();

	private ConcurrentHashMap<String, ProtocolBallot> ballots = new ConcurrentHashMap<String, ProtocolBallot>();

	private ConcurrentHashMap<String, GStarModElement> partDecryptions = new ConcurrentHashMap<String, GStarModElement>();
	
	private ConcurrentHashMap<GStarModElement, ZModElement> combinationsMap = new ConcurrentHashMap<GStarModElement, ZModElement>();

	private BroadcastReceiver voteReceiver;

	private BroadcastReceiver partDecryptionReceiver;
	
	private BroadcastReceiver stopReceiver;
	
	private BroadcastReceiver coefficientCommitReceiver;

	private BroadcastReceiver keyShareReceiver;
	
	private BroadcastReceiver keyShareCommitmentReceiver;
	
	private int numberOfBitsPerOption;

	private ElGamalEncryptionScheme<GStarMod, GStarModElement> elGamal;

	private SigmaChallengeGenerator scg;

	private ElGamalEncryptionValidityProofGenerator pg;

	private GStarModElement[] possibleMessages;

	private ProgressDialog progressDialog;

	private VoteUpdaterThread voteUpdaterThread;

	protected boolean resultFound;

	

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

		keyShareCommitmentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				handleReceiveShareCommitment(
						(GStarModElement) intent
								.getSerializableExtra("keyShareCommitment"),
						intent.getStringExtra("sender"));
			}
		};

		voteReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
				ProtocolBallot ballot = (ProtocolBallot) intent
						.getSerializableExtra("vote");

				handleReceiveVote(ballot, intent.getStringExtra("voter"));
			}
		};
		

		partDecryptionReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {

				GStarModElement partDecryption = (GStarModElement) intent
						.getSerializableExtra("partDecryption");
				handlePartDecryption(partDecryption,
						intent.getStringExtra("sender"));
			}
		};
		

		// Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				partDecrypt(protocolPoll);
			}
		};
		

	}



	@Override
	public void showReview(Poll poll) {
		// Add protocol specific stuff to the poll

		ProtocolPoll protocolPoll;
		if (poll instanceof ProtocolPoll) {
			protocolPoll = (ProtocolPoll) poll;
		} else {
			protocolPoll = new ProtocolPoll(poll);
		}

		List<Option> options = new ArrayList<Option>();
		for (Option op : poll.getOptions()) {
			ProtocolOption protocolOption = new ProtocolOption(op);
			options.add(protocolOption);
		}
		protocolPoll.setOptions(options);

		Map<String, Participant> participants = new TreeMap<String, Participant>();

		for (Participant p : poll.getParticipants().values()) {
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
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				coefficientCommitReceiver,
				new IntentFilter(BroadcastIntentTypes.coefficientCommitment));
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				keyShareReceiver,
				new IntentFilter(BroadcastIntentTypes.keyShare));
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				keyShareCommitmentReceiver, new IntentFilter(BroadcastIntentTypes.keyShareCommitment));
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				voteReceiver, new IntentFilter(BroadcastIntentTypes.newVote));
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				partDecryptionReceiver,
				new IntentFilter(BroadcastIntentTypes.partDecryption));
		
		LocalBroadcastManager.getInstance(context).registerReceiver(
				stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));
		
		
		
		this.protocolPoll = (ProtocolPoll) poll;

		receivedShares.clear();
		receivedShareCommitments.clear();
		ballots.clear();
		partDecryptions.clear();
		combinationsMap.clear();
		voteUpdaterThread = null;
		
		publicKey = gQ.getIdentityElement();
		productLeft = gQ.getIdentityElement();
		productRight = gQ.getIdentityElement();
		

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
			// context.startService(new Intent(context, VoteService.class)
			// .putExtra("poll", poll));

			// Send a broadcast to start the review activity
			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
			intent.putExtra("poll", (Serializable) poll);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} else {
			// called when start message received from admin

			// Do some protocol specific stuff

			// start service listening to incoming votes and stop voting period
			// events
			// context.startService(new Intent(context, VoteService.class)
			// .putExtra("poll", poll));
		}

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... arg0) {

				// Distributed Key generation

				PolynomialElement polynomial = PolynomialRing.getInstance(zQ)
						.getRandomElement(protocolPoll.getThreshold() - 1);

				// Generate an array of commitments to the coefficients and
				// broadcast it

				GStarModElement[] coefficientCommitments = new GStarModElement[polynomial
						.getDegree() + 1];

				for (int i = 0; i <= polynomial.getDegree(); i++) {
					coefficientCommitments[i] = gQ.getDefaultGenerator().power(
							polynomial.getCoefficient(i));
				}

				Log.d(this.getClass().getSimpleName(), "I AM: "
						+ AndroidApplication.getInstance()
								.getNetworkInterface().getMyUniqueId());
				Log.d(this.getClass().getSimpleName(),
						"broadcasting commitments... ");
				AndroidApplication
						.getInstance()
						.getNetworkInterface()
						.sendMessage(
								new VoteMessage(
										VoteMessage.Type.VOTE_MESSAGE_COEFFICIENT_COMMITMENT,
										coefficientCommitments));

				// Create a share for each participant and distribute it using
				// unicast

				ZModElement[] shares = new ZModElement[protocolPoll
						.getParticipants().size()];

				int k = 0;
				for (Map.Entry<String, Participant> entry : protocolPoll
						.getParticipants().entrySet()) {
					Participant participant = entry.getValue();

					StringElement id = StringMonoid.getInstance(
							Alphabet.PRINTABLE_ASCII).getElement(
							participant.getUniqueId());
					FiniteByteArrayElement trusteeId = id.getHashValue();
					Log.d(this.getClass().getSimpleName(),
							"generate: trusteeID for "
									+ entry.getKey()
									+ ": "
									+ ModuloFunction.getInstance(
											trusteeId.getSet(), zQ).apply(
											trusteeId));

					shares[k] = (ZModElement) polynomial
							.evaluate(ModuloFunction.getInstance(
									trusteeId.getSet(), zQ).apply(trusteeId));
					AndroidApplication
							.getInstance()
							.getNetworkInterface()
							.sendMessage(
									new VoteMessage(
											VoteMessage.Type.VOTE_MESSAGE_KEY_SHARE,
											shares[k]),
									participant.getUniqueId());
					
					k++;
				}

				final int NUMBER_OF_OPTIONS = protocolPoll.getOptions().size();

				numberOfBitsPerOption = (int) Math.ceil(Math.log(protocolPoll
						.getNumberOfParticipants()) / Math.log(2));

				possibleMessages = new GStarModElement[NUMBER_OF_OPTIONS];
				BigInteger shiftedBigInteger;
				for (int i = 0; i < NUMBER_OF_OPTIONS; i++) {
					shiftedBigInteger = BigInteger.valueOf(1).shiftLeft(
							i * numberOfBitsPerOption);
					possibleMessages[i] = gQ.getDefaultGenerator().power(
							shiftedBigInteger);
				}

				elGamal = ElGamalEncryptionScheme.getInstance(gQ);

				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
	public void vote(final Option selectedOption, final Poll poll) {
		// do some protocol specific stuff
		Log.d(this.getClass().getSimpleName(), "vote method started");

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(AndroidApplication
						.getInstance().getCurrentActivity());
				progressDialog.setMessage("Casting vote, please wait...");
				progressDialog.show();
				super.onPreExecute();
			}

			@Override
			protected Object doInBackground(Object... arg0) {

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"encrypt ballot using the following public key:");
				Log.d(CGS97Protocol.this.getClass().getSimpleName(), publicKey.toString());

				int index = poll.getOptions().indexOf(selectedOption);

				ZModElement randomization = zQ.getRandomElement();

				Tuple ballotEncryption = elGamal.encrypt(publicKey,
						possibleMessages[index], randomization);
				ProtocolParticipant voter = (ProtocolParticipant) protocolPoll
						.getParticipants().get(
								AndroidApplication.getInstance()
										.getNetworkInterface().getMyUniqueId());

				scg = ElGamalEncryptionValidityProofGenerator
						.createNonInteractiveChallengeGenerator(elGamal,
								possibleMessages.length);

				Subset plaintexts = Subset.getInstance(gQ, possibleMessages);

				pg = ElGamalEncryptionValidityProofGenerator.getInstance(scg,
						elGamal, publicKey, plaintexts);

				Tuple privateInput = pg
						.createPrivateInput(randomization, index);
				Tuple proof = pg.generate(privateInput, ballotEncryption);

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"creating protocol ballot...");

				ProtocolBallot ballot = new ProtocolBallot(voter,
						ballotEncryption, proof);

				// send the vote over the network
				Log.d(CGS97Protocol.this.getClass().getSimpleName(), "send ballot...");
				AndroidApplication
						.getInstance()
						.getNetworkInterface()
						.sendMessage(
								new VoteMessage(
										VoteMessage.Type.VOTE_MESSAGE_VOTE,
										ballot));

				// Send a broadcast to start the review activity
				Intent intent = new Intent(
						BroadcastIntentTypes.showNextActivity);
				intent.putExtra("poll", (Serializable) poll);
				return LocalBroadcastManager.getInstance(context)
						.sendBroadcast(intent);

			}

			@Override
			protected void onPostExecute(Object result) {
				progressDialog.dismiss();
				super.onPostExecute(result);
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * Method called when the result must be computed (all votes received or
	 * stop asked by the admin)
	 * 
	 * @param poll
	 *            poll object
	 */
	public void computeResult(Poll poll) {

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

		progressDialog.dismiss();

		// Send a broadcast to start the result activity
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
		ProtocolParticipant senderParticipant = (ProtocolParticipant) protocolPoll
				.getParticipants().get(sender);

		senderParticipant.setCoefficientCommitments(coefficientCommitments);

	}

	public void handleReceiveShare(ZModElement keyShare, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got a keyshare from " + sender);
		ProtocolParticipant senderParticipant = (ProtocolParticipant) protocolPoll
				.getParticipants().get(sender);
		if (senderParticipant.getCoefficientCommitments() == null) {
			// receivedShares++;
		}
		senderParticipant.setKeyShareFrom(keyShare);

		// Check the keyshares using the commitments sent earlier

		GStarModElement[] coefficientCommitments = senderParticipant
				.getCoefficientCommitments();

		StringElement id = StringMonoid.getInstance(Alphabet.PRINTABLE_ASCII)
				.getElement(
						AndroidApplication.getInstance().getNetworkInterface()
								.getMyUniqueId());

		FiniteByteArrayElement trusteeId = id.getHashValue();

		GStarModElement product = gQ.getIdentityElement();

		for (int i = 0; i < coefficientCommitments.length; i++) {
			Log.d(this.getClass().getSimpleName(), "Coeff: "
					+ coefficientCommitments[i]);

			// coefficientCommitments[i] ^ trusteeId ^ degree
			product = product.apply(coefficientCommitments[i]
					.power(ModuloFunction.getInstance(trusteeId.getSet(), zQ)
							.apply(trusteeId).power(i)));
		}

		Log.d(this.getClass().getSimpleName(), "Key Share from " + sender
				+ ": " + keyShare);

		if (gQ.getDefaultGenerator().power(keyShare).isEquivalent(product)) {
			publicKey = publicKey.apply(coefficientCommitments[0]);
			protocolPoll.setPublicKey(publicKey);
			receivedShares.put(sender, keyShare);
			Log.d(this.getClass().getSimpleName(), "Key Share of " + sender
					+ " ok");
		} else {
			Log.d(this.getClass().getSimpleName(), "Key Share of " + sender
					+ " NOT ok");
		}
	}
	
	protected void handleReceiveShareCommitment(
			GStarModElement keyShareCommitment, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got keyshare commitment from " + sender);
		receivedShareCommitments.put(sender, keyShareCommitment);
	}

	protected void handleReceiveVote(ProtocolBallot ballot, String sender) {
		Log.d(this.getClass().getSimpleName(), "handleReceiveVote called...");
		ballots.put(sender, ballot);
		protocolPoll.getParticipants().get(sender).setHasVoted(true);

		Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
		i.putExtra("votes", ballots.size());
		i.putExtra("options", (Serializable) protocolPoll.getOptions());
		i.putExtra("participants",
				(Serializable) protocolPoll.getParticipants());
		LocalBroadcastManager.getInstance(context).sendBroadcast(i);

		Log.d(this.getClass().getSimpleName(), "Got ballot from " + sender);

		if (ballots.size() >= protocolPoll.getNumberOfParticipants()) {
			Log.d(this.getClass().getSimpleName(), "calling partdecrypt...");
			LocalBroadcastManager.getInstance(context).unregisterReceiver(
					voteReceiver);
			voteUpdaterThread.interrupt();
			partDecrypt(protocolPoll);
		} else {
			if (voteUpdaterThread == null) {
				voteUpdaterThread = new VoteUpdaterThread();
				voteUpdaterThread.start();
			}
		}
	}

	private void partDecrypt(ProtocolPoll poll) {

		Log.d(this.getClass().getSimpleName(), "Running part decryption...");

		progressDialog = new ProgressDialog(AndroidApplication.getInstance()
				.getCurrentActivity());
		progressDialog.setMessage("Starting tallying process...");

		// context.stopService(new Intent(context, VoteService.class));

		// do some protocol specific stuff

		// compute the product of all ballot ciphertexts

		scg = ElGamalEncryptionValidityProofGenerator
				.createNonInteractiveChallengeGenerator(elGamal,
						possibleMessages.length);

		Subset plaintexts = Subset.getInstance(gQ, possibleMessages);

		pg = ElGamalEncryptionValidityProofGenerator.getInstance(scg, elGamal,
				publicKey, plaintexts);
		
		productLeft = gQ.getIdentityElement();
		productRight = gQ.getIdentityElement();

		for (ProtocolBallot ballot : ballots.values()) {
			if (pg.verify(ballot.getValidityProof(), ballot.getBallot())
					.getBoolean()) {
				Log.d(this.getClass().getSimpleName(), "Ballot ok, counting");
				productLeft = productLeft.multiply((GStarModElement) ballot
						.getBallot().getAt(0));
				productRight = productRight.multiply((GStarModElement) ballot
						.getBallot().getAt(1));
			} else {
				Log.d(this.getClass().getSimpleName(),
						"Ballot NOT ok, NOT counting");
			}
		}

		// Do the part decryption
		ZModElement keyShare = zQ.getIdentityElement();
		
		AndroidApplication
		.getInstance()
		.getNetworkInterface()
		.sendMessage(
				new VoteMessage(
						VoteMessage.Type.VOTE_MESSAGE_KEY_SHARE_COMMITMENT,
						elGamal.getGenerator().power(keyShare)));
		

		Log.d(this.getClass().getSimpleName(), "Number of shares combined: " + receivedShares.size());
		for (ZModElement share : receivedShares.values()) {
			keyShare = keyShare.add(share);
		}

		Log.d(this.getClass().getSimpleName(), "My Keyshare: " + keyShare);

		GStarModElement partDecryption = productLeft.power(keyShare);

		Function f1 = GeneratorFunction.getInstance(elGamal.getGenerator());
		Function f2 = GeneratorFunction.getInstance(productLeft);

		ProductFunction f = ProductFunction.getInstance(f1, f2);

		SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator
				.getInstance(f.getCoDomain(),
						(ProductSemiGroup) f.getCoDomain(),
						ZMod.getInstance(f.getDomain().getMinimalOrder()));

		PreimageEqualityProofGenerator pg = PreimageEqualityProofGenerator
				.getInstance(scg, f1, f2);
	
		Element privateInput = keyShare;
		Element publicInput = Tuple
				.getInstance(elGamal.getGenerator().power(keyShare), partDecryption);

		Triple proof = pg.generate(privateInput, publicInput);

		Log.d(this.getClass().getSimpleName(), "Part decryption proof valid: " + pg.verify(proof, publicInput));
		

		AndroidApplication
				.getInstance()
				.getNetworkInterface()
				.sendMessage(
						new VoteMessage(
								VoteMessage.Type.VOTE_MESSAGE_PART_DECRYPTION,
								partDecryption));

	}

	protected void handlePartDecryption(GStarModElement partDecryption,
			String sender) {
		Log.d(this.getClass().getSimpleName(),
				"handlePartDecryption started...");

		Log.d(this.getClass().getSimpleName(), "Got part decryption from "
				+ sender);
		ProtocolParticipant senderParticipant = (ProtocolParticipant) protocolPoll
				.getParticipants().get(sender);

		senderParticipant.setPartDecryption(partDecryption);
		partDecryptions.put(sender, partDecryption);

		if (partDecryptions.size() == protocolPoll.getThreshold()) {
			Log.d(this.getClass().getSimpleName(),
					"calling interpolateResult...");
			LocalBroadcastManager.getInstance(context).unregisterReceiver(
					partDecryptionReceiver);
			interpolateResult();
		}

		Log.d(this.getClass().getSimpleName(),
				"handlePartDecryption finished...");
	}

	private void interpolateResult() {

		Log.d(this.getClass().getSimpleName(), "interpolateResult started...");
		Log.d(this.getClass().getSimpleName(), "partdecryptions available: " + partDecryptions.size());

		// converting the unique trustee id and the part decryption to a point
		Pair[] pairs = new Pair[partDecryptions.size()];
		int length = pairs.length;

		Iterator<Entry<String, GStarModElement>> it = partDecryptions
				.entrySet().iterator();
		int k = 0;
		while (it.hasNext()) {
			Entry<String, GStarModElement> entry = it.next();

			StringElement id = StringMonoid.getInstance(
					Alphabet.PRINTABLE_ASCII).getElement(entry.getKey());
			FiniteByteArrayElement trusteeId = id.getHashValue();

			pairs[k] = Pair.getInstance(
					ModuloFunction.getInstance(trusteeId.getSet(), zQ).apply(
							trusteeId), entry.getValue());
			k++;
		}

		// Calculating the lagrange coefficients for each point we got
		DualisticElement lagrangeProduct = null;
		DualisticElement[] lagrangeCoefficients = new DualisticElement[length];
		for (int j = 0; j < length; j++) {
			lagrangeProduct = null;
			DualisticElement elementJ = (DualisticElement) pairs[j].getFirst();
			for (int l = 0; l < length; l++) {
				DualisticElement elementL = (DualisticElement) pairs[l]
						.getFirst();
				if (!elementJ.equals(elementL)) {
					if (lagrangeProduct == null) {
						lagrangeProduct = elementL.divide(elementL
								.subtract(elementJ));
					} else {
						lagrangeProduct = lagrangeProduct.multiply(elementL
								.divide(elementL.subtract(elementJ)));
					}
				}
			}
			lagrangeCoefficients[j] = lagrangeProduct;
		}

		GStarModElement product = null;

		for (int i = 0; i < pairs.length; i++) {
			if (product == null) {
				product = ((GStarModElement) pairs[i].getSecond())
						.power(lagrangeCoefficients[i]);
			} else {
				product = product.multiply(((GStarModElement) pairs[i]
						.getSecond()).power(lagrangeCoefficients[i]));
			}
		}
		
		

		GStarModElement result = productRight.divide(product);

		numberOfBitsPerOption = (int) Math.ceil(Math.log(protocolPoll
				.getNumberOfParticipants()) / Math.log(2));

		Log.d(this.getClass().getSimpleName(),
				"Calling getCombinations with "
						+ protocolPoll.getOptions().size() + " options and "
						+ ballots.size() + " ballots and " + numberOfBitsPerOption + " Bits per option.");
		getCombinations(protocolPoll.getOptions().size(), new Stack<Integer>(),
				ballots.size());
		
		Log.d(this.getClass().getSimpleName(), "Hashcode of combinationsMap: " + combinationsMap.hashCode());

		Log.d(this.getClass().getSimpleName(), "Result: " + result);
		Log.d(this.getClass().getSimpleName(),
				"Map size: " + combinationsMap.size());

		if (combinationsMap.containsKey(result)) {
			resultFound = true;
			BigInteger decodedResult = combinationsMap.get(result).getValue();
			Log.d(this.getClass().getSimpleName(), "DecodedResult: "
					+ decodedResult);

			BigInteger mask = BigInteger.valueOf((long) (Math.pow(2,
					numberOfBitsPerOption) - 1));

			for (int i = 0; i < protocolPoll.getOptions().size(); i++) {
				protocolPoll
						.getOptions()
						.get(i)
						.setVotes(
								decodedResult
										.shiftRight(i * numberOfBitsPerOption)
										.and(mask).intValue());
				Log.d(this.getClass().getSimpleName(),
						"Found Result for Option "
								+ protocolPoll.getOptions().get(i).getText()
								+ ": "
								+ protocolPoll.getOptions().get(i).getVotes());
			}
			Log.d(this.getClass().getSimpleName(),
					"interpolateResult finished...");

			computeResult(protocolPoll);
		} else {
			resultFound = false;
		}

		if (resultFound == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					AndroidApplication.getInstance().getCurrentActivity());
			// Add the buttons
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
							context.startActivity(new Intent(AndroidApplication
									.getInstance().getCurrentActivity(),
									MainActivity.class).addFlags(
									Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(
									Intent.FLAG_ACTIVITY_CLEAR_TASK));
						}
					});
			builder.setMessage(R.string.dialog_decryption_error);
			AlertDialog dialog = builder.create();

			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					Utility.setTextColor(dialog, context.getResources()
							.getColor(R.color.theme_color));
					((AlertDialog) dialog)
							.getButton(AlertDialog.BUTTON_NEUTRAL)
							.setBackgroundResource(
									R.drawable.selectable_background_votebartheme);

				}
			});

			dialog.show();
		}

	}

	private void getCombinations(int length, Stack<Integer> used, int val) {
		Log.d(this.getClass().getSimpleName(), "getCombinations started...");
		if (val == 0) {
			Integer[] combination = new Integer[length];
			combination = used.toArray(combination);
			BigInteger candidate = BigInteger.ZERO;
			for (int i = 0; i < combination.length; i++) {
				if (combination[i] == null) {
					combination[i] = 0;
				}

				candidate = candidate.or(BigInteger.valueOf(combination[i])
						.shiftLeft(i * numberOfBitsPerOption));
			}
			ZModElement candidateElement = zQ.getElement(candidate);
			combinationsMap.put(gQ.getDefaultGenerator().power(candidate),
					candidateElement);
			return;
		}

		if (val < 0) {
			return;
		}

		for (int i = val; i >= 0; i--) {
			if (used.size() < length) {
				used.push(i);
				getCombinations(length, used, val - i);
				used.pop();
			}
		}
		Log.d(this.getClass().getSimpleName(), "getCombinations finished");
	}

	class VoteUpdaterThread extends Thread {

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
				i.putExtra("votes", ballots.size());
				i.putExtra("options", (Serializable) protocolPoll.getOptions());
				i.putExtra("participants",
						(Serializable) protocolPoll.getParticipants());
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
				SystemClock.sleep(1000);
			}
		}
	};

}
