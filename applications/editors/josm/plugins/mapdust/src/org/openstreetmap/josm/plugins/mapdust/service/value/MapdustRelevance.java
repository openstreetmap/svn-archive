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
 * Created on Mar 31, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.service.value;


import java.io.Serializable;
import java.util.HashMap;


/**
 * This a helper object, used for representing the MapDust bug relevance. A bug
 * relevance is defined by a name and a relevance range.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustRelevance implements Serializable,
        Comparable<MapdustRelevance> {

    /** Serial Version UID */
    private static final long serialVersionUID = 8910563589536711954L;

    /** The low MapDust bug relevance */
    public static final MapdustRelevance LOW = new MapdustRelevance("Low",
            RelevanceRange.LOW_RANGE);

    /** The low MapDust bug relevance */
    public static final MapdustRelevance MID_LOW = new MapdustRelevance(
            "Mid-Low", RelevanceRange.MID_LOW_RANGE);

    /** The low MapDust bug relevance */
    public static final MapdustRelevance MEDIUM = new MapdustRelevance(
            "Medium", RelevanceRange.MEDIUM_RANGE);

    /** The low MapDust bug relevance */
    public static final MapdustRelevance MID_HIGH = new MapdustRelevance(
            "Mid-High", RelevanceRange.MID_HIGH_RANGE);

    /** The low MapDust bug relevance */
    public static final MapdustRelevance HIGH = new MapdustRelevance("High",
            RelevanceRange.HIGH_RANGE);

    /** The name of the relevance */
    private String name;

    /** The range of the relevance */
    private RelevanceRange range;

    /** The hash map containing the valid values */
    private static java.util.HashMap<RelevanceRange, MapdustRelevance> map;

    /***
     * Builds a new <code>MapdustRelevance</code> object.
     */
    public MapdustRelevance() {}

    /**
     * Builds a new <code>MapdustRelevance</code> object based on the given
     * arguments.
     *
     * @param name The name of the relevance
     * @param range The range of the relevance
     */
    public MapdustRelevance(String name, RelevanceRange range) {
        this.name = name;
        this.range = range;
        if (MapdustRelevance.map == null) {
            MapdustRelevance.map =
                    new HashMap<RelevanceRange, MapdustRelevance>();
        }
        MapdustRelevance.map.put(range, this);
    }

    /**
     * Returns the corresponding <code>MapdustRelevance</code> object for the
     * given value. If there is no corresponding <code>MapdustRelevance</code>
     * for the given value, the method returns null.
     *
     * @param value The value
     * @return A <code>MapdustRelevance</code> object
     */
    public static MapdustRelevance getMapdustRelevance(int value) {
        RelevanceRange range = RelevanceRange.getRelevanceRange(value);
        if (range != null) {
            MapdustRelevance relevance = map.get(range);
            return relevance;
        }
        return null;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the range.
     *
     * @return the range
     */
    public RelevanceRange getRange() {
        return range;
    }


    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        MapdustRelevance other = (MapdustRelevance) obj;
        if (other == null) {
            return false;
        }
        if (name == null || other.name == null) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (range == null || other.getRange() == null) {
            return false;
        }
        if (!range.equals(other.range)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(MapdustRelevance obj) {
        if (this.equals(obj)) {
            return 0;
        }
        if (obj == null) {
            return -1;
        }
        return this.getRange().compareTo(obj.getRange());
    }

}
