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
 * Created on 06.03.2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.service.value;


/**
 * Helper object used for representing a bounding box. This object it is used
 * for representing the searching area.
 *
 * @author Bea
 * @version $Revision$
 */
public class BoundingBox {

    /** The minimum longitude */
    private Double minLon;

    /** The minimum latitude */
    private Double minLat;

    /** The maximum longitude */
    private Double maxLon;

    /** The maximum latitude */
    private Double maxLat;

    /**
     * Builds a <code>BoundingBox</code> object
     */
    public BoundingBox(){}

    /**
     * Builds a <code>BoundingBox</code> object based on the given arguments.
     *
     * @param minLon The minimum longitude
     * @param minLat The minimum latitude
     * @param maxLon The maximum longitude
     * @param maxLat The maximum latitude
     */
    public BoundingBox(Double minLon, Double minLat, Double maxLon,
            Double maxLat) {
        this.minLon = minLon;
        this.minLat = minLat;
        this.maxLon = maxLon;
        this.maxLat = maxLat;
        normalize();
    }

    /**
     * Normalize the bounding box values.
     */
    private void normalize() {
        if (minLon < -180) {
            minLon = -180.0;
        }
        if (maxLon > 180) {
            maxLon = 180.0;
        }
        if (minLat < -90) {
            minLat = -90.0;
        }
        if (maxLat > 90) {
            maxLat = 90.0;
        }
    }

    /**
     * Returns the minimum longitude
     *
     * @return the minLon
     */
    public Double getMinLon() {
        return minLon;
    }

    /**
     * Returns the minimum latitude
     *
     * @return the minLat
     */
    public Double getMinLat() {
        return minLat;
    }

    /**
     * Returns the maximum longitude
     *
     * @return the maxLon
     */
    public Double getMaxLon() {
        return maxLon;
    }

    /**
     * Returns the maximum latitude
     *
     * @return the maxLat
     */
    public Double getMaxLat() {
        return maxLat;
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
        result = prime * result + ((maxLat == null) ? 0 : maxLat.hashCode());
        result = prime * result + ((maxLon == null) ? 0 : maxLon.hashCode());
        result = prime * result + ((minLat == null) ? 0 : minLat.hashCode());
        result = prime * result + ((minLon == null) ? 0 : minLon.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundingBox) {
            BoundingBox bBox = (BoundingBox) obj;
            Double bBoxMinLon = bBox.getMinLon();
            Double bBoxMinLat = bBox.getMinLat();
            Double bBoxMaxLon = bBox.getMaxLon();
            Double bBoxMaxLat = bBox.getMaxLat();
            return (minLon.equals(bBoxMinLon) && minLat.equals(bBoxMinLat)
                    && maxLon.equals(bBoxMaxLon) && maxLat.equals(bBoxMaxLat));
        }
        return false;
    }

}
