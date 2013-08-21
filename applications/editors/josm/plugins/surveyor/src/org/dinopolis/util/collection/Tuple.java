/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package org.dinopolis.util.collection;

import java.util.Map;

/**
 * Simple implementation of a tuple (two objects).
 *
 * @author cdaller
 * @deprecated Should be replaced by {@link org.openstreetmap.josm.tools.Pair}
 */
public class Tuple<T1 extends Object, T2 extends Object> implements Map.Entry<T1, T2>{
    T1 first;
    T2 second;

    /**
     * Constructor filling the values.
     * @param first the first value.
     * @param second the second value.
     */
    public Tuple(T1 one, T2 two) {
        this.first = one;
        this.second = two;
    }

    /**
     * @return the first
     */
    public T1 getFirst() {
        return this.first;
    }

    /**
     * @param first the first to set
     */
    public void setFirst(T1 first) {
        this.first = first;
    }

    /**
     * @return the second
     */
    public T2 getSecond() {
        return this.second;
    }

    /**
     * @param second the second to set
     */
    public T2 setSecond(T2 second) {
        T2 oldValue = this.second;
        this.second = second;
        return oldValue;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#getKey()
     */
    //@Override
    public T1 getKey() {
        return getFirst();
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#getValue()
     */
    //@Override
    public T2 getValue() {
        return getSecond();
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    //@Override
    public T2 setValue(T2 value) {
        return setSecond(value);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.first == null) ? 0 : this.first.hashCode());
        result = prime * result + ((this.second == null) ? 0 : this.second.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Tuple<?, ?> other = (Tuple<?, ?>) obj;
        if (this.first == null) {
            if (other.first != null)
                return false;
        } else if (!this.first.equals(other.first))
            return false;
        if (this.second == null) {
            if (other.second != null)
                return false;
        } else if (!this.second.equals(other.second))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[" + first + "=" + second + "]";
    }
}
