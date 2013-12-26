package ch.bfh.evoting.voterapp.network;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import ch.bfh.evoting.voterapp.AndroidApplication;
import ch.bfh.evoting.voterapp.entities.VoteMessage;
import ch.bfh.evoting.voterapp.util.BroadcastIntentTypes;
import ch.bfh.evoting.voterapp.util.SerializationUtil;

/**
 * Abstract implementation of the Network Interface
 * This class only provide a method used to send to correct broadcast depending of the type of message received 
 * @author Philémon von Bergen
 *
 */
public abstract class AbstractNetworkInterface implements NetworkInterface {

	protected Context context;
	protected final SerializationUtil su;

	/**
	 * Create an object of this class
	 * @param context Android context of the application
	 */
	public AbstractNetworkInterface(Context context){
		this.context = context;
		su = AndroidApplication.getInstance().getSerializationUtil();		
	}

	/**
	 * This method checks the message type and inform the application of the new incoming message.
	 * 
	 * @param voteMessage
	 */
	protected final void transmitReceivedMessage(VoteMessage voteMessage) {
		if(voteMessage==null) return;

		Intent messageArrivedIntent;
		switch(voteMessage.getMessageType()){
		case VOTE_MESSAGE_ELECTORATE:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.electorate);
			messageArrivedIntent.putExtra("participants", voteMessage.getMessageContent());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_POLL_TO_REVIEW:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.pollToReview);
			messageArrivedIntent.putExtra("poll", voteMessage.getMessageContent());
			messageArrivedIntent.putExtra("sender", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_ACCEPT_REVIEW:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.acceptReview);
			messageArrivedIntent.putExtra("participant", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_START_POLL:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.startVote);
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_STOP_POLL:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.stopVote);
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_VOTE:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.newVote);
			messageArrivedIntent.putExtra("vote", voteMessage.getMessageContent());
			messageArrivedIntent.putExtra("voter", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_CANCEL_POLL:
			// notify the UI that new message has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.cancelVote);
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_COEFFICIENT_COMMITMENT:
			// notify the UI that a coefficient commitment has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.coefficientCommitment);
			messageArrivedIntent.putExtra("coefficientCommitments", voteMessage.getMessageContent());
			messageArrivedIntent.putExtra("sender", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		case VOTE_MESSAGE_KEY_SHARE:
			// notify the UI that a coefficient commitment has arrived
			messageArrivedIntent = new Intent(BroadcastIntentTypes.keyShare);
			messageArrivedIntent.putExtra("keyShare", voteMessage.getMessageContent());
			messageArrivedIntent.putExtra("sender", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
			
		case VOTE_MESSAGE_PART_DECRYPTION:
			messageArrivedIntent = new Intent(BroadcastIntentTypes.partDecryption);
			messageArrivedIntent.putExtra("partDecryption", voteMessage.getMessageContent());
			messageArrivedIntent.putExtra("sender", voteMessage.getSenderUniqueId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(messageArrivedIntent);
			break;
		default:
			break;
		}
	}



}
