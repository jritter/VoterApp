package ch.bfh.evoting.voterapp.util;

import java.io.Serializable;
import java.util.Comparator;

public class IPAddressComparator implements Comparator<String>, Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(String ip1, String ip2) {
		if(ip1==null || ip2==null) return -1;
		String ip1String = ip1.replace(".", "");
		String ip2String = ip2.replace(".", "");
		try{
			long ip1Int = Long.parseLong(ip1String);
			long ip2Int = Long.parseLong(ip2String);
			return ((Long)ip1Int).compareTo(ip2Int);
		} catch (NumberFormatException e){
			return ip1String.compareTo(ip2String);
		}
	}

}
