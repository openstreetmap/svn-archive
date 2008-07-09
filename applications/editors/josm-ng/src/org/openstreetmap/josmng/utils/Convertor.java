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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple generic conversion framework.
 * It looks up all the convertors registered in services folder
 * and tries to chain them to obtain given type from the source type.
 */
public abstract class Convertor<F,T> {
    private final Class<F> from;
    private final Class<T> to;

    protected Convertor(Class<F> from, Class<T> to) {
        this.from = from;
        this.to = to;
    }
    
    private boolean canAccept(Object source) {
        return from.isAssignableFrom(source.getClass()) && accept((F)source);
    }
    
    public abstract boolean accept(F source);
    public abstract T convert(F from);
    
    private static List<Convertor<?,?>> convertors = new ArrayList<Convertor<?, ?>>();
    static {
        for (Convertor<?,?> conv : ServiceLoader.load(Convertor.class)) convertors.add(conv);
    }
    
    public static <F,T> T convert(F from, Class<T> to) {
        return convert(from, to, new HashSet<Convertor>());
    }
    
    private static <F,T> T convert(F from, Class<T> to, Set<Convertor> used) {
        if (to.isAssignableFrom(from.getClass())) return (T)from;
        for (Convertor c : convertors) {
            if (used.contains(c)) continue;
            if (c.canAccept(from)) {
                used.add(c);
                Object intermediate = c.convert(from);
                T ret = convert(intermediate, to, used);
                used.remove(c);
                if (ret != null) return ret;
            }
        }
        return null;
    }
}
