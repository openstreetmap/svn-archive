package org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate.helper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Observable {@link ArrayList}
 * @author rebsc
 * @param <E> object type
 */
public class ObservableArrayList<E> extends ArrayList<E>{

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public ObservableArrayList() {
		super();
	}

	public ObservableArrayList(int cap) {
		super(cap);
	}

	public ObservableArrayList(Collection<? extends E> c) {
		super(c);
	}


	@Override
	public boolean add(E e) {
		int oldSize = super.size();
		if(super.add(e)) {
			changes.firePropertyChange("size", oldSize, super.size());
			return true;
		};
		return false;
	}


    @Override
	public void add(int index, E element) {
    	int oldSize = super.size();
    	super.add(index, element);
    	changes.firePropertyChange("size", oldSize, super.size());
    }

    @Override
	public boolean addAll(Collection<? extends E> c) {
    	int oldSize = super.size();
		if(super.addAll(c)) {
			changes.firePropertyChange("size", oldSize, super.size());
			return true;
		};
		return false;
    }

    @Override
	public E remove(int index) {
    	int oldSize = super.size();
        E removed = super.remove(index);
    	changes.firePropertyChange("size", oldSize, super.size());
    	return removed;
    }

    @Override
	public boolean remove(Object o) {
    	int oldSize = super.size();
    	if(super.remove(o)) {
    		changes.firePropertyChange("size", oldSize, super.size());
    		return true;
    	}
    	return false;
    }

	@Override
	public void clear() {
		int oldSize = super.size();
    	super.clear();
    	changes.firePropertyChange("size", oldSize, super.size());
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

	public void removeAllListener() {
		PropertyChangeListener[] listener = changes.getPropertyChangeListeners();
		int size = listener.length;
		for(int i=0; i<size; i++)	changes.removePropertyChangeListener(listener[i]);
	}

}
