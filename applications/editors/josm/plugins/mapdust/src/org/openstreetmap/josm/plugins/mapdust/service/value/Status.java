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


import java.io.Serializable;
import java.util.HashMap;


/**
 * Defines the attributes of the <code>Status</code> object.
 *
 * @author Bea
 */
public class Status implements Serializable {

    /** The serial version UID */
    private static final long serialVersionUID = 5534551701260061940L;

    /** The key of the <code>Status</code> */
    private Integer key;

    /** The value of the <code>Status</code> */
    private String value;

    /** The hash map containing the valid values */
    private static java.util.HashMap<Integer, Status> table = null;

    /**
     * Builds a new <code>Status</code> object
     */
    public Status() {}

    /**
     * Builds a new <code>status</code> object
     *
     * @param key The key of the object
     * @param value The value of the object
     */
    public Status(Integer key, String value) {
        this.key = key;
        this.value = value;
        if (Status.table == null) {
            Status.table = new HashMap<Integer, Status>();
        }
        Status.table.put(key, this);
    }

    /** The open status */
    public static final Status OPEN = new Status(1, "Open");

    /** The fixed status */
    public static final Status FIXED = new Status(2, "Fixed");

    /** The closed status */
    public static final Status INVALID = new Status(3, "Invalid");

    /**
     * Returns the <code>Status</code> object for the given value.
     *
     * @param value The value of the object
     * @return A <code>Status</code> object
     * @throws java.lang.IllegalStateException If the status value is invalid
     */
    public static Status getStatus(Integer value)
            throws java.lang.IllegalStateException {
        Status status = table.get(value);
        if (status == null) {
            throw new java.lang.IllegalStateException();
        }
        return status;
    }

    /**
     * Verifies if two objects are equals or not.
     */
    @Override
    public boolean equals(java.lang.Object obj) {
        return (obj == this);
    }

    /**
     * Returns the hash code of the object
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns the key
     *
     * @return the key
     */
    public Integer getKey() {
        return key;
    }

    /**
     * Sets the key
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
