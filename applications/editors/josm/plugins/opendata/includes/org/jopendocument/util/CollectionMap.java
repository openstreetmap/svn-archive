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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Une MultiMap qui permet de ne pas renvoyer <code>null</code>. De plus elle permet de choisir le
 * type de Collection utilisé.
 * 
 * @author ILM Informatique 8 sept. 2004
 * @param <K> type of the keys
 * @param <V> type of elements in collections
 */
@SuppressWarnings({ "unchecked", "serial" })
public class CollectionMap<K, V> extends MultiHashMap {

    private static final int DEFAULT_CAPACITY = 16;

    private final Class<? extends Collection<V>> collectionClass;
    private final Collection<V> collectionSpecimen;

    /**
     * Une nouvelle map avec ArrayList comme collection.
     */
    public CollectionMap() {
        this(ArrayList.class);
    }

    /**
     * Une nouvelle map. <code>collectionClass</code> doit descendre de Collection, et posséder un
     * constructeur prenant une Collection (c'est le cas de la majorité des classes de java.util).
     * 
     * @param aCollectionClass le type de collection utilisé.
     */
    public CollectionMap(Class aCollectionClass) {
        this(aCollectionClass, DEFAULT_CAPACITY);
    }

    /**
     * Une nouvelle map sans préciser le type de collection. Dans ce cas si vous voulez spécifier
     * une collection surchargez {@link #createCollection(Collection)}. Ce constructeur est donc
     * utile pour des raisons de performances (évite la réflexion nécessaire avec les autres).
     * 
     * @param initialCapacity the initial capacity.
     */
    public CollectionMap(final int initialCapacity) {
        this((Class) null, initialCapacity);
    }

    public CollectionMap(Class aCollectionClass, final int initialCapacity) {
        super(initialCapacity);
        this.collectionClass = aCollectionClass;
        this.collectionSpecimen = null;
        new MultiValueMap(); // TODO: use this class instead of deprecated MultiHashMap
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.MultiHashMap#createCollection(java.util.Collection)
     */
    public Collection<V> createCollection(Collection coll) {
        if (this.collectionClass != null)
            try {
                if (coll == null) {
                    return this.collectionClass.newInstance();
                } else {
                    return this.collectionClass.getConstructor(new Class[] { Collection.class }).newInstance(new Object[] { coll });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else if (this.collectionSpecimen != null) {
            try {
                final Collection<V> res = CopyUtils.copy(this.collectionSpecimen);
                if (coll != null)
                    res.addAll(coll);
                return res;
            } catch (Exception e) {
                throw ExceptionUtils.createExn(IllegalStateException.class, "clone() failed", e);
            }
        } else
            return super.createCollection(coll);
    }

    /**
     * Fusionne la MultiMap avec celle-ci. C'est à dire rajoute les valeurs de mm à la suite des
     * valeurs de cette map (contrairement à putAll(Map) qui ajoute les valeurs de mm en tant que
     * valeur scalaire et non en tant que collection).
     * 
     * @param mm la MultiMap à fusionner.
     */
    public void merge(MultiMap mm) {
        // copied from super ctor
        for (Iterator it = mm.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Map.Entry) it.next();
            Collection<V> coll = (Collection<V>) entry.getValue();
            Collection newColl = createCollection(coll);
            this.putAll(entry.getKey(), newColl);
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map. This method is equivalent to
     * {@link MultiHashMap#MultiHashMap(Map)}. NOTE: cannot use Map<? extends K, ? extends V> since
     * java complains (MultiHashMap not being generic).
     * 
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(Map mapToCopy) {
        if (mapToCopy instanceof MultiMap) {
            this.merge((MultiMap) mapToCopy);
        } else {
            super.putAll(mapToCopy);
        }
    }

    public boolean putAll(K key, V... values) {
        return this.putAll(key, asList(values));
    }

    // generics : MultiHashMap is not generic but it extends HashMap who does
    // so just override

    @Override
    public Set<Map.Entry<K, Collection<V>>> entrySet() {
        return super.entrySet();
    }

    @Override
    public Set<K> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<V> values() {
        return super.values();
    }
}