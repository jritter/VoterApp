package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12.statemachine;

import java.io.Serializable;

import com.continuent.tungsten.fsm.core.Event;

/**
 * State machnine event used for transition from state tally to state exit
 * @author Philémon von Bergen
 *
 */
public class ResultComputedEvent extends Event implements Serializable{

	private static final long serialVersionUID = 1L;

	public ResultComputedEvent(Object data) {
		super(data);
	}
}
