package ch.bfh.evoting.voterapp.protocol.hkrs12;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

public class ProtocolMessageContainer implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Element value;
	private Element proof;

	public ProtocolMessageContainer(Element value, Element proof){
		this.value = value;
		this.proof = proof;
	}

	public Element getValue() {
		return value;
	}

	public Element getProof() {
		return proof;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
	
	
}
