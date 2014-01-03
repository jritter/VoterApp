package ch.bfh.evoting.voterapp.protocol;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.Option;
import ch.bfh.evoting.voterapp.entities.Participant;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolOption;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolParticipant;
import ch.bfh.evoting.voterapp.protocol.hkrs12.ProtocolPoll;
import ch.bfh.evoting.voterapp.protocol.hkrs12.statemachine.StateMachineManager;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.xml.XMLEqualityProof;
import ch.bfh.evoting.voterapp.util.xml.XMLGqElement;
import ch.bfh.evoting.voterapp.util.xml.XMLGqPair;
import ch.bfh.evoting.voterapp.util.xml.XMLKnowledgeProof;
import ch.bfh.evoting.voterapp.util.xml.XMLOption;
import ch.bfh.evoting.voterapp.util.xml.XMLParticipant;
import ch.bfh.evoting.voterapp.util.xml.XMLPoll;
import ch.bfh.evoting.voterapp.util.xml.XMLValidityProof;
import ch.bfh.evoting.voterapp.util.xml.XMLZqElement;
import ch.bfh.unicrypt.crypto.random.classes.PseudoRandomOracle;
import ch.bfh.unicrypt.crypto.random.interfaces.RandomReferenceString;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayMonoid;
import ch.bfh.unicrypt.math.algebra.general.classes.FiniteByteArrayElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class HKRS12ProtocolInterface extends ProtocolInterface {

	private static final String TAG = HKRS12ProtocolInterface.class.getSimpleName();
	private Context context;
	private StateMachineManager stateMachineManager;

	public HKRS12ProtocolInterface(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public void showReview(final Poll poll) {

		Log.e(TAG, "Show preview thread:"+Thread.currentThread().getId());
		//send broadcast to dismiss the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		new AsyncTask<Object, Object, Object>(){

			@Override
			protected Object doInBackground(Object... arg0) {
				Log.e(TAG, "Show preview async thread:"+Thread.currentThread().getId());

				//do some protocol specific stuff to the poll
				//transform poll in protocol poll
				ProtocolPoll newPoll = new ProtocolPoll(poll);

				//compute Baudron et al
				Element two = newPoll.getZ_q().getElement(BigInteger.valueOf(2));
				BigInteger pow2i;
				int m=-1;
				do{
					m++;
					pow2i = two.getValue().pow(m);
				}while(pow2i.compareTo(BigInteger.valueOf(poll.getNumberOfParticipants()))<1);

				//transform options in protocol options and create a "generator" for each option
				int j=0;
				List<Option> options = new ArrayList<Option>();
				for(Option op : poll.getOptions()){
					ProtocolOption protocolOption = new ProtocolOption(op);
					protocolOption.setRepresentation(newPoll.getZ_q().getElement(two.getValue().pow(j*m)));
					options.add(protocolOption);
					j++;
				}
				newPoll.setOptions(options);
				newPoll.generateGenerator();

				//transform participants in protocol participants
				Map<String, Participant> participants = new TreeMap<String,Participant>();
				int i=0;
				for(Participant p : poll.getParticipants().values()){
					ProtocolParticipant protocolParticipant = new ProtocolParticipant(p);
					protocolParticipant.setProtocolParticipantIndex(i);
					i++;
					participants.put(p.getUniqueId(), protocolParticipant);
				}
				newPoll.setParticipants(participants);

				//Send poll to other participants
				VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_POLL_TO_REVIEW, (Serializable)newPoll);
				AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);


				//send broadcast to dismiss the wait dialog
				Intent intent1 = new Intent(BroadcastIntentTypes.dismissWaitDialog);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

				//Send a broadcast to start the next activity
				Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
				intent.putExtra("poll", (Serializable)newPoll);
				intent.putExtra("sender", AndroidApplication.getInstance().getNetworkInterface().getMyUniqueId());
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);	


				return null;
			}

		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	@Override
	public void beginVotingPeriod(final Poll poll) {

		//Send a broadcast to start the vote activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		//send broadcast to dismiss the wait dialog
		Intent intent1 = new Intent(BroadcastIntentTypes.showWaitDialog);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);

		if(AndroidApplication.getInstance().isAdmin()){
			//called when admin want to begin voting period

			// Send start poll signal over the network
			VoteMessage vm = new VoteMessage(VoteMessage.Type.VOTE_MESSAGE_START_POLL, null);
			AndroidApplication.getInstance().getNetworkInterface().sendMessage(vm);	


		} 

		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {
				//Do some protocol specific stuff
				//reset state if another protocol was run before
				reset();
				stateMachineManager = new StateMachineManager(context, (ProtocolPoll)poll);
				new Thread(stateMachineManager).start();
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
	public void vote(final Option selectedOption, final Poll poll) {



		//Send a broadcast to start the wait for vote activity
		Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
		intent.putExtra("poll", (Serializable)poll);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {

				//do some protocol specific stuff
				int index = -1;
				int j = 0;
				for(Option op : poll.getOptions()){
					if(op.equals(selectedOption)){
						index = j;
						break;
					}
					j++;
				}
				Intent i = new Intent(BroadcastIntentTypes.vote);
				i.putExtra("option", (Serializable)selectedOption);
				i.putExtra("index", index);
				LocalBroadcastManager.getInstance(context).sendBroadcast(i);

				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}


	@Override
	protected void handleReceivedPoll(final Poll poll, final String sender) {
		new AsyncTask<Object, Object, Object>(){
			@Override
			protected Object doInBackground(Object... arg0) {
				//do some protocol specific stuff
				//check if speaking the same language
				if(!(poll instanceof ProtocolPoll)){
					LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BroadcastIntentTypes.differentProtocols));
					Log.e(TAG, "Not using the same protocol");
					return null;
				}

				/*
				 * Because of a modification of the SecureRandom in version 4.2 and higher of Android,
				 * the RandomOracle of UniCrypt (at the current development stage) does not always return
				 * the same generator as it is needed for the following check. So, if the version of Android is
				 * 4.2 or higher, this check is disabled.
				 * 
				 * see: http://android-developers.blogspot.co.at/2013/02/security-enhancements-in-jelly-bean.html
				 * New implementation of SecureRandom
				 * Android 4.2 includes a new default implementation of SecureRandom based on OpenSSL.
				 * In the older Bouncy Castle-based implementation, given a known seed, SecureRandom 
				 * could technically (albeit incorrectly) be treated as a source of deterministic data. 
				 * With the new OpenSSL-based implementation, this is no longer possible.
				 *
				 */
				//if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){

					//computes a generator depending on the text of the poll
					String texts = poll.getQuestion();
					Element[] representations = new Element[poll.getOptions().size()];
					int i=0;
					for(Option op:poll.getOptions()){
						texts += op.getText();
						representations[i]=((ProtocolOption)op).getRepresentation();
						i++;
					}

					Tuple tuple = Tuple.getInstance(representations);
					FiniteByteArrayElement representationsElement = tuple.getHashValue();
					ByteArrayElement textElement = ByteArrayMonoid.getInstance().getElement(texts.getBytes());

					ByteBuffer buffer = ByteBuffer.allocate(textElement.getByteArray().length+representationsElement.getByteArray().length);
					buffer.put(textElement.getByteArray());
					buffer.put(representationsElement.getByteArray());
					buffer.flip(); 

					ProtocolPoll pp = (ProtocolPoll)poll;
					
					RandomReferenceString rrs = PseudoRandomOracle.getInstance().getRandomReferenceString(buffer.array());
					GStarModElement verificationGenerator = pp.getG_q().getIndependentGenerator(1, rrs);
					
					if(!pp.getG_q().areEqual(pp.getGenerator(), verificationGenerator)){
						LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BroadcastIntentTypes.differentPolls));
						Log.e(TAG, "There are some difference between the poll used by the admin and the one received");
						return null;
					}
				//}

				//Send a broadcast to start the review activity
				Intent intent = new Intent(BroadcastIntentTypes.showNextActivity);
				intent.putExtra("poll", (Serializable) poll);
				intent.putExtra("sender", sender);
				LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
				return null;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}


	public StateMachineManager getStateMachineManager(){
		return this.stateMachineManager;
	}

	/**
	 * Reset the state machine
	 * This unregisters the broadcast receivers registered in all actions
	 */
	public void reset(){
		if(stateMachineManager!=null)
			this.stateMachineManager.reset();
		this.stateMachineManager = null;
	}

	@Override
	public void exportToXML(File file, Poll poll) {
		ProtocolPoll pp = (ProtocolPoll)poll;

		List<XMLOption> newOptions = new ArrayList<XMLOption>();
		for(Option op : pp.getOptions()){
			ProtocolOption pop = (ProtocolOption)op;
			XMLOption xop = new XMLOption(pop.getText(), pop.getVotes(), new XMLZqElement(pop.getRepresentation().getValue().toString(10)));
			newOptions.add(xop);
		}
		Log.e(TAG,"XML options prepared");

		List<XMLParticipant> newParticipant = new ArrayList<XMLParticipant>();
		for(Participant p : pp.getParticipants().values()){
			ProtocolParticipant pPart = (ProtocolParticipant)p;
			XMLParticipant xpp = new XMLParticipant();
			xpp.setIdentification(pPart.getIdentification());
			xpp.setUniqueId(pPart.getUniqueId());
			xpp.setProtocolParticipantIndex(pPart.getProtocolParticipantIndex());
			if(pPart.getAi()!=null)
				xpp.setAi(new XMLGqElement(pPart.getAi().getValue().toString(10)));
			if(pPart.getProofForXi()!=null){
				Tuple proof = (Tuple)pPart.getProofForXi();
				XMLGqElement value11 = new XMLGqElement(proof.getAt(0).getValue().toString(10));
				XMLZqElement value12 = new XMLZqElement(proof.getAt(1).getValue().toString(10));
				XMLZqElement value13 = new XMLZqElement(proof.getAt(2).getValue().toString(10));	
				xpp.setProofForXi(new XMLKnowledgeProof(value11, value12, value13));
			}
			if(pPart.getBi()!=null){
				//Does only include Hi if Bi is not null, because when Bi is null, all other participant
				//never received Hi, so the do not include it in their XML
				//if pPart is me, and I didn't vote, without this check, I would include my Hi, but
				//other participant don't. So I don't have to do it, in order to get the same XML file
				//as the others
				xpp.setHi(new XMLGqElement(pPart.getHi().getValue().toString(10)));
			}
			if(pPart.getBi()!=null)
				xpp.setBi(new XMLGqElement(pPart.getBi().getValue().toString(10)));
			if(pPart.getProofValidVote()!=null){
				Tuple proof = (Tuple)pPart.getProofValidVote();
				Tuple subPart11 = (Tuple)proof.getAt(0);//list of Gq pairs
				Tuple subPart12 = (Tuple)proof.getAt(1);//list ZModElements
				Tuple subPart13 = (Tuple)proof.getAt(2);//list ZModElements
				List<XMLGqPair> value11 = new ArrayList<XMLGqPair>();
				for(Element e : subPart11.getAll()){
					Tuple tuple = (Tuple)e;
					XMLGqPair pair = new XMLGqPair(new XMLGqElement(tuple.getAt(0).getValue().toString(10)),
											new XMLGqElement(tuple.getAt(1).getValue().toString(10)));
					value11.add(pair);
				}
				List<XMLZqElement> value12 = new ArrayList<XMLZqElement>();
				for(Element e : subPart12.getAll()){
					value12.add(new XMLZqElement(e.getValue().toString(10)));
				}
				List<XMLZqElement> value13 = new ArrayList<XMLZqElement>();
				for(Element e : subPart13.getAll()){
					value13.add(new XMLZqElement(e.getValue().toString(10)));
				}

				XMLValidityProof xvp = new XMLValidityProof(value11, value12, value13);
				xpp.setProofValidVote(xvp);
			}
			if(pPart.getHiHat()!=null)
				xpp.setHiHat(new XMLGqElement(pPart.getHiHat().getValue().toString(10)));
			if(pPart.getHiHatPowXi()!=null)
				xpp.setHiHatPowXi(new XMLGqElement(pPart.getHiHatPowXi().getValue().toString(10)));
			if(pPart.getProofForHiHat()!=null){
				Tuple proof = (Tuple)pPart.getProofForHiHat();
				XMLGqElement value111 = new XMLGqElement(((Tuple)proof.getAt(0)).getAt(0).getValue().toString(10));
				XMLGqElement value112 = new XMLGqElement(((Tuple)proof.getAt(0)).getAt(1).getValue().toString(10));
				XMLZqElement value12 = new XMLZqElement(proof.getAt(1).getValue().toString(10));	
				XMLZqElement value13 = new XMLZqElement(proof.getAt(2).getValue().toString(10));	
				xpp.setProofForHiHat(new XMLEqualityProof(value111, value112, value12, value13));
			}
			newParticipant.add(xpp);
		}

		XMLPoll xmlPoll = new XMLPoll(pp.getQuestion(), newOptions, newParticipant, pp.getP().toString(10), new XMLGqElement(pp.getGenerator().getValue().toString(10)));
		Serializer serializer = new Persister();
		try {
			serializer.write(xmlPoll, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void cancelVotingPeriod() {
		this.reset();		
	}

}
