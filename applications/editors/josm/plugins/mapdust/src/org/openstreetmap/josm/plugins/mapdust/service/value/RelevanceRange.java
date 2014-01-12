/*
 * Copyright (c) 2010, skobbler GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Apr 5, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.service.value;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This a helper object, used for representing the MapDust bug relevance range.
 * A bug relevance range is defined by the lower and the upper values.
 *
 * @author Bea
 * @version $Revision$
 */
public class RelevanceRange implements Serializable, Comparable<RelevanceRange> {

    /** The serial version UID */
    private static final long serialVersionUID = -5187434838022942754L;

    /** The low range */
    public static final RelevanceRange LOW_RANGE = new RelevanceRange(0, 3);

    /** The mid-low range */
    public static final RelevanceRange MID_LOW_RANGE = new RelevanceRange(4, 6);

    /** The medium range */
    public static final RelevanceRange MEDIUM_RANGE = new RelevanceRange(7, 9);

    /** The mid-high range */
    public static final RelevanceRange MID_HIGH_RANGE = new RelevanceRange(10,
            13);

    /** The high range */
    public static final RelevanceRange HIGH_RANGE = new RelevanceRange(14, 17);

    /** A set containing all the valid range values */
    private static java.util.Set<RelevanceRange> set;

    /** The minimal value */
    private int lowerValue;

    /** The maximal value */
    private int upperValue;

    /**
     * Builds a new <code>RelevanceRange</code> object.
     *
     */
    private RelevanceRange() {}

    /**
     * Builds a new <code>RelevanceRange</code> object based on the given
     * arguments.
     *
     * @param lowerValue The mimal value of the range
     * @param upperValue The maximal value of the range
     */
    private RelevanceRange(int lowerValue, int upperValue) {
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
        if (set == null) {
            set = new HashSet<RelevanceRange>();
        }
        set.add(this);
    }

    /**
     * Returns the range for the given value. A value belongs to a range if
     * value>=loweValue and value <=upperValue. If there is no range defined for
     * the given value, then the method return null.
     *
     * @param value An integer value, where value >=0 and value <=17
     * @return The <code>RelevanceRange</code> corresponding to the given value.
     */
    public static RelevanceRange getRelevanceRange(int value) {
        Iterator<RelevanceRange> it = set.iterator();
        RelevanceRange range = null;
        while (it.hasNext()) {
            range = it.next();
            if (value >= range.getLowerValue()
                    && value <= range.getUpperValue()) {
                return range;
            }
        }
        return null;
    }

    /**
     * Returns the lower value
     *
     * @return the lowerValue
     */
    public int getLowerValue() {
        return lowerValue;
    }

    /**
     * Returns the upper value
     *
     * @return the upperValue
     */
    public int getUpperValue() {
        return upperValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lowerValue;
        result = prime * result + upperValue;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        RelevanceRange other = (RelevanceRange) obj;
        if (lowerValue != other.lowerValue)
            return false;
        if (upperValue != other.upperValue)
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(RelevanceRange obj) {
        if (this.getLowerValue() < obj.getLowerValue()
                && this.getUpperValue() < obj.getUpperValue()) {
            return -1;
        }
        if (this.getLowerValue() > obj.getLowerValue()
                && this.getUpperValue() > obj.getUpperValue()) {
            return 1;
        }
        return 0;
    }

}
