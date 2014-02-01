package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97;

import java.io.Serializable;

import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolPartDecryption implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private final GStarModElement partDecryption;
	private final Triple proof;
	private final long timestamp;
	
	
	public ProtocolPartDecryption(GStarModElement partDecryption, Triple proof) {
		this.partDecryption = partDecryption;
		this.proof = proof;
		this.timestamp = System.currentTimeMillis();
	}


	public GStarModElement getPartDecryption() {
		return partDecryption;
	}


	public Triple getProof() {
		return proof;
	}


	public long getTimestamp() {
		return timestamp;
	}

	
}
