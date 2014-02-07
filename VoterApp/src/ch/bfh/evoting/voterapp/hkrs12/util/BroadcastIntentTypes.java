package ch.bfh.evoting.voterapp.hkrs12.util;

import android.content.Intent;

/**
 * Class listing local broadcast intent types that are sent in the application
 * @author Philémon von Bergen
 *
 */
public class BroadcastIntentTypes {
	/**
	 * Intent type sent when a participant has changed his state i.e. has joined or left the network
	 * Extras:
	 * - action: left when participant left, added when participant added
	 * - id: id of participant
	 */
	public static final String participantStateUpdate = "participantStateUpdate";
	/**
	 * Intent type sent when the admin sends the participants present in the network
	 * Extras:
	 * - "participants": List<Participant> list of Participant objects present in the network
	 */
	public static final String electorate = "electorate";
	/**
	 * Intent type sent when a participant sends his acknowledgement for the review
	 * Extras:
	 * - "participant": ip of participant that has accepted the review
	 */
	public static final String acceptReview = "acceptReview";
	/**
	 * Intent type sent when the admin sends the participants present in the network
	 * Extras:
	 * - "poll": Poll object containing all data of the poll
	 * - "sender": String of the identification of the sender of the poll to review
	 */
	public static final String pollToReview = "pollToReview";
	/**
	 * Intent type sent when the admin sends the start signal for the voting period
	 * Extras:
	 * - nothing
	 */
	public static final String startVote = "startVote";
	/**
	 * Intent type sent when the admin sends the stop signal for the voting period
	 * Extras:
	 * - nothing
	 */
	public static final String stopVote = "stopVote";
	
	/**
	 * Intent type sent when the admin sends the cancel signal for the voting period
	 * Extras:
	 * - nothing
	 */
	public static String cancelVote = "cancelVote";

	/**
	 * Intent type sent when a new vote is received
	 * Extras:
	 * - "message": the vote
	 * - "sender": unique id of voter
	 */
	public static final String newVote = "newVote";
	
	/**
	 * Intent type sent when a the network group was destroyed
	 * Extras:
	 * - nothing
	 */
	public static final String networkGroupDestroyedEvent = "networkGroupDestroyedEvent";
	
	/**
	 * Intent type sent when connection to network was successful
	 * Extras:
	 * - nothing
	 */
	public static final String networkConnectionSuccessful = "NetworkServiceStarted";

	/**
	 * Intent type sent when connection to network failed
	 * Extras:
	 * - "error": integer, 1 = Invalid group name, 2 = Group name already exists (creation), 3 = Group not found (join)
	 */
	public static final String networkConnectionFailed = "networkConnectionFailed";
	
	/**
	 * Intent type sent when a new vote is received (used for simulation, when no protocol implemented)
	 * Extras:
	 * -"votes" number of votes already received
	 * -"options" list of options in the poll
	 * -"participants" list of participants of the poll
	 */
	public static final String newIncomingVote = "UpdateNewVotes";
	
	/**
	 * Intent type sent when an attack has been detected
	 * Extras:
	 * -"type" type of the attack
	 * 		1: impersonalization on the network: someone has send another public key for a peer that had already authenticated itself by sending its key
	 * 		2: message sent from someone on behalf of another peer
	 */
	public static final String attackDetected = "attackDetected";
	
	/**
	 * Intent type sent when wifi connected to a new network
	 * Extras:
	 * - nothing
	 * */
	public static final String networkSSIDUpdate = "networkSSIDUpdate";
	
	/**
	 * Intent type sent when NFC Tag is tapped to the back of a device
	 * Extras:
	 * - The NFC Tag
	 * */
	public static final String nfcTagTapped = "NFCTagTapped";
	
	/**
	 * Intent type sent when 5 decryptions failed and 0 successed
	 * This means that we are probably using a wrong decryption key (wrong group password or wrong salt)
	 * Extras:
	 * - "type" type of error
	 * 		password: 	the error is contained in the password
	 * 		salt:		the salt received did not correspond to the hash contained in the password
	 * */
	public static final String probablyWrongDecryptionKeyUsed = "probablyWrongDecryptionKeyUsed";
	
	/**
	 * Intent type sent when asking to show next activity
	 * Extras:
	 * - "poll" poll to use in GUI part (not always set)
	 * - "sender" sender of the poll (=administrator) (not always set)
	 */
	public static final String showNextActivity = "showNextActivity";
	
	/**
	 * Intent type sent when asking to show the Result activity
	 * Extras:
	 * - "poll" poll to use in GUI part (not always set)
	 */
	public static final String showResultActivity = "showResultActivity";
	
	/**
	 * Intent type sent when a commitment message is received
	 * Extras:
	 * - "message": the message
	 * - "sender": unique id of the sender
	 */
	public static String commitMessage = "commitMessage";
	
	/**
	 * Intent type sent when a recovery message is received
	 * Extras:
	 * - "message": the message
	 * - "sender": unique id of the sender
	 */
	public static String recoveryMessage = "recoveryMessage";
	
	/**
	 * Intent type sent when a setup message is received
	 * Extras:
	 * - "message": the message
	 * - "sender": unique id of the sender
	 */
	public static String setupMessage = "setupMessage";
		
	/**
	 * Intent type sent when user has chosen his vote
	 * Extras:
	 * - "option" the chosen option
	 * - "index" the index of the option in the list of options
	 */
	public static final String vote = "vote";
	
	/**
	 * Intent type sent when asking GUI to show a wait dialog
	 * Extras:
	 * - nothing
	 */
	public static final String showWaitDialog = "showWaitDialog";
	
	/**
	 * Intent type sent when asking GUI to dismiss the wait dialog
	 * Extras:
	 * - nothing
	 */
	public static final String dismissWaitDialog = "dismissWaitDialog";
	
	/**
	 * Intent type sent when detecting that different protocols are used on the network
	 * Extras:
	 * - nothing
	 */
	public static final String differentProtocols = "differentProtocols";
	
	/**
	 * Intent type sent when detecting that the poll used by the administrator is not the same as the one used here
	 * Extras:
	 * - nothing
	 */
	public static final String differentPolls = "differentPolls";

	/**
	 * Intent type sent when computation of the final result failed
	 * Extras:
	 * - nothing
	 */
	public static final String resultNotFound = "resultNotFound";
	
	/**
	 * Intent type sent when the verification of a proof failed
	 * Extras:
	 * - participant: well-known name of the participant
	 */
	public static String proofVerificationFailed = "proofVerificationFailed";

}
