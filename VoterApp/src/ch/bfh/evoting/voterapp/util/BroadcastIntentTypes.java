package ch.bfh.evoting.voterapp.util;

import java.io.Serializable;

/**
 * Class listing local broadcast intent types that are sent in the application
 * @author Philémon von Bergen
 *
 */
public class BroadcastIntentTypes {
	/**
	 * Intent type sent when a participant has changed his state i.e. has joined the network
	 * Extras:
	 * - nothing
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
	 * Intent type sent when the admin sends the start signal for the voting phase
	 * Extras:
	 * - nothing
	 */
	public static final String startVote = "startVote";
	/**
	 * Intent type sent when the admin sends the stop signal for the voting phase
	 * Extras:
	 * - nothing
	 */
	public static final String stopVote = "stopVote";
	/**
	 * Intent type sent when a new vote is received
	 * Extras:
	 * - "vote": the vote
	 * - "voter": ip address of voter
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
}
