/* Copyright (c) 2010, skobbler GmbH
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
 */
package org.openstreetmap.josm.plugins.mapdust.service.connector.response;


/**
 * Defines the attributes of the <code>MapdustBugContent</code> object.
 *
 * @author Bea
 *
 */
public class MapdustBugContent extends GeneralContent {

    /** The <code>Geometry</code> object */
    private Geometry geometry;

    /** The <code>MapdustBugProperties</code> object */
    private MapdustBugProperties properties;

    /**
     * Builds a <code>MapdustBugContent</code> object.
     *
     */
    public MapdustBugContent() {}

    /**
     * Builds a <code>MapdustBugContent</code> object based on the given
     * arguments.
     *
     * @param geometry A <code>Geometry</code> object
     * @param properties A <code>MapdustBugProperties</code> object
     */
    public MapdustBugContent(Geometry geometry, MapdustBugProperties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    /**
     * Returns the geometry
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the geometry
     *
     * @param geometry the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Returns the MapDust bug properties
     *
     * @return the properties
     */
    public MapdustBugProperties getProperties() {
        return properties;
    }

    /**
     * Sets the MapDust bug properties
     *
     * @param properties the properties to set
     */
    public void setProperties(MapdustBugProperties properties) {
        this.properties = properties;
    }

}
