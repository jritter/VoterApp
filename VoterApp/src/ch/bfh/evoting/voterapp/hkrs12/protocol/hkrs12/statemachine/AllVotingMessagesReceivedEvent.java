package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.statemachine;

import java.io.Serializable;

import com.continuent.tungsten.fsm.core.Event;

/**
 * State machnine event used for transition from state vote to state tally
 * @author Philémon von Bergen
 *
 */
public class AllVotingMessagesReceivedEvent extends Event implements Serializable{

	private static final long serialVersionUID = 1L;

	public AllVotingMessagesReceivedEvent(Object data) {
		super(data);
	}

}
