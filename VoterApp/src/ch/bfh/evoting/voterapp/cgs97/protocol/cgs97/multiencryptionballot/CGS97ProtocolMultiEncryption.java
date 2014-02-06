package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

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
import ch.bfh.evoting.voterapp.cgs97.AndroidApplication;
import ch.bfh.evoting.voterapp.cgs97.MainActivity;
import ch.bfh.evoting.voterapp.cgs97.R;
import ch.bfh.evoting.voterapp.cgs97.entities.Option;
import ch.bfh.evoting.voterapp.cgs97.entities.Participant;
import ch.bfh.evoting.voterapp.cgs97.entities.Poll;
import ch.bfh.evoting.voterapp.cgs97.entities.VoteMessage;
import ch.bfh.evoting.voterapp.cgs97.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.ProtocolPoll;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLBallot;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLPartDecryption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLParticipant;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLMultiEncryptionPoll;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml.XMLOption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;
import ch.bfh.evoting.voterapp.cgs97.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.cgs97.util.Utility;
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
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import ch.bfh.unicrypt.math.function.classes.ModuloFunction;
import ch.bfh.unicrypt.math.function.classes.ProductFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.unicrypt.math.helper.Alphabet;

public class CGS97ProtocolMultiEncryption extends ProtocolInterface {

	private Context context;

	private final BigInteger P = new BigInteger(
			"24421817481307177449647246484681681783337829412862177682538435312071281569646025606745584903210775224457523457768723824442724616998787110952108654428565400454402598245210227144929556256697593550903247924055714937916514526166092438066936693296218391429342957961400667273342778842895486447440287639065428393782477303395870298962805975752198304889507138990179204870133839847367098792875574662446712567387387134946911523722735147628746206081844500879809860996360597720571611720620174658556850893276934140542331691801045622505813030592119908356317756153773900818965668280464355085745552657819811997912683349698802670648319");

	private ProtocolPoll protocolPoll;

	private GStarModSafePrime gQ = GStarModSafePrime.getInstance(P);

	private GStarModElement publicKey = gQ.getIdentityElement();

	private ZModPrime zQ = (ZModPrime) gQ.getZModOrder();

	private List<Pair> ballotProducts = new ArrayList<Pair>();

	private ConcurrentHashMap<String, ZModElement> receivedShares = new ConcurrentHashMap<String, ZModElement>();

	private ConcurrentHashMap<String, GStarModElement> receivedShareCommitments = new ConcurrentHashMap<String, GStarModElement>();

	private ConcurrentHashMap<String, ProtocolBallot> ballots = new ConcurrentHashMap<String, ProtocolBallot>();

	private ConcurrentHashMap<String, GStarModElement[]> participantCoefficientCommitments = new ConcurrentHashMap<String, GStarModElement[]>();

	private ConcurrentHashMap<String, ProtocolPartDecryption> partDecryptions = new ConcurrentHashMap<String, ProtocolPartDecryption>();

	private ConcurrentHashMap<GStarModElement, ZModElement> combinationsMap = new ConcurrentHashMap<GStarModElement, ZModElement>();

	private BroadcastReceiver voteReceiver;

	private BroadcastReceiver partDecryptionReceiver;

	private BroadcastReceiver stopReceiver;

	private BroadcastReceiver coefficientCommitReceiver;

	private BroadcastReceiver keyShareReceiver;

	private BroadcastReceiver keyShareCommitmentReceiver;

	private ElGamalEncryptionScheme elGamal;

	private SigmaChallengeGenerator scg;

	private ElGamalEncryptionValidityProofGenerator pg;

	private GStarModElement[] possibleMessages;

	private ProgressDialog progressDialog;

	private VoteUpdaterThread voteUpdaterThread;

	protected boolean resultFound;

	private boolean tallyDone = false;
	private boolean publicKeyComplete = false;

	private int partDecryptionRejections = 0;

	private ZModElement keyShare = zQ.getIdentityElement();

	private boolean isInElectorate;

