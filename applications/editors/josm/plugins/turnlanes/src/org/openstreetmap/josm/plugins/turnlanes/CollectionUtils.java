package org.openstreetmap.josm.plugins.turnlanes;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
}
