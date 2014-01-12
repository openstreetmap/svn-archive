/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.commons.collections.iterators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An IteratorChain is an Iterator that wraps a number of Iterators.
 * <p>
 * This class makes multiple iterators look like one to the caller
 * When any method from the Iterator interface is called, the IteratorChain
 * will delegate to a single underlying Iterator. The IteratorChain will
 * invoke the Iterators in sequence until all Iterators are exhausted.
 * <p>
 * Under many circumstances, linking Iterators together in this manner is
 * more efficient (and convenient) than reading out the contents of each
 * Iterator into a List and creating a new Iterator.
 * <p>
 * Calling a method that adds new Iterator<i>after a method in the Iterator
 * interface has been called</i> will result in an UnsupportedOperationException.
 * Subclasses should <i>take care</i> to not alter the underlying List of Iterators.
 * <p>
 * NOTE: As from version 3.0, the IteratorChain may contain no
 * iterators. In this case the class will function as an empty iterator.
 *
 * @since Commons Collections 2.1
 * @version $Revision: 647116 $ $Date: 2008-04-11 13:23:08 +0200 (ven., 11 avr. 2008) $
 *
 * @author Morgan Delagrange
 * @author Stephen Colebourne
 */
public class IteratorChain implements Iterator {

    /** The chain of iterators */
    protected final List iteratorChain = new ArrayList();
    /** The index of the current iterator */
    protected int currentIteratorIndex = 0;
    /** The current iterator */
    protected Iterator currentIterator = null;
    /**
     * The "last used" Iterator is the Iterator upon which
     * next() or hasNext() was most recently called
     * used for the remove() operation only
     */
    protected Iterator lastUsedIterator = null;
    /**
     * ComparatorChain is "locked" after the first time
     * compare(Object,Object) is called
     */
    protected boolean isLocked = false;

    //-----------------------------------------------------------------------
    /**
     * Construct an IteratorChain with no Iterators.
     * <p>
     * You will normally use {@link #addIterator(Iterator)} to add
     * some iterators after using this constructor.
     */
    public IteratorChain() {
        super();
    }
    
    //-----------------------------------------------------------------------
    /**
     * Add an Iterator to the end of the chain
     *
     * @param iterator Iterator to add
     * @throws IllegalStateException if I've already started iterating
     * @throws NullPointerException if the iterator is null
     */
    public void addIterator(Iterator iterator) {
        checkLocked();
        if (iterator == null) {
            throw new NullPointerException("Iterator must not be null");
        }
        iteratorChain.add(iterator);
    }

    /**
     * Checks whether the iterator chain is now locked and in use.
     */
    private void checkLocked() {
        if (isLocked == true) {
            throw new UnsupportedOperationException("IteratorChain cannot be changed after the first use of a method from the Iterator interface");
        }
    }

    /**
     * Lock the chain so no more iterators can be added.
     * This must be called from all Iterator interface methods.
     */
    private void lockChain() {
        if (isLocked == false) {
            isLocked = true;
        }
    }

    /**
     * Updates the current iterator field to ensure that the current Iterator
     * is not exhausted
     */
    protected void updateCurrentIterator() {
        if (currentIterator == null) {
            if (iteratorChain.isEmpty()) {
                currentIterator = EmptyIterator.INSTANCE;
            } else {
                currentIterator = (Iterator) iteratorChain.get(0);
            }
            // set last used iterator here, in case the user calls remove
            // before calling hasNext() or next() (although they shouldn't)
            lastUsedIterator = currentIterator;
        }

        while (currentIterator.hasNext() == false && currentIteratorIndex < iteratorChain.size() - 1) {
            currentIteratorIndex++;
            currentIterator = (Iterator) iteratorChain.get(currentIteratorIndex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Return true if any Iterator in the IteratorChain has a remaining element.
     *
     * @return true if elements remain
     */
    public boolean hasNext() {
        lockChain();
        updateCurrentIterator();
        lastUsedIterator = currentIterator;

        return currentIterator.hasNext();
    }

    /**
     * Returns the next Object of the current Iterator
     *
     * @return Object from the current Iterator
     * @throws java.util.NoSuchElementException if all the Iterators are exhausted
     */
    public Object next() {
        lockChain();
        updateCurrentIterator();
        lastUsedIterator = currentIterator;

        return currentIterator.next();
    }

    /**
     * Removes from the underlying collection the last element
     * returned by the Iterator.  As with next() and hasNext(),
     * this method calls remove() on the underlying Iterator.
     * Therefore, this method may throw an
     * UnsupportedOperationException if the underlying
     * Iterator does not support this method.
     *
     * @throws UnsupportedOperationException
     *   if the remove operator is not supported by the underlying Iterator
     * @throws IllegalStateException
     *   if the next method has not yet been called, or the remove method has
     *   already been called after the last call to the next method.
     */
    public void remove() {
        lockChain();
        if (currentIterator == null) {
            updateCurrentIterator();
        }
        lastUsedIterator.remove();
    }

}