	public CGS97ProtocolMultiEncryption(Context context) {
		super(context);
		this.context = context;

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

		receivedShares.clear();
		receivedShareCommitments.clear();
		participantCoefficientCommitments.clear();
		ballots.clear();
		partDecryptions.clear();
		combinationsMap.clear();
		voteUpdaterThread = null;
		scg = null;
		pg = null;
		partDecryptionRejections = 0;
		tallyDone = false;
		publicKeyComplete = false;

		ballotProducts.clear();

		publicKey = gQ.getIdentityElement();

		// initialize the product
		for (int i = 0; i < poll.getOptions().size(); i++) {
			ballotProducts.add(Pair.getInstance(gQ.getIdentityElement(),
					gQ.getIdentityElement()));
		}

		keyShare = zQ.getIdentityElement();

		elGamal = ElGamalEncryptionScheme.getInstance(gQ);

		isInElectorate = isContainedInParticipants(AndroidApplication
				.getInstance().getNetworkInterface().getMyUniqueId(), poll
				.getParticipants().values());

		if (AndroidApplication.getInstance().isAdmin()) {
			// called when admin want to begin voting period

			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(
					VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface()
					.sendMessage(vm);


			// Send a broadcast to start the review activity
			Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
			intent.putExtra("poll", (Serializable) poll);
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		} 

		if (isInElectorate) {
			new AsyncTask<Object, Object, Object>() {

				@Override
				protected Object doInBackground(Object... arg0) {

					// Distributed Key generation

					PolynomialElement polynomial = PolynomialRing.getInstance(
							zQ).getRandomElement(
							protocolPoll.getThreshold() - 1);

					// Generate an array of commitments to the coefficients and
					// broadcast it

					GStarModElement[] coefficientCommitments = new GStarModElement[polynomial
							.getValue().getDegree() + 1];

					for (int i = 0; i <= polynomial.getValue().getDegree(); i++) {
						coefficientCommitments[i] = gQ.getDefaultGenerator()
								.power(polynomial.getValue().getCoefficient(i));
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

					// Create a share for each participant and distribute it
					// using unicast

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
						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(),
								"generate: trusteeID for "
										+ entry.getKey()
										+ ": "
										+ ModuloFunction.getInstance(
												trusteeId.getSet(), zQ).apply(
												trusteeId));

						shares[k] = (ZModElement) polynomial
								.evaluate(ModuloFunction.getInstance(
										trusteeId.getSet(), zQ)
										.apply(trusteeId));

						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(), "Sending key share to "
								+ participant.getUniqueId());

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

					possibleMessages = new GStarModElement[2];
					possibleMessages[0] = ((GStarModElement) elGamal
							.getGenerator()).power(0);
					possibleMessages[1] = ((GStarModElement) elGamal
							.getGenerator()).power(1);

					return null;
				}
			}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

				while (publicKeyComplete == false) {
					try {
						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(),
								"Waiting for public key to be completely available...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(),
						"encrypt ballot using the following public key:");
				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), publicKey.toString());

				// int index = poll.getOptions().indexOf(selectedOption);

				ZModElement randomizationSum = zQ.getIdentityElement();
				GStarModElement ballotProductLeft = gQ.getIdentityElement();
				GStarModElement ballotProductRight = gQ.getIdentityElement();
				List<ProtocolBallotOption> options = new ArrayList<ProtocolBallotOption>();
				int index = 0;

				Subset plaintexts = Subset.getInstance(gQ, possibleMessages);
				Element proofElement = StringMonoid.getInstance(
						Alphabet.PRINTABLE_ASCII).getElement(
						protocolPoll.toString());

				scg = ElGamalEncryptionValidityProofGenerator
						.createNonInteractiveChallengeGenerator(elGamal,
								possibleMessages.length, proofElement);

				pg = ElGamalEncryptionValidityProofGenerator.getInstance(scg,
						elGamal, publicKey, plaintexts);

				for (Option option : poll.getOptions()) {
					ZModElement randomization = zQ.getRandomElement();
					randomizationSum = randomizationSum.add(randomization);
					if (option == selectedOption) {
						index = 1;

					} else {
						index = 0;
					}

					Pair optionEncryption = elGamal.encrypt(publicKey,
							possibleMessages[index], randomization);
					ballotProductLeft = ballotProductLeft
							.multiply(optionEncryption.getFirst());
					ballotProductRight = ballotProductRight
							.multiply(optionEncryption.getSecond());

					Tuple privateInput = pg.createPrivateInput(randomization,
							index);
					Tuple proof = pg.generate(privateInput, optionEncryption);

					options.add(new ProtocolBallotOption(optionEncryption,
							proof));
				}

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "Randomization sum : "
						+ randomizationSum);
				Tuple privateInput = pg.createPrivateInput(randomizationSum, 1);
				Tuple proof = pg
						.generate(privateInput, Pair.getInstance(
								ballotProductLeft, ballotProductRight));

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "creating protocol ballot...");

				ProtocolBallot ballot = new ProtocolBallot(options, proof);

				// send the vote over the network
				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "send ballot...");
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

		// progressDialog.dismiss();

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
		ProtocolPoll pp = (ProtocolPoll) poll;


		List<XMLOption> xmlOptions = new ArrayList<XMLOption>();
		for (Option op : pp.getOptions()) {
			xmlOptions.add(new XMLOption(op));
		}

		List<XMLParticipant> xmlParticipants = new ArrayList<XMLParticipant>();
		for (Participant p : pp.getParticipants().values()) {

			List<XMLGqElement> xmlCoefficientCommitments = new ArrayList<XMLGqElement>();
			
			for (int i = 0; i < participantCoefficientCommitments.get(p.getUniqueId()).length; i++) {
				xmlCoefficientCommitments
						.add(new XMLGqElement(participantCoefficientCommitments
								.get(p.getUniqueId())[i].getValue()
								.toString(10)));
			}

			XMLGqElement xmlKeyShareCommitment = new XMLGqElement(
					receivedShareCommitments.get(p.getUniqueId()).getValue()
							.toString(10));
						
			XMLPartDecryption xmlPartDecryption = new XMLPartDecryption(partDecryptions.get(p.getUniqueId()));
			
			XMLBallot xmlBallot = null;
			if (ballots.get(p.getUniqueId()) != null){
				 xmlBallot = new XMLBallot(ballots.get(p.getUniqueId()));
			}
			
			XMLParticipant xmlParticipant = new XMLParticipant(
					p.getIdentification(), p.getUniqueId(),
					xmlCoefficientCommitments, xmlKeyShareCommitment,
					xmlPartDecryption, xmlBallot);
			xmlParticipants.add(xmlParticipant);
		}

		XMLMultiEncryptionPoll xmlPoll = new XMLMultiEncryptionPoll(
				pp.getQuestion(),
				xmlOptions,
				xmlParticipants,
				gQ.getModulus().toString(10),
				gQ.getZModOrder().getModulus().toString(10),
				new XMLGqElement(((BigInteger)elGamal.getGenerator().getValue()).toString(10)),
				new XMLGqElement(publicKey.getValue().toString(10)), pp
						.getThreshold());
		Serializer serializer = new Persister();
		try {
			serializer.write(xmlPoll, file);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancelVotingPeriod() {
		
	}

	public void handleReceiveCommitments(
			GStarModElement[] coefficientCommitments, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got commitments from " + sender);
		participantCoefficientCommitments.put(sender, coefficientCommitments);

	}

	public void handleReceiveShare(final ZModElement keyShare,
			final String sender) {
		Log.d(this.getClass().getSimpleName(), "Got a keyshare from " + sender);

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				// Check the keyshares using the commitments sent earlier
				GStarModElement[] coefficientCommitments = participantCoefficientCommitments
						.get(sender);

				StringElement id = StringMonoid.getInstance(
						Alphabet.PRINTABLE_ASCII).getElement(
						AndroidApplication.getInstance().getNetworkInterface()
								.getMyUniqueId());

				FiniteByteArrayElement trusteeId = id.getHashValue();

				GStarModElement product = gQ.getIdentityElement();

				for (int i = 0; i < coefficientCommitments.length; i++) {
					// coefficientCommitments[i] ^ trusteeId ^ degree
					product = product.apply(coefficientCommitments[i]
							.power(ModuloFunction
									.getInstance(trusteeId.getSet(), zQ)
									.apply(trusteeId).power(i)));
				}

				// check the received share
				if (gQ.getDefaultGenerator().power(keyShare)
						.isEquivalent(product)) {
					publicKey = publicKey.apply(coefficientCommitments[0]);
					protocolPoll.setPublicKey(publicKey);
					receivedShares.put(sender, keyShare);
					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(), "Key Share of " + sender + " ok");
				} else {
					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(), "Key Share of " + sender
							+ " NOT ok");
				}

				// As soon as we have enough shares, we can calculate the our
				// key share and broadcast a commitment
				if (receivedShares.size() == protocolPoll
						.getNumberOfParticipants()) {
					publicKeyComplete = true;
					for (ZModElement share : receivedShares.values()) {
						CGS97ProtocolMultiEncryption.this.keyShare = CGS97ProtocolMultiEncryption.this.keyShare
								.add(share);
					}
					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(),
							"Broadcasting keyshare coefficient");
					AndroidApplication
							.getInstance()
							.getNetworkInterface()
							.sendMessage(
									new VoteMessage(
											VoteMessage.Type.VOTE_MESSAGE_KEY_SHARE_COMMITMENT,
											((GStarModElement) elGamal
													.getGenerator())
													.power(CGS97ProtocolMultiEncryption.this.keyShare)));
				}
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	protected void handleReceiveShareCommitment(
			GStarModElement keyShareCommitment, String sender) {
		Log.d(this.getClass().getSimpleName(), "Got keyshare commitment from "
				+ sender);
		receivedShareCommitments.put(sender, keyShareCommitment);
	}

	protected void handleReceiveVote(final ProtocolBallot ballot,
			final String sender) {

		Log.d(this.getClass().getSimpleName(), "handleReceiveVote called...");
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				if (!protocolPoll.getParticipants().get(sender).hasVoted()) {

					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(), "Got ballot from " + sender);

					boolean ballotIsValid = true;

					if (isInElectorate) {
						while (publicKeyComplete == false) {
							try {
								Log.d(CGS97ProtocolMultiEncryption.this
										.getClass().getSimpleName(),
										"Waiting for public key to be completely available...");
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						protocolPoll.getParticipants().get(sender)
								.setHasVoted(true);

						Element proofElement = StringMonoid.getInstance(
								Alphabet.PRINTABLE_ASCII).getElement(
								protocolPoll.toString());

						if (scg == null) {
							scg = ElGamalEncryptionValidityProofGenerator
									.createNonInteractiveChallengeGenerator(
											elGamal, possibleMessages.length,
											proofElement);
						}

						if (pg == null) {
							Subset plaintexts = Subset.getInstance(gQ,
									possibleMessages);
							pg = ElGamalEncryptionValidityProofGenerator
									.getInstance(scg, elGamal, publicKey,
											plaintexts);
						}

						GStarModElement ballotProductLeft = gQ
								.getIdentityElement();
						GStarModElement ballotProductRight = gQ
								.getIdentityElement();

						for (int i = 0; i < ballot.getOptions().size(); i++) {
							Pair currentProduct = ballotProducts.get(i);

							ballotProductLeft = ballotProductLeft
									.multiply(ballot.getOptions().get(i)
											.getBallotOptionEncryption()
											.getFirst());
							ballotProductRight = ballotProductRight
									.multiply(ballot.getOptions().get(i)
											.getBallotOptionEncryption()
											.getSecond());
							GStarModElement productLeft = ((GStarModElement) currentProduct
									.getFirst()).multiply(ballot.getOptions()
									.get(i).getBallotOptionEncryption()
									.getFirst());
							GStarModElement productRight = ((GStarModElement) currentProduct
									.getSecond()).multiply(ballot.getOptions()
									.get(i).getBallotOptionEncryption()
									.getSecond());
							ballotProducts
									.set(i, Pair.getInstance(productLeft,
											productRight));
							BooleanElement ballotValidElement = pg.verify(
									ballot.getOptions().get(i)
											.getValidityProof(), ballot
											.getOptions().get(i)
											.getBallotOptionEncryption());
							Log.d(CGS97ProtocolMultiEncryption.this.getClass()
									.getSimpleName(), "Ballot valid: "
									+ ballotValidElement);
							if (!ballotValidElement.getValue()) {
								ballotIsValid = false;
							}
						}

						if (pg.verify(
								ballot.getValidityProof(),
								Pair.getInstance(ballotProductLeft,
										ballotProductRight)).getValue() == false) {
							ballotIsValid = false;
						}

					}

					if (ballotIsValid) {
						ballots.put(sender, ballot);
						protocolPoll.getParticipants().get(sender)
								.setHasVoted(true);
						Intent i = new Intent(
								BroadcastIntentTypes.newIncomingVote);
						i.putExtra("votes", ballots.size());
						i.putExtra("options",
								(Serializable) protocolPoll.getOptions());
						i.putExtra("participants",
								(Serializable) protocolPoll.getParticipants());
						LocalBroadcastManager.getInstance(context)
								.sendBroadcast(i);
						if (ballots.size() >= protocolPoll
								.getNumberOfParticipants()) {
							tallyDone = true;
							if (voteUpdaterThread != null) {
								voteUpdaterThread.interrupt();
							}
							Log.d(CGS97ProtocolMultiEncryption.this.getClass()
									.getSimpleName(),
									"calling partdecrypt, number of participants: "
											+ protocolPoll
													.getNumberOfParticipants()
											+ " ballots size: "
											+ ballots.size());
							if (isInElectorate) {
								partDecrypt(protocolPoll);
							} else {
								computeResult(protocolPoll);
							}
						} else {
							if (voteUpdaterThread == null) {
								voteUpdaterThread = new VoteUpdaterThread();
								voteUpdaterThread.start();
							}
						}
					}

				}
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void partDecrypt(ProtocolPoll poll) {

		Log.d(this.getClass().getSimpleName(), "Running part decryption...");

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "Number of shares combined: "
						+ receivedShares.size());

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "My Keyshare: " + keyShare);

				List<GStarModElement> partDecryptions = new ArrayList<GStarModElement>();
				List<Triple> proofs = new ArrayList<Triple>();

				for (Pair ballotProduct : ballotProducts) {

					GStarModElement partDecryption = ((GStarModElement) ballotProduct
							.getFirst()).power(keyShare);

					Function f1 = GeneratorFunction.getInstance(elGamal
							.getGenerator());
					Function f2 = GeneratorFunction.getInstance(ballotProduct
							.getFirst());

					ProductFunction f = ProductFunction.getInstance(f1, f2);

					SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator
							.getInstance(f.getCoDomain(), (ProductSemiGroup) f
									.getCoDomain(), ZMod.getInstance(f
									.getDomain().getMinimalOrder()));

					PreimageEqualityProofGenerator pg = PreimageEqualityProofGenerator
							.getInstance(scg, f1, f2);

					Element privateInput = CGS97ProtocolMultiEncryption.this.keyShare;
					Element publicInput = Tuple
							.getInstance(
									((GStarModElement) elGamal.getGenerator())
											.power(CGS97ProtocolMultiEncryption.this.keyShare),
									partDecryption);

					Triple proof = pg.generate(privateInput, publicInput);

					partDecryptions.add(partDecryption);
					proofs.add(proof);
				}

				ProtocolPartDecryption protocolPartDecryption = new ProtocolPartDecryption(
						partDecryptions, proofs);

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "Sending part decryption");
				AndroidApplication
						.getInstance()
						.getNetworkInterface()
						.sendMessage(
								new VoteMessage(
										VoteMessage.Type.VOTE_MESSAGE_PART_DECRYPTION,
										protocolPartDecryption));
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	protected void handlePartDecryption(
			final ProtocolPartDecryption protocolPartDecryption,
			final String sender) {

		Log.d(this.getClass().getSimpleName(),
				"handlePartDecryption started...");

		Log.d(this.getClass().getSimpleName(), "Got part decryption from "
				+ sender);

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				while (tallyDone == false) {
					try {
						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(), "Waiting for tallying...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				boolean acceptDecryption = true;
				for (int i = 0; i < protocolPartDecryption.getPartDecryptions()
						.size(); i++) {
					Function f1 = GeneratorFunction.getInstance(elGamal
							.getGenerator());
					Function f2 = GeneratorFunction.getInstance(ballotProducts
							.get(i).getFirst());
					ProductFunction f = ProductFunction.getInstance(f1, f2);

					SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator
							.getInstance(f.getCoDomain(), (ProductSemiGroup) f
									.getCoDomain(), ZMod.getInstance(f
									.getDomain().getMinimalOrder()));

					PreimageEqualityProofGenerator pg = PreimageEqualityProofGenerator
							.getInstance(scg, f1, f2);

					Element publicInput = Tuple.getInstance(
							receivedShareCommitments.get(sender),
							protocolPartDecryption.getPartDecryptions().get(i));

					if (pg.verify(protocolPartDecryption.getProofs().get(i),
							publicInput).getValue()) {
						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(), "Accepting part decryption "
								+ i + " of " + sender);
					} else {
						acceptDecryption = false;
					}

				}

				if (acceptDecryption) {
					// senderParticipant.setPartDecryption(partDecryption);
					partDecryptions.put(sender, protocolPartDecryption);

					if (partDecryptions.size() == protocolPoll.getThreshold()) {
						Log.d(CGS97ProtocolMultiEncryption.this.getClass()
								.getSimpleName(),
								"calling interpolateResult...");
						interpolateResult();
					}
				} else {
					partDecryptionRejections++;

					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(), "Rejecting partdecryption of "
							+ sender);
				}

				Log.d(CGS97ProtocolMultiEncryption.this.getClass()
						.getSimpleName(), "handlePartDecryption finished...");
				return null;
			}

			@Override
			protected void onPostExecute(Object result) {

				if (partDecryptionRejections > protocolPoll
						.getNumberOfParticipants()
						- protocolPoll.getThreshold()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							AndroidApplication.getInstance()
									.getCurrentActivity());
					// Add the buttons
					builder.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									context.startActivity(new Intent(
											AndroidApplication.getInstance()
													.getCurrentActivity(),
											MainActivity.class)
											.addFlags(
													Intent.FLAG_ACTIVITY_NEW_TASK)
											.addFlags(
													Intent.FLAG_ACTIVITY_CLEAR_TASK));
								}
							});
					builder.setMessage(R.string.dialog_too_many_incorrect_decryptions);
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

				super.onPostExecute(result);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	private synchronized void interpolateResult() {

		Log.d(this.getClass().getSimpleName(), "interpolateResult started...");
		Log.d(this.getClass().getSimpleName(), "partdecryptions available: "
				+ partDecryptions.size());

		for (int i = 0; i < protocolPoll.getOptions().size(); i++) {

			// converting the unique trustee id and the part decryption to a
			// point
			Pair[] pairs = new Pair[partDecryptions.size()];
			int length = pairs.length;

			Iterator<Entry<String, ProtocolPartDecryption>> it = partDecryptions
					.entrySet().iterator();

			for (int j = 0; j < pairs.length; j++) {
				Entry<String, ProtocolPartDecryption> entry = it.next();

				StringElement id = StringMonoid.getInstance(
						Alphabet.PRINTABLE_ASCII).getElement(entry.getKey());
				FiniteByteArrayElement trusteeId = id.getHashValue();

				pairs[j] = Pair.getInstance(
						ModuloFunction.getInstance(trusteeId.getSet(), zQ)
								.apply(trusteeId), entry.getValue()
								.getPartDecryptions().get(i));
			}

			// Calculating the lagrange coefficients for each point we got
			DualisticElement lagrangeProduct = null;
			DualisticElement[] lagrangeCoefficients = new DualisticElement[length];
			for (int j = 0; j < length; j++) {
				lagrangeProduct = null;
				DualisticElement elementJ = (DualisticElement) pairs[j]
						.getFirst();
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

			for (int j = 0; j < pairs.length; j++) {
				if (product == null) {
					product = ((GStarModElement) pairs[j].getSecond())
							.power(lagrangeCoefficients[j]);
				} else {
					product = product.multiply(((GStarModElement) pairs[j]
							.getSecond()).power(lagrangeCoefficients[j]));
				}
			}

			GStarModElement result = ((GStarModElement) ballotProducts.get(i)
					.getSecond()).divide(product);

			for (int j = 0; j <= protocolPoll.getNumberOfParticipants(); j++) {
				if (gQ.getDefaultGenerator().power(j).equals(result)) {
					Log.d(CGS97ProtocolMultiEncryption.this.getClass()
							.getSimpleName(), "Result found for Option "
							+ protocolPoll.getOptions().get(i).getText()
							+ " : " + j);
					protocolPoll.getOptions().get(i).setVotes(j);
					break;
				}
			}

		}

		computeResult(protocolPoll);

	}

	/**
	 * Indicate if the peer identified with the given string is contained in the
	 * list of participants
	 * 
	 * @param uniqueId
	 *            identifier of the peer
	 * @return true if it is contained in the list of participants, false
	 *         otherwise
	 */
	private boolean isContainedInParticipants(String uniqueId,
			Collection<Participant> participants) {
		for (Participant p : participants) {
			if (p.getUniqueId().equals(uniqueId)) {
				return true;
			}
		}
		return false;
	}

	class VoteUpdaterThread extends Thread {

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				Log.d(this.getClass().getSimpleName(), "Sending vote update...");
				Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
				i.putExtra("votes", ballots.size());
				i.putExtra("options", (Serializable) protocolPoll.getOptions());
				i.putExtra("participants",
						(Serializable) protocolPoll.getParticipants());
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);
				SystemClock.sleep(1000);
			}
		}
	}

	@Override
	public void activate() {
		// Register a BroadcastReceiver for receiving the commitments to the
		// coefficients during the key generation phase
		coefficientCommitReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {

				if (isInElectorate) {
					handleReceiveCommitments(
							(GStarModElement[]) intent
									.getSerializableExtra("coefficientCommitments"),
							intent.getStringExtra("sender"));
				}
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
				if (isInElectorate) {
					handleReceiveShare(
							(ZModElement) intent
									.getSerializableExtra("keyShare"),
							intent.getStringExtra("sender"));
				}
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				keyShareReceiver,
				new IntentFilter(BroadcastIntentTypes.keyShare));

		keyShareCommitmentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				if (isInElectorate) {
					handleReceiveShareCommitment(
							(GStarModElement) intent
									.getSerializableExtra("keyShareCommitment"),
							intent.getStringExtra("sender"));
				}
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				keyShareCommitmentReceiver,
				new IntentFilter(BroadcastIntentTypes.keyShareCommitment));

		voteReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				ProtocolBallot ballot = (ProtocolBallot) intent
						.getSerializableExtra("vote");

				handleReceiveVote(ballot, intent.getStringExtra("voter"));
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				voteReceiver, new IntentFilter(BroadcastIntentTypes.newVote));

		partDecryptionReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent intent) {
				if (isInElectorate) {
					ProtocolPartDecryption protocolPartDecryption = (ProtocolPartDecryption) intent
							.getSerializableExtra("partDecryption");
					handlePartDecryption(protocolPartDecryption,
							intent.getStringExtra("sender"));
				}
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				partDecryptionReceiver,
				new IntentFilter(BroadcastIntentTypes.partDecryption));

		// Register a BroadcastReceiver on stop poll order events
		stopReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				if (isInElectorate) {
					tallyDone = true;
					partDecrypt(protocolPoll);
				}
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				stopReceiver, new IntentFilter(BroadcastIntentTypes.stopVote));
	}

	@Override
	public void deactivate() {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				coefficientCommitReceiver);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				keyShareReceiver);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				keyShareCommitmentReceiver);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				voteReceiver);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				partDecryptionReceiver);
		LocalBroadcastManager.getInstance(context).unregisterReceiver(
				stopReceiver);

	}

}
