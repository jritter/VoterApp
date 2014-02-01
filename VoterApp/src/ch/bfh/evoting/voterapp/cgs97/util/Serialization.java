package ch.bfh.evoting.voterapp.cgs97.util;

/**
 * Serialization interface for strategy pattern
 * @author Philémon von Bergen
 *
 */
public interface Serialization {
	
	/**
	 * Serialize the given object
	 * @param o the object to serialize
	 * @return the string resulting from the serialization
	 */
	public String serialize(Object o);
	
	/**
	 * Deserialize the given string
	 * @param s the string to deserialize
	 * @return the deserialized object
	 */
	public Object deserialize(String s);

}
