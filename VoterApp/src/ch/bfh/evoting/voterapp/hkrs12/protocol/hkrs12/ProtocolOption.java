package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12;

import ch.bfh.evoting.voterapp.hkrs12.entities.Option;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

public class ProtocolOption extends Option {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element representation;

	/**
	 * Create a ProtocolOption by giving an Option object
	 * @param op the option object to use in this ProtocolOption
	 */
	public ProtocolOption(Option op){
		super(op.getText(),op.getVotes(), op.getPercentage(), op.getId(), op.getPollId());
	}
	
	/**
	 * Get the cryptographic representation of the option
	 * @return the cryptographic representation of the option
	 */
	public Element getRepresentation() {
		return representation;
	}
	
	/**
	 * Set the cryptographic representation of the option
	 * @param representation the cryptographic representation of the option
	 */
	public void setRepresentation(Element representation) {
		this.representation = representation;
	}

}
