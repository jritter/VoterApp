package ch.bfh.evoting.voterapp.protocol.cgs97;

import ch.bfh.evoting.voterapp.entities.Option;

public class ProtocolOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProtocolOption(Option op){
		super(op.getText(),op.getVotes(), op.getPercentage(), op.getId(), op.getPollId());
	}
}
