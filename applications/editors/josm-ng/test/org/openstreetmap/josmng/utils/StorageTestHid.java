/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.utils;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Basic testing infrastructure for Storage
 * 
 * @author nenik
 */
public abstract class StorageTestHid<T> {

    public StorageTestHid() {
        prepare();
    }
    
    Storage<T> storage;
    List<T> master;


    protected abstract List<T> getTestData();
    protected abstract T createEqualObject(T str);
    
    protected Hash<T,T> createHash() {
        return Storage.<T>defaultHash();
    }
    
    private void prepare() {
        master = getTestData();
        storage = new Storage<T>(createHash());

        // Fill the map
        for (T s : master) {
            assertNull(storage.put(s));
        }
    }
    
    
    // check presence of original instances
    public @Test void testGet() {
        for (T s : master) {
            assertSame(s, storage.get(s));
        }
    }

    // check get through equal key
    public @Test void testGetEquivalent() {
        for (T s : master) {
            assertSame(s, storage.get(createEqualObject(s)));
        }
    }
    
    public @Test void testPutUnique() {
        // check putIfUnique doesn't replace
        for (T s : master) {
            assertSame(s, storage.putUnique(createEqualObject(s)));
        }
        
    }

    public @Test void testPutReplace() {
        // check putIfUnique doesn't replace
        for (T s : master) {
            T replacement = createEqualObject(s);
            T orig = storage.put(replacement);

            // original instance returned
            assertSame(orig,s); 
            
            // original was removed from the storage ...
            assertNotSame(s, storage.get(s));

            // ... even when asking with different instance ...
            assertNotSame(s, storage.get(replacement));

            // ... and the new one is there
            assertSame(replacement, storage.get(s));
        }
    }

    private <T> Set<T> iterableToSet(Iterable<T> i) {
        Set<T> all = new HashSet<T>();
        for (T t : i) all.add(t);
        return all;
    }

    // slightly randomized
    public @Test void testRemoveElem() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        String failure = "fail for seed=" + seed;
        
        Set<T> survived = new HashSet<T>();
        for (T s : master) {
            if (random.nextBoolean()) {
                assertSame(failure, s, storage.removeElem(createEqualObject(s))); 
            } else {
                survived.add(s);
            }
        }

        Set<T> compare = iterableToSet(storage);
        assertEquals(failure, survived, compare);
    }
    
    public @Test void testIterate() {
        Set<T> all = iterableToSet(storage);
        Set<T> compare = new HashSet<T>(master);
        assertEquals(all, compare);
    }

    public @Test void testIterateWithoutHasNext() {
        Set<T> all = new HashSet<T>();
        Iterator<T> it = storage.iterator();
        for (int i=0; i<master.size(); i++) {
            all.add(it.next());
        }
        Set<T> compare = new HashSet<T>(master);
        assertEquals(all, compare);
        
        assertFalse(it.hasNext());
        
        try {
            it.next();
            fail("Expected an exception");
        } catch (NoSuchElementException nse) { /* expected */}
    }
    

    // slightly randomized
    public @Test void testIteratorRemove() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        String failure = "fail for seed=" + seed;
        
        Set<T> survived = new HashSet<T>();
        for (Iterator<T> it = storage.iterator(); it.hasNext();) {
            T s = it.next();
            if (random.nextBoolean()) {
                it.remove();
            } else {
                survived.add(s);
            }
        }

        Set<T> compare = iterableToSet(storage);
        assertEquals(failure, survived, compare);
    }
    
    
    public @Test void comodificationTest() {
        Iterator<T> iter = storage.iterator();

        iter.hasNext(); // must survive
        
        storage.remove(iter.next());

        try {
            iter.hasNext();
            fail("Expected an exception");
        } catch (ConcurrentModificationException cme) { /* expected */}

        try {
            iter.next();
            fail("Expected an exception");
        } catch (ConcurrentModificationException cme) { /* expected */}
    }
    
    public @Test void iteratorEarlyRemoveFailsTest() {
        Iterator<T> iter = storage.iterator();
        
        try {
            iter.remove();
            fail("Expected an exception on early remove");
        } catch (IllegalStateException ise) { /* expected */}

        iter.next();
        iter.remove();
        try {
            iter.remove();
            fail("Expected an exception on double remove");
        } catch (IllegalStateException ise) { /* expected */}
    }
    
    private Storage<T> getClonedStorage(Storage s, boolean reversed) {
        List<T> temp = new LinkedList<T>(storage);
        for (T t : storage) temp.add(0, createEqualObject(t));
        if (reversed) Collections.reverse(temp);
        
        Storage<T> copy = new Storage(createHash());
        copy.addAll(temp);
        return copy;
    }
    
    public @Test void storageEqualsWithStorage() {
        Storage<T> clone = getClonedStorage(storage, true);
        assertTrue(storage.equals(clone));
    }
    
    public @Test void storageKeepsHashCode() {
        int hash = storage.hashCode();
        int cmp = getClonedStorage(storage, true).hashCode();
        assertEquals(hash, cmp);
    }
    
}
