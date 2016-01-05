/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.util;

import static java.util.Arrays.asList;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

/**
 * Une MultiMap.
 * 
 * @author ILM Informatique 8 sept. 2004
 * @param <K> type of the keys
 * @param <V> type of elements in collections
 */
@SuppressWarnings({ "unchecked", "serial" })
public class CollectionMap<K, V> extends HashSetValuedHashMap<K,V> {

    /**
     * Une nouvelle map
     */
    public CollectionMap() {
    }

    /**
     * Une nouvelle map
     * 
     * @param initialCapacity the initial capacity.
     */
    public CollectionMap(final int initialCapacity) {
        super(initialCapacity);
    } 

    public boolean putAll(K key, V... values) {
        return this.putAll(key, asList(values));
    }
}