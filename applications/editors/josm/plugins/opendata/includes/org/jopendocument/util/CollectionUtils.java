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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import org.jopendocument.util.cc.ITransformer;

/**
 * Une classe regroupant des méthodes utilitaires pour les collections.
 * 
 * @author ILM Informatique 30 sept. 2004
 */
public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {

    /**
     * Concatene une collection. Cette méthode va appliquer un transformation sur chaque élément
     * avant d'appeler toString(). join([-1, 3, 0], " ,", doubleTransformer) == "-2, 6, 0"
     * 
     * @param <E> type of items
     * @param c la collection a concaténer.
     * @param sep le séparateur entre chaque élément.
     * @param tf la transformation à appliquer à chaque élément.
     * @return la chaine composée de chacun des éléments séparés par <code>sep</code>.
     */
    static public final <E> String join(final Collection<E> c, final String sep, final ITransformer<? super E, ?> tf) {
        if (c.size() == 0)
            return "";

        final StringBuffer res = new StringBuffer(c.size() * 4);
        if (c instanceof RandomAccess && c instanceof List) {
            final List<E> list = (List<E>) c;
            final int stop = c.size() - 1;
            for (int i = 0; i < stop; i++) {
                res.append(tf.transformChecked(list.get(i)));
                res.append(sep);

            }
            res.append(tf.transformChecked(list.get(stop)));
        } else {
            final Iterator<E> iter = c.iterator();
            while (iter.hasNext()) {
                final E elem = iter.next();
                res.append(tf.transformChecked(elem));
                if (iter.hasNext())
                    res.append(sep);
            }
        }
        return res.toString();
    }

    /**
     * Concatene une collection en appelant simplement toString() sur chaque élément.
     * 
     * @param <T> type of collection
     * @param c la collection a concaténer.
     * @param sep le séparateur entre chaque élément.
     * @return la chaine composée de chacun des éléments séparés par <code>sep</code>.
     * @see #join(Collection, String, ITransformer)
     */
    static public <T> String join(Collection<T> c, String sep) {
        return join(c, sep, org.jopendocument.util.cc.Transformer.<T> nopTransformer());
    }
}