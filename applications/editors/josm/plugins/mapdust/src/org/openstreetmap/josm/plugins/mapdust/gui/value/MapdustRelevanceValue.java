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
 * Created on Apr 6, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.gui.value;


import java.util.HashMap;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;


/**
 * This is a helper object, used for representing the MapDust bug relevance -
 * actual value mappings (value from the slider).
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustRelevanceValue {

    /** The low MapDust bug relevance value */
    public static final MapdustRelevanceValue LOW = new MapdustRelevanceValue(
            MapdustRelevance.LOW, 0);

    /** The mid-low MapDust bug relevance value */
    public static final MapdustRelevanceValue MID_LOW =
            new MapdustRelevanceValue(MapdustRelevance.MID_LOW, 4);

    /** The medium MapDust bug relevance value */
    public static final MapdustRelevanceValue MEDIUM =
            new MapdustRelevanceValue(MapdustRelevance.MEDIUM, 8);

    /** The mid-high MapDust bug relevance value */
    public static final MapdustRelevanceValue MID_HIGH =
            new MapdustRelevanceValue(MapdustRelevance.MID_HIGH, 12);

    /** The high MapDust bug relevance value */
    public static final MapdustRelevanceValue HIGH = new MapdustRelevanceValue(
            MapdustRelevance.HIGH, 16);

    /** The MapDust bug relevance */
    private MapdustRelevance relevance;

    /** The actual slider value corresponding for this relevance */
    private int sliderValue;

    /**
     * The hash map containing the valid <code>MapdustRelevanceValue</code>
     * objects
     */
    private static java.util.HashMap<MapdustRelevance, Integer> map;

    /**
     * Builds a new <code>MapdustRelevanceValue</code> object.
     */
    private MapdustRelevanceValue() {}

    /**
     * Builds a new <code>MapdustRelevanceValue</code> object based on the given
     * arguments.
     *
     * @param relevance The MapDust bug relevance
     * @param sliderValue The actual slider value
     */
    private MapdustRelevanceValue(MapdustRelevance relevance, int sliderValue) {
        this.relevance = relevance;
        this.sliderValue = sliderValue;
        if (MapdustRelevanceValue.map == null) {
            MapdustRelevanceValue.map =
                    new HashMap<MapdustRelevance, Integer>();
        }
        MapdustRelevanceValue.map.put(relevance, sliderValue);
    }

    /**
     * Returns the slider value for the given MapDust bug relevance. If the
     * given relevance does not exists, then the method returns null.
     *
     * @param relevance The <code>MapdustRelevanceValue</code> object
     * @return The corresponding slider value
     */
    public static Integer getSliderValue(MapdustRelevance relevance) {
        if (relevance != null) {
            Integer sliderValue = MapdustRelevanceValue.map.get(relevance);
            return sliderValue;
        }
        return null;
    }

    /**
     * Returns the relevance
     *
     * @return the relevance
     */
    public MapdustRelevance getRelevance() {
        return relevance;
    }

    /**
     * Returns the slider value
     *
     * @return the sliderValue
     */
    public int getSliderValue() {
        return sliderValue;
    }

}
