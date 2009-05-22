package org.openstreetmap.josm.plugins.czechaddress;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ArrayList, which refuses to add {@code null}.
 * 
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class NotNullList<E> extends ArrayList<E> {

    public NotNullList() {
        super();
    }

    public NotNullList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * If {@code e} is {@code null}, nothing is done. Otherwise like
     * {@code ArrayList}.
     */
    @Override
    public boolean add(E e) {
        if (e != null)
            return super.add(e);
        else
            return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {

        if (c == null)
            return false;
        
        if (c instanceof NotNullList)
            return super.addAll(c);

        for (E e : c) add(e);
        return true;
    }
    
}
