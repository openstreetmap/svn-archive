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

import java.util.Arrays;
import java.util.List;

/**
 * A simple class to hold 2 values in a type-safe manner.
 * 
 * @author Sylvain
 * 
 * @param <A> type of first value.
 * @param <B> type of second value.
 */
public class Tuple2<A, B> {

    // just to make the code shorter
    public static final <A, B> Tuple2<A, B> create(A a, B b) {
        return new Tuple2<A, B>(a, b);
    }

    private final A a;
    private final B b;

    public Tuple2(A a, B b) {
        super();
        this.a = a;
        this.b = b;
    }

    public final A get0() {
        return this.a;
    }

    public final B get1() {
        return this.b;
    }

    public List<Object> asList() {
        return Arrays.asList(get0(), get1());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple2) {
            final Tuple2<?, ?> o = (Tuple2<?, ?>) obj;
            return this.asList().equals(o.asList());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return this.asList().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + this.asList();
    }
}
