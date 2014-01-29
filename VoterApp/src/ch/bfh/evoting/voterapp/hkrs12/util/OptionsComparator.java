package ch.bfh.evoting.voterapp.hkrs12.util;

import java.io.Serializable;
import java.util.Comparator;

import ch.bfh.evoting.voterapp.hkrs12.entities.Option;

/**
 * Comparator used to sort the options in the descendant order of the number of votes they received
 * @author Philémon von Bergen
 *
 */
public class OptionsComparator implements Comparator<Option>, Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(Option op1, Option op2) {
		//return the inverse of what is specified in the doc to have
		//an inverted order (descending) when calling Collections.sort()
		if(op1.getVotes()>op2.getVotes()) return -1;
		else if (op1.getVotes()>op2.getVotes()) return 1;
		else return 0;
	}

}
