package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.xml;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;

import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.multiencryptionballot.ProtocolPartDecryption;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLEqualityProof;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLGqElement;
import ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml.XMLZqElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModElement;

public class XMLPartDecryption {

	@ElementList
	private List<XMLGqElement> partDecryptions;
	@ElementList
	private List<XMLEqualityProof> proofs;
	
	public XMLPartDecryption(List<XMLGqElement> partDecryptions,
			 List<XMLEqualityProof> proofs) {
		super();
		this.partDecryptions = partDecryptions;
		this.proofs = proofs;
	}
	
	public XMLPartDecryption(ProtocolPartDecryption partDecryption){
		
		partDecryptions = new ArrayList<XMLGqElement>();
		proofs = new ArrayList<XMLEqualityProof>();
		
		if (partDecryption != null){
		
			for (GStarModElement partDecryptionValue : partDecryption.getPartDecryptions()){
				partDecryptions.add(new XMLGqElement(partDecryptionValue.getValue().toString(10)));
			}
			
			for (Tuple equalityProof : partDecryption.getProofs()){
	
				XMLGqElement valueT1 = new XMLGqElement(
						((Tuple) equalityProof.getAt(0)).getAt(0)
								.getBigInteger().toString(10));
				XMLGqElement valueT2 = new XMLGqElement(
						((Tuple) equalityProof.getAt(0)).getAt(1)
								.getBigInteger().toString(10));
				XMLZqElement valueC = new XMLZqElement(equalityProof.getAt(1)
						.getBigInteger().toString(10));
				XMLZqElement valueS = new XMLZqElement(equalityProof.getAt(2)
						.getBigInteger().toString(10));
				proofs.add(new XMLEqualityProof(
						valueT1, valueT2, valueC, valueS));
			
			}
		}
	}


	public List<XMLGqElement> getPartDecryptions() {
		return partDecryptions;
	}


	public void setPartDecryptions(List<XMLGqElement> partDecryptions) {
		this.partDecryptions = partDecryptions;
	}


	public List<XMLEqualityProof> getProofs() {
		return proofs;
	}


	public void setProofs(List<XMLEqualityProof> proofs) {
		this.proofs = proofs;
	}

}
