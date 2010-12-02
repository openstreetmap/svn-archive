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
 * Represents the response of the 'getBug' method . This object is used for
 * building the response from a JSON format.
 *
 * @author Bea
 *
 */
public class MapdustGetBugResponse {

    /** The <code>Geometry</code> object */
    private Geometry geometry;

    /** The id */
    private Long id;

    /** The <code>MapdustBugProprties</code> object */
    private MapdustBugProperties properties;

    /**
     * Builds a <code>MapdustGetBugResponse</code> object
     */
    public MapdustGetBugResponse() {}

    /**
     * Builds a <code>MapdustGetBugResponse</code> object
     *
     * @param geometry The <code>Geometry</code object
     * @param id The id of the object
     * @param properties The <code>MapdustBugProperties</code> object
     */
    public MapdustGetBugResponse(Geometry geometry, Long id,
            MapdustBugProperties properties) {
        this.geometry = geometry;
        this.id = id;
        this.properties = properties;
    }

    /**
     * Returns the <code>Geometry</code> object
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Sets the <code>Geometry</code> object
     *
     * @param geometry the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Returns the id
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the <code>MapdustBugProperties</code> properties
     *
     * @return the properties
     */
    public MapdustBugProperties getProperties() {
        return properties;
    }

    /**
     * Sets the <code>MapdustBugProperties</code> object
     *
     * @param properties the properties to set
     */
    public void setProperties(MapdustBugProperties properties) {
        this.properties = properties;
    }

}
