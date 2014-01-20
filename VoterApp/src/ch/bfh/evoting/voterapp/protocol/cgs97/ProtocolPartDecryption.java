package ch.bfh.evoting.voterapp.protocol.cgs97;

import java.io.Serializable;
import java.util.List;

import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class ProtocolPartDecryption implements Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final List<GStarModElement> partDecryptions;
	private final List<Triple> proofs;
	private final long timestamp;
	
	
	public ProtocolPartDecryption(List<GStarModElement> partDecryptions, List<Triple> proofs) {
		this.partDecryptions = partDecryptions;
		this.proofs = proofs;
		this.timestamp = System.currentTimeMillis();
	}
	
	public List<GStarModElement> getPartDecryptions() {
		return partDecryptions;
	}


	public List<Triple> getProofs() {
		return proofs;
	}


	public long getTimestamp() {
		return timestamp;
	}
	
}
