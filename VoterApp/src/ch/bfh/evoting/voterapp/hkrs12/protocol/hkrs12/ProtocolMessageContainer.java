package ch.bfh.evoting.voterapp.hkrs12.protocol.hkrs12;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

/**
 * Class representing a message send or received inside the protocol
 * @author Philémon von Bergen
 *
 */
public class ProtocolMessageContainer implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element value;
	private Element proof;
	private Element complementaryValue;

	/**
	 * Constructs a message
	 * @param value value to be sent
	 * @param proof proof corresponding to the value sent
	 */
	public ProtocolMessageContainer(Element value, Element proof){
		this.value = value;
		this.proof = proof;
	}
	
	/**
	 * Constructs a message
	 * @param value value to be sent
	 * @param proof proof corresponding to the value sent
	 * @param complementaryValue additional value needed to verify the proof
	 */
	public ProtocolMessageContainer(Element value, Element proof, Element complementaryValue){
		this.value = value;
		this.proof = proof;
		this.complementaryValue = complementaryValue;
	}

	/**
	 * Get the value stored in the message
	 * @return the value stored in the message
	 */
	public Element getValue() {
		return value;
	}

	/**
	 * Get the proof stored in the message
	 * @return the proof stored in the message
	 */
	public Element getProof() {
		return proof;
	}
	
	/**
	 * Get the additional value stored in the message
	 * @return the addtitional value stored in the message
	 */
	public Element getComplementaryValue() {
		return complementaryValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((complementaryValue == null) ? 0 : complementaryValue.getValue()
						.hashCode());
		result = prime * result + ((proof == null) ? 0 : proof.getValue().hashCode());
		result = prime * result + ((value == null) ? 0 : value.getValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProtocolMessageContainer other = (ProtocolMessageContainer) obj;
		if (complementaryValue == null) {
			if (other.complementaryValue != null)
				return false;
		} else if (!complementaryValue.getValue().equals(other.complementaryValue.getValue()))
			return false;
		if (proof == null) {
			if (other.proof != null)
				return false;
		} else if (!proof.getValue().equals(other.proof.getValue()))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.getValue().equals(other.value.getValue()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProtocolMessageContainer [value=" + value + ", proof=" + proof
				+ ", complementaryValue=" + complementaryValue + "]";
	}
	
}
