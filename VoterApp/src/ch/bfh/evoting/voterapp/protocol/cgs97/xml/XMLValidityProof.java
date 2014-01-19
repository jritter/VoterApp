package ch.bfh.evoting.voterapp.protocol.cgs97.xml;

import java.util.List;

import org.simpleframework.xml.ElementList;

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