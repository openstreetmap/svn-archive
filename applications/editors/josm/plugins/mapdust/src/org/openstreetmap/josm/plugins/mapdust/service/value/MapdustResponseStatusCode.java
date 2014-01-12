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
package org.openstreetmap.josm.plugins.mapdust.service.value;


/**
 * Defines the response status codes of the Mapdust service.
 *
 * @author Bea
 */
public enum MapdustResponseStatusCode {

    /** Status 200 */
    Status200(200, null, "SUCESS"),

    /** Status 201 */
    Status201(201, null, "SUCESS"),

    /** Status 204 */
    Status204(204, 204, "No results found."),

    /** A general status */
    Status(null, null, "Some other error."),

    /** Status 400 */
    Status400(400, 400, "Missing or invalid value for parameter 'x'."),

    /** Status 401 */
    Status401(401, 401, "Invalid API key."),

    /** Status 403 */
    Status403(403, 403, "Request rate limit exceeded."),

    /** Status 405 */
    Status405(405, 405, "Method not allowed."),

    /** Status 404 */
    Status404(404, 404, "Not found."),

    /** Status 500 */
    Status500(500, 500, "Internal server error."),

    /** Status 601 */
    Status601(400, 601, "There is no bug with the given ID."),

    /** Status 602 */
    Status602(400, 602, "There is no skobbler user with thw igven ID.");

    /** The code of the error */
    private Integer statusCode;

    /** The Mapdust API code */
    private Integer apiCode;

    /** The description of the response code */
    private String description;

    /**
     * Builds a new <code>BillingServiceResponseCode</code>
     *
     * @param statusCode The code of the response
     * @param apiCode The code of the Mapdust API
     * @param description The description of the response
     */
    private MapdustResponseStatusCode(Integer statusCode, Integer apiCode,
            String description) {
        this.statusCode = statusCode;
        this.apiCode = apiCode;
        this.description = description;
    }

    /**
     * Returns the status code
     *
     * @return the statusCode
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the api code
     *
     * @return the apiCode
     */
    public Integer getApiCode() {
        return apiCode;
    }

    /**
     * Returns the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
