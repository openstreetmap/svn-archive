package org.openstreetmap.josm.plugins.turnlanes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class CollectionUtils {
    public static <E> Iterable<E> reverse(final List<E> list) {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                final ListIterator<E> it = list.listIterator(list.size());
                
                return new Iterator<E>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasPrevious();
                    }
                    
                    @Override
                    public E next() {
                        return it.previous();
                    }
                    
                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }
    
    public static <E> Set<E> toSet(Iterable<? extends E> iterable) {
        final Set<E> set = new HashSet<E>();
        
        for (E e : iterable) {
            set.add(e);
        }
        
        return Collections.unmodifiableSet(set);
    }
    
    public static <E> List<E> toList(Iterable<? extends E> iterable) {
        final List<E> list = new ArrayList<E>();
        
        for (E e : iterable) {
            list.add(e);
        }
        
        return Collections.unmodifiableList(list);
    }
}
