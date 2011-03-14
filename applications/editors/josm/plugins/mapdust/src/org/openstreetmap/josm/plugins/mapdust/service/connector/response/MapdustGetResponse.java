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


import org.openstreetmap.josm.plugins.mapdust.service.value.Paging;


/**
 * Represents a general response for the "GET" http method.
 *
 * @author Bea
 *
 */
public class MapdustGetResponse {

    /**
     * The <code>Paging</code> object
     */
    private Paging paging;

    /**
     * Builds a <code>MapdustGetResponse</code> object
     */
    public MapdustGetResponse() {}

    /**
     * Builds a <code>MapdustGetResponse</code> object
     *
     * @param paging A <code>Paging</code> object
     */
    public MapdustGetResponse(Paging paging) {
        this.paging = paging;
    }

    /**
     * Returns the <code>Paging</code> object
     *
     * @return the paging
     */
    public Paging getPaging() {
        return paging;
    }

    /**
     * Sets the <code>Paging</code> object
     *
     * @param paging the paging to set
     */
    public void setPaging(Paging paging) {
        this.paging = paging;
    }

}
