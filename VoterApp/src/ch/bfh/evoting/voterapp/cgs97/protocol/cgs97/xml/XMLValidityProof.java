package ch.bfh.evoting.voterapp.cgs97.protocol.cgs97.xml;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;

import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;

//Validity proof
//1        ProductGroupTuple[
//                   1.1 ProductGroupTuple[
//                          1.1.1 ProductGroupTuple[1.1.1.1 GStarModElement[], 1.1.1.2 GStarModElement[]],
//                          1.1.2 ProductGroupTuple[1.1.2.1 GStarModElement[], 1.1.2.2 GStarModElement[]]],
//                   1.2 ProductGroupTuple[1.2.1 ZModElement[], 1.2.2 ZModElement[]],
//                   1.3 ProductGroupTuple[1.3.1 ZModElement[], 1.3.2 ZModElement[]]]

public class XMLValidityProof {

	@ElementList
	private List<XMLGqPair> valueT;
	@ElementList
	private List<XMLZqElement> valueC;
	@ElementList
	private List<XMLZqElement> valueS;

	public XMLValidityProof() {
	}

	public XMLValidityProof(List<XMLGqPair> value11,
			List<XMLZqElement> value12, List<XMLZqElement> value13) {
		super();
		this.valueT = value11;
		this.valueC = value12;
		this.valueS = value13;
	}

	public XMLValidityProof(Tuple validityProof) {

		Tuple subPartT = (Tuple) validityProof.getAt(0); // list of Gq
															// pairs
		Tuple subPartC = (Tuple) validityProof.getAt(1); // list
		// ZModElements
		Tuple subPartS = (Tuple) validityProof.getAt(2); // list
		// ZModElements
		valueT = new ArrayList<XMLGqPair>();
		for (Element e : subPartT.getAll()) {
			Tuple tuple = (Tuple) e;
			XMLGqPair pair = new XMLGqPair(new XMLGqElement(((BigInteger) tuple
					.getAt(0).getValue()).toString(10)), new XMLGqElement(
					((BigInteger) tuple.getAt(1).getValue()).toString(10)));
			valueT.add(pair);
		}
		valueC = new ArrayList<XMLZqElement>();
		for (Element e : subPartC.getAll()) {
			valueC.add(new XMLZqElement(((BigInteger) e.getValue())
					.toString(10)));
		}
		valueS = new ArrayList<XMLZqElement>();
		for (Element e : subPartS.getAll()) {
			valueS.add(new XMLZqElement(((BigInteger) e.getValue())
					.toString(10)));
		}

	}

	public List<XMLGqPair> getValue11() {
		return valueT;
	}

	public void setValue11(List<XMLGqPair> value11) {
		this.valueT = value11;
	}

	public List<XMLZqElement> getValue12() {
		return valueC;
	}

	public void setValue12(List<XMLZqElement> value12) {
		this.valueC = value12;
	}

	public List<XMLZqElement> getValue13() {
		return valueS;
	}

	public void setValue13(List<XMLZqElement> value13) {
		this.valueS = value13;
	}

}