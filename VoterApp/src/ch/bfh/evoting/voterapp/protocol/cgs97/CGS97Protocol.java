package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
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
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.MainActivity;
import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.ProtocolInterface;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLBallot;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLEqualityProof;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLGqElement;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLGqPair;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLOption;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLPartDecryption;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLParticipant;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLPoll;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLValidityProof;
import ch.bfh.evoting.voterapp.protocol.cgs97.xml.XMLZqElement;
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

	private ConcurrentHashMap<String, GStarModElement[]> participantCoefficientCommitments = new ConcurrentHashMap<String, GStarModElement[]>();

	private ConcurrentHashMap<String, ProtocolPartDecryption> partDecryptions = new ConcurrentHashMap<String, ProtocolPartDecryption>();

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

	private boolean tallyDone = false;
	private boolean publicKeyComplete = false;

	private int partDecryptionRejections = 0;

	private ZModElement keyShare = zQ.getIdentityElement();

	private boolean isInElectorate;

	public CGS97Protocol(Context context) {
		super(context);
		this.context = context;

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

		publicKey = gQ.getIdentityElement();
		productLeft = gQ.getIdentityElement();
		productRight = gQ.getIdentityElement();
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
							.getDegree() + 1];

					for (int i = 0; i <= polynomial.getDegree(); i++) {
						coefficientCommitments[i] = gQ.getDefaultGenerator()
								.power(polynomial.getCoefficient(i));
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
					// using
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
						Log.d(CGS97Protocol.this.getClass().getSimpleName(),
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
						
						Log.d(CGS97Protocol.this.getClass().getSimpleName(), "Sending key share to " + participant.getUniqueId());
						
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

					final int NUMBER_OF_OPTIONS = protocolPoll.getOptions()
							.size();

					numberOfBitsPerOption = (int) Math.ceil(Math
							.log(protocolPoll.getNumberOfParticipants())
							/ Math.log(2));

					possibleMessages = new GStarModElement[NUMBER_OF_OPTIONS];
					BigInteger shiftedBigInteger;
					for (int i = 0; i < NUMBER_OF_OPTIONS; i++) {
						shiftedBigInteger = BigInteger.valueOf(1).shiftLeft(
								i * numberOfBitsPerOption);
						possibleMessages[i] = gQ.getDefaultGenerator().power(
								shiftedBigInteger);

					}

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
						Log.d(CGS97Protocol.this.getClass().getSimpleName(),
								"Waiting for public key to be completely available...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"encrypt ballot using the following public key:");
				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						publicKey.toString());

				int index = poll.getOptions().indexOf(selectedOption);

				ZModElement randomization = zQ.getRandomElement();

				Tuple ballotEncryption = elGamal.encrypt(publicKey,
						possibleMessages[index], randomization);

				Element proofElement = StringMonoid.getInstance(
						Alphabet.PRINTABLE_ASCII).getElement(
						protocolPoll.toString());

				scg = ElGamalEncryptionValidityProofGenerator
						.createNonInteractiveChallengeGenerator(elGamal,
								possibleMessages.length, proofElement);

				Subset plaintexts = Subset.getInstance(gQ, possibleMessages);

				pg = ElGamalEncryptionValidityProofGenerator.getInstance(scg,
						elGamal, publicKey, plaintexts);

				Tuple privateInput = pg
						.createPrivateInput(randomization, index);
				Tuple proof = pg.generate(privateInput, ballotEncryption);

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"creating protocol ballot...");

				ProtocolBallot ballot = new ProtocolBallot(ballotEncryption,
						proof);

				// send the vote over the network
				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"send ballot...");
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
			XMLGqElement representation = new XMLGqElement(possibleMessages[pp
					.getOptions().indexOf(op)].getValue().toString(10));
			XMLOption xop = new XMLOption(op.getText(), op.getVotes(),
					representation);
			xmlOptions.add(xop);
		}

		List<XMLParticipant> xmlParticipants = new ArrayList<XMLParticipant>();
		for (Participant p : pp.getParticipants().values()) {

			List<XMLGqElement> xmlCoefficientCommitments = new ArrayList<XMLGqElement>();
			for (int i = 0; i < participantCoefficientCommitments.get(p
					.getUniqueId()).length; i++) {
				xmlCoefficientCommitments
						.add(new XMLGqElement(participantCoefficientCommitments
								.get(p.getUniqueId())[i].getValue()
								.toString(10)));
			}

			XMLGqElement xmlKeyShareCommitment = new XMLGqElement(
					receivedShareCommitments.get(p.getUniqueId()).getValue()
							.toString(10));

			XMLPartDecryption xmlPartDecryption = null;
			if (partDecryptions.get(p.getUniqueId()) != null){
				XMLGqElement partDecryptionValue = new XMLGqElement(partDecryptions
						.get(p.getUniqueId()).getPartDecryption().getValue()
						.toString(10));
	
				Tuple equalityProof = partDecryptions.get(p.getUniqueId())
						.getProof();
				XMLGqElement valueT1 = new XMLGqElement(
						((Tuple) equalityProof.getAt(0)).getAt(0).getValue()
								.toString(10));
				XMLGqElement valueT2 = new XMLGqElement(
						((Tuple) equalityProof.getAt(0)).getAt(1).getValue()
								.toString(10));
				XMLZqElement valueC = new XMLZqElement(equalityProof.getAt(1)
						.getValue().toString(10));
				XMLZqElement valueS = new XMLZqElement(equalityProof.getAt(2)
						.getValue().toString(10));
				XMLEqualityProof xmlEqualityProof = new XMLEqualityProof(valueT1,
						valueT2, valueC, valueS);
	
				xmlPartDecryption = new XMLPartDecryption(
						partDecryptionValue, xmlEqualityProof);
			
			} else {
				// Handling the case where not all part decryptions were used...
				XMLGqElement partDecryptionValue = new XMLGqElement("N/A");
				XMLGqElement valueT1 = new XMLGqElement("N/A");
				XMLGqElement valueT2 = new XMLGqElement("N/A");
				XMLZqElement valueC = new XMLZqElement("N/A");
				XMLZqElement valueS = new XMLZqElement("N/A");
				XMLEqualityProof xmlEqualityProof = new XMLEqualityProof(valueT1,
						valueT2, valueC, valueS);
	
				xmlPartDecryption = new XMLPartDecryption(
						partDecryptionValue, xmlEqualityProof);
			}
			
			XMLBallot xmlBallot;

			if (ballots.get(p.getUniqueId()) != null){
				XMLGqElement xmlLeft = new XMLGqElement(ballots
						.get(p.getUniqueId()).getBallot().getAt(0).getValue()
						.toString(10));
				XMLGqElement xmlRight = new XMLGqElement(ballots
						.get(p.getUniqueId()).getBallot().getAt(1).getValue()
						.toString(10));
	
				XMLGqPair xmlBallotEncryption = new XMLGqPair(xmlLeft, xmlRight);
	
				Tuple validityProof = ballots.get(p.getUniqueId())
						.getValidityProof();
				Tuple subPartT = (Tuple) validityProof.getAt(0); // list of Gq pairs
				Tuple subPartC = (Tuple) validityProof.getAt(1); // list
																	// ZModElements
				Tuple subPartS = (Tuple) validityProof.getAt(2); // list
																	// ZModElements
				List<XMLGqPair> valueListT = new ArrayList<XMLGqPair>();
				for (Element e : subPartT.getAll()) {
					Tuple tuple = (Tuple) e;
					XMLGqPair pair = new XMLGqPair(new XMLGqElement(tuple.getAt(0)
							.getValue().toString(10)), new XMLGqElement(tuple
							.getAt(1).getValue().toString(10)));
					valueListT.add(pair);
				}
				List<XMLZqElement> valueListC = new ArrayList<XMLZqElement>();
				for (Element e : subPartC.getAll()) {
					valueListC.add(new XMLZqElement(e.getValue().toString(10)));
				}
				List<XMLZqElement> valueListS = new ArrayList<XMLZqElement>();
				for (Element e : subPartS.getAll()) {
					valueListS.add(new XMLZqElement(e.getValue().toString(10)));
				}
	
				XMLValidityProof xmlValidityProof = new XMLValidityProof(
						valueListT, valueListC, valueListS);
	
				xmlBallot = new XMLBallot(xmlBallotEncryption,
						xmlValidityProof);
			}
			else {
				XMLGqPair xmlBallotEncryption = new XMLGqPair(new XMLGqElement("N/A"), new XMLGqElement("N/A"));
				XMLValidityProof xmlValidityProof = new XMLValidityProof(
						new ArrayList<XMLGqPair>(), new ArrayList<XMLZqElement>(), new ArrayList<XMLZqElement>());
				
				xmlBallot = new XMLBallot(xmlBallotEncryption,
						xmlValidityProof);
			}
			

			XMLParticipant xmlParticipant = new XMLParticipant(
					p.getIdentification(), p.getUniqueId(),
					xmlCoefficientCommitments, xmlKeyShareCommitment,
					xmlPartDecryption, xmlBallot);
			xmlParticipants.add(xmlParticipant);
		}

		XMLPoll xmlPoll = new XMLPoll(
				pp.getQuestion(),
				xmlOptions,
				xmlParticipants,
				gQ.getModulus().toString(10),
				gQ.getZModOrder().getModulus().toString(10),
				new XMLGqElement(elGamal.getGenerator().getValue().toString(10)),
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
		// TODO Auto-generated method stub

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
					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Key Share of " + sender + " ok");
				} else {
					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Key Share of " + sender + " NOT ok");
				}

				// As soon as we have enough shares, we can calculate the our
				// key share and broadcast a commitment
				if (receivedShares.size() == protocolPoll
						.getNumberOfParticipants()) {
					publicKeyComplete = true;
					for (ZModElement share : receivedShares.values()) {
						CGS97Protocol.this.keyShare = CGS97Protocol.this.keyShare
								.add(share);
					}
					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Broadcasting keyshare coefficient");
					AndroidApplication
							.getInstance()
							.getNetworkInterface()
							.sendMessage(
									new VoteMessage(
											VoteMessage.Type.VOTE_MESSAGE_KEY_SHARE_COMMITMENT,
											elGamal.getGenerator()
													.power(CGS97Protocol.this.keyShare)));
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

					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Got ballot from " + sender);

					if (isInElectorate) {
						while (publicKeyComplete == false) {
							try {
								Log.d(CGS97Protocol.this.getClass()
										.getSimpleName(),
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

						if (pg.verify(ballot.getValidityProof(),
								ballot.getBallot()).getBoolean()) {
							Log.d(CGS97Protocol.this.getClass().getSimpleName(),
									"Ballot ok, counting");
							productLeft = productLeft
									.multiply((GStarModElement) ballot
											.getBallot().getAt(0));
							productRight = productRight
									.multiply((GStarModElement) ballot
											.getBallot().getAt(1));
						} else {
							Log.d(CGS97Protocol.this.getClass().getSimpleName(),
									"Ballot NOT ok, NOT counting");
						}

					}

					ballots.put(sender, ballot);
					protocolPoll.getParticipants().get(sender)
							.setHasVoted(true);
					Intent i = new Intent(BroadcastIntentTypes.newIncomingVote);
					i.putExtra("votes", ballots.size());
					i.putExtra("options",
							(Serializable) protocolPoll.getOptions());
					i.putExtra("participants",
							(Serializable) protocolPoll.getParticipants());
					LocalBroadcastManager.getInstance(context).sendBroadcast(i);
					if (ballots.size() >= protocolPoll
							.getNumberOfParticipants()) {
						tallyDone = true;
						if (voteUpdaterThread != null) {
							voteUpdaterThread.interrupt();
						}
						Log.d(CGS97Protocol.this.getClass().getSimpleName(),
								"calling partdecrypt, number of participants: "
										+ protocolPoll
												.getNumberOfParticipants()
										+ " ballots size: " + ballots.size());
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
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void partDecrypt(ProtocolPoll poll) {

		Log.d(this.getClass().getSimpleName(), "Running part decryption...");

		// progressDialog = new ProgressDialog(AndroidApplication.getInstance()
		// .getCurrentActivity());
		// progressDialog.setMessage("Starting tallying process...");
		// progressDialog.show();

		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"Number of shares combined: " + receivedShares.size());

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"My Keyshare: " + keyShare);

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"productLeft: " + productLeft);
				GStarModElement partDecryption = productLeft.power(keyShare);

				Function f1 = GeneratorFunction.getInstance(elGamal
						.getGenerator());
				Function f2 = GeneratorFunction.getInstance(productLeft);

				ProductFunction f = ProductFunction.getInstance(f1, f2);

				SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator
						.getInstance(f.getCoDomain(), (ProductSemiGroup) f
								.getCoDomain(), ZMod.getInstance(f.getDomain()
								.getMinimalOrder()));

				PreimageEqualityProofGenerator pg = PreimageEqualityProofGenerator
						.getInstance(scg, f1, f2);

				Element privateInput = CGS97Protocol.this.keyShare;
				Element publicInput = Tuple.getInstance(elGamal.getGenerator()
						.power(CGS97Protocol.this.keyShare), partDecryption);

				Triple proof = pg.generate(privateInput, publicInput);

				ProtocolPartDecryption protocolPartDecryption = new ProtocolPartDecryption(
						partDecryption, proof);

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"Sending part decryption");
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
						Log.d(CGS97Protocol.this.getClass().getSimpleName(),
								"Waiting for tallying...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				Function f1 = GeneratorFunction.getInstance(elGamal
						.getGenerator());
				Function f2 = GeneratorFunction.getInstance(productLeft);

				ProductFunction f = ProductFunction.getInstance(f1, f2);

				SigmaChallengeGenerator scg = StandardNonInteractiveSigmaChallengeGenerator
						.getInstance(f.getCoDomain(), (ProductSemiGroup) f
								.getCoDomain(), ZMod.getInstance(f.getDomain()
								.getMinimalOrder()));

				PreimageEqualityProofGenerator pg = PreimageEqualityProofGenerator
						.getInstance(scg, f1, f2);

				Element publicInput = Tuple.getInstance(
						receivedShareCommitments.get(sender),
						protocolPartDecryption.getPartDecryption());

				if (pg.verify(protocolPartDecryption.getProof(), publicInput)
						.getBoolean()) {
					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Accepting part decryption of " + sender);
					// senderParticipant.setPartDecryption(partDecryption);
					partDecryptions.put(sender, protocolPartDecryption);

					if (partDecryptions.size() == protocolPoll.getThreshold()) {
						Log.d(CGS97Protocol.this.getClass().getSimpleName(),
								"calling interpolateResult...");
						interpolateResult();
					}
				} else {
					partDecryptionRejections++;

					Log.d(CGS97Protocol.this.getClass().getSimpleName(),
							"Rejecting partdecryption of " + sender);
				}

				Log.d(CGS97Protocol.this.getClass().getSimpleName(),
						"handlePartDecryption finished...");
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

		// converting the unique trustee id and the part decryption to a point
		Pair[] pairs = new Pair[partDecryptions.size()];
		int length = pairs.length;

		Iterator<Entry<String, ProtocolPartDecryption>> it = partDecryptions
				.entrySet().iterator();
		
		for (int i = 0; i < pairs.length; i++){
			Entry<String, ProtocolPartDecryption> entry = it.next();

			StringElement id = StringMonoid.getInstance(
					Alphabet.PRINTABLE_ASCII).getElement(entry.getKey());
			FiniteByteArrayElement trusteeId = id.getHashValue();

			pairs[i] = Pair.getInstance(
					ModuloFunction.getInstance(trusteeId.getSet(), zQ).apply(
							trusteeId), entry.getValue().getPartDecryption());
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
						+ ballots.size() + " ballots and "
						+ numberOfBitsPerOption + " Bits per option.");
		getCombinations(protocolPoll.getOptions().size(), new Stack<Integer>(),
				ballots.size());

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

	}

	private void getCombinations(int length, Stack<Integer> used, int val) {
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
	};

}
