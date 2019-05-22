package org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate.helper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.SimpleIntegerProperty;

/**
 * Observable {@link ArrayList}
 * @author rebsc
 * @param <E> object type
 */
public class ObservableArrayList<E> extends ArrayList<E>{

    private SimpleIntegerProperty sizeProperty;
    protected PropertyChangeSupport propertyChangeSupport;

	public ObservableArrayList() {
		super();
		propertyChangeSupport = new PropertyChangeSupport(this);
	    sizeProperty = new SimpleIntegerProperty(0);
	}

	public ObservableArrayList(int initialCapacity) {
		super(initialCapacity);
		propertyChangeSupport = new PropertyChangeSupport(this);
	    sizeProperty = new SimpleIntegerProperty(0);
	}

	public ObservableArrayList(Collection<? extends E> c) {
		super(c);
		propertyChangeSupport = new PropertyChangeSupport(this);
	    sizeProperty = new SimpleIntegerProperty(0);
	}


	@Override
	public boolean add(E e) {
		boolean returnValue = super.add(e);
        sizeProperty.set(size());
        propertyChangeSupport.firePropertyChange(sizeProperty.toString(), false, true);
        return returnValue;
	}

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        sizeProperty.set(size());
        propertyChangeSupport.firePropertyChange(sizeProperty.toString(), false, true);
    }

    @Override
    public E remove(int index) {
        E returnValue = super.remove(index);
        sizeProperty.set(size());
        propertyChangeSupport.firePropertyChange(sizeProperty.toString(), false, true);
        return returnValue;
    }

    @Override
	public void clear() {
    	super.clear();
    	sizeProperty.set(size());
    	propertyChangeSupport.firePropertyChange(sizeProperty.toString(), false, true);
    }

    @Override
    public boolean remove(Object o) {
        boolean returnValue = super.remove(o);
        if(returnValue){
            sizeProperty.set(size());
            propertyChangeSupport.firePropertyChange(sizeProperty.toString(), false, true);
        }
        return returnValue;
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
