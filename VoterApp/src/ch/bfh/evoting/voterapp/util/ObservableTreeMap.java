package ch.bfh.evoting.voterapp.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.TreeMap;

public class ObservableTreeMap<K, V> extends TreeMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PROP_PUT = "put";
	private PropertyChangeSupport propertySupport;

	public ObservableTreeMap() {
		super();
		propertySupport = new PropertyChangeSupport(this);
	}

	@Override
	public V put(K k, V v) {
		V old = super.put(k, v);
		propertySupport.firePropertyChange(PROP_PUT, old, v);
		return old;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		super.putAll(map);
		propertySupport.firePropertyChange(PROP_PUT, null, null);
	}

	@Override
	public void clear() {
		super.clear();
		propertySupport.firePropertyChange(PROP_PUT, null, null);
	}

	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		propertySupport.firePropertyChange(PROP_PUT, null, value);
		return value;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

}
