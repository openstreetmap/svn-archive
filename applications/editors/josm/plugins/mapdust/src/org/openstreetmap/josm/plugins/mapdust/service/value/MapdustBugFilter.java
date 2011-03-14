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
 * Created on Feb 17, 2011 by Bea
 * Modified on $DateTime$ by $Author$
 */
package org.openstreetmap.josm.plugins.mapdust.service.value;


import java.util.List;


/**
 * This object represents the MapDust bug filter.
 *
 * @author Bea
 * @version $Revision$
 */
public class MapdustBugFilter {

    /** The list of status filter values */
    private List<Integer> statuses;

    /** The list of type filter values */
    private List<String> types;

    /** The description filter value */
    private Boolean descr;

    /**
     * Build a <code>MapdustBugFilter</code> object
     */
    public MapdustBugFilter() {}

    /**
     * Builds a <code>MapdustBugFilter</code> object based on the given
     * arguments.
     *
     * @param statuses The list of status filter values
     * @param types The list of type filter values
     * @param descr The description filter value
     */
    public MapdustBugFilter(List<Integer> statuses, List<String> types,
            Boolean descr) {
        this.statuses = statuses;
        this.types = types;
        this.descr = descr;
    }

    /**
     * Returns the list of status filter values
     *
     * @return the statuses
     */
    public List<Integer> getStatuses() {
        return statuses;
    }

    /**
     * Sets the list of status filter values.
     *
     * @param statuses the statuses to set
     */
    public void setStatuses(List<Integer> statuses) {
        this.statuses = statuses;
    }

    /**
     * Returns the list of type filter values.
     *
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * Sets the list of type filter values.
     *
     * @param types the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

    /**
     * Returns the description filter value.
     *
     * @return the descr
     */
    public Boolean getDescr() {
        return descr;
    }

    /**
     * Sets the description filter value.
     *
     * @param descr the descr to set
     */
    public void setDescr(Boolean descr) {
        this.descr = descr;
    }

}
