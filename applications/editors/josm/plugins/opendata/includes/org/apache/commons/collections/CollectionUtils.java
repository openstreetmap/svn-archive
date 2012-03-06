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
package org.apache.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides utility methods and decorators for {@link Collection} instances.
 *
 * @since Commons Collections 1.0
 * @version $Revision: 646777 $ $Date: 2008-04-10 14:33:15 +0200 (jeu., 10 avr. 2008) $
 * 
 * @author Rodney Waldhoff
 * @author Paul Jack
 * @author Stephen Colebourne
 * @author Steve Downey
 * @author Herve Quiroz
 * @author Peter KoBek
 * @author Matthew Hawthorne
 * @author Janek Bogucki
 * @author Phil Steitz
 * @author Steven Melzer
 * @author Jon Schewe
 * @author Neil O'Toole
 * @author Stephen Smith
 */
public class CollectionUtils {


    /**
     * <code>CollectionUtils</code> should not normally be instantiated.
     */
    public CollectionUtils() {
    }


    /** 
     * Transform the collection by applying a Transformer to each element.
     * <p>
     * If the input collection or transformer is null, there is no change made.
     * <p>
     * This routine is best for Lists, for which set() is used to do the 
     * transformations "in place."  For other Collections, clear() and addAll()
     * are used to replace elements.  
     * <p>
     * If the input collection controls its input, such as a Set, and the
     * Transformer creates duplicates (or are otherwise invalid), the 
     * collection may reduce in size due to calling this method.
     * 
     * @param collection  the collection to get the input from, may be null
     * @param transformer  the transformer to perform, may be null
     */
    public static void transform(Collection collection, Transformer transformer) {
        if (collection != null && transformer != null) {
            if (collection instanceof List) {
                List list = (List) collection;
                for (ListIterator it = list.listIterator(); it.hasNext();) {
                    it.set(transformer.transform(it.next()));
                }
            } else {
                Collection resultCollection = collect(collection, transformer);
                collection.clear();
                collection.addAll(resultCollection);
            }
        }
    }
        
    /** 
     * Returns a new Collection consisting of the elements of inputCollection transformed
     * by the given transformer.
     * <p>
     * If the input transformer is null, the result is an empty list.
     * 
     * @param inputCollection  the collection to get the input from, may not be null
     * @param transformer  the transformer to use, may be null
     * @return the transformed result (new list)
     * @throws NullPointerException if the input collection is null
     */
    public static Collection collect(Collection inputCollection, Transformer transformer) {
        ArrayList answer = new ArrayList(inputCollection.size());
        collect(inputCollection, transformer, answer);
        return answer;
    }
    
    /** 
     * Transforms all elements from inputCollection with the given transformer 
     * and adds them to the outputCollection.
     * <p>
     * If the input collection or transformer is null, there is no change to the 
     * output collection.
     *
     * @param inputCollection  the collection to get the input from, may be null
     * @param transformer  the transformer to use, may be null
     * @param outputCollection  the collection to output into, may not be null
     * @return the outputCollection with the transformed input added
     * @throws NullPointerException if the output collection is null
     */
    public static Collection collect(Collection inputCollection, final Transformer transformer, final Collection outputCollection) {
        if (inputCollection != null) {
            return collect(inputCollection.iterator(), transformer, outputCollection);
        }
        return outputCollection;
    }

    /** 
     * Transforms all elements from the inputIterator with the given transformer 
     * and adds them to the outputCollection.
     * <p>
     * If the input iterator or transformer is null, there is no change to the 
     * output collection.
     *
     * @param inputIterator  the iterator to get the input from, may be null
     * @param transformer  the transformer to use, may be null
     * @param outputCollection  the collection to output into, may not be null
     * @return the outputCollection with the transformed input added
     * @throws NullPointerException if the output collection is null
     */
    public static Collection collect(Iterator inputIterator, final Transformer transformer, final Collection outputCollection) {
        if (inputIterator != null && transformer != null) {
            while (inputIterator.hasNext()) {
                Object item = inputIterator.next();
                Object value = transformer.transform(item);
                outputCollection.add(value);
            }
        }
        return outputCollection;
    }
}
