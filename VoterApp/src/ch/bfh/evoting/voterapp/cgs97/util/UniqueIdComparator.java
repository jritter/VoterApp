package ch.bfh.evoting.voterapp.cgs97.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator used to compare the unique identifiers of participants
 * @author Philémon von Bergen
 *
 */
public class UniqueIdComparator implements Comparator<String>, Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(String id1, String id2) {
		if(id1==null || id2==null) return -1;
		
		try{
			//if strings containe ip addresses as unique id
			String id1String = id1.replace(".", "");
			String id2String = id2.replace(".", "");
			long ip1Int = Long.parseLong(id1String);
			long ip2Int = Long.parseLong(id2String);
			return ((Long)ip1Int).compareTo(ip2Int);
		} catch (NumberFormatException e){
			//else if unique id are simple strings
			return id1.compareTo(id2);
		}
	}

}
