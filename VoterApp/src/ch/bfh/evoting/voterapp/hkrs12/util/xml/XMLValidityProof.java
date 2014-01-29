package ch.bfh.evoting.voterapp.hkrs12.util.xml;

import java.util.List;

import org.simpleframework.xml.ElementList;

//Validity proof
//1	ProductGroupTuple[
//	           1.1 ProductGroupTuple[
//	                  1.1.1 ProductGroupTuple[1.1.1.1 GStarModElement[], 1.1.1.2 GStarModElement[]],
//	                  1.1.2 ProductGroupTuple[1.1.2.1 GStarModElement[], 1.1.2.2 GStarModElement[]]],
//	           1.2 ProductGroupTuple[1.2.1 ZModElement[], 1.2.2 ZModElement[]],
//	           1.3 ProductGroupTuple[1.3.1 ZModElement[], 1.3.2 ZModElement[]]]

public class XMLValidityProof {

	@ElementList
	private List<XMLGqPair> value11;
	@ElementList
	private List<XMLZqElement> value12;
	@ElementList
	private List<XMLZqElement> value13;

	
	public XMLValidityProof(){}


	public XMLValidityProof(List<XMLGqPair> value11,
			List<XMLZqElement> value12, List<XMLZqElement> value13) {
		super();
		this.value11 = value11;
		this.value12 = value12;
		this.value13 = value13;
	}


	public List<XMLGqPair> getValue11() {
		return value11;
	}


	public void setValue11(List<XMLGqPair> value11) {
		this.value11 = value11;
	}


	public List<XMLZqElement> getValue12() {
		return value12;
	}


	public void setValue12(List<XMLZqElement> value12) {
		this.value12 = value12;
	}


	public List<XMLZqElement> getValue13() {
		return value13;
	}


	public void setValue13(List<XMLZqElement> value13) {
		this.value13 = value13;
	}

	
	


}
