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
 * Defines the attributes of the <code>Paging</code> object.
 *
 * @author Bea
 */
public class Paging {

    /** The page */
    private Integer page;

    /** The items */
    private Integer items;

    /** The total */
    private Integer total;

    /**
     * Builds a <code>Paging</code> object.
     *
     */
    public Paging() {}

    /**
     * Builds a <code>Paging</code> object with the given argument.
     *
     * @param page The page number
     * @param items The items of objects displayed on that page
     * @param total The total number of objects
     */
    public Paging(Integer page, Integer items, Integer total) {
        this.page = page;
        this.items = items;
        this.total = total;
    }

    /**
     * Returns the page number
     *
     * @return the page
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Sets the page number
     *
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * Returns the items
     *
     * @return the items
     */
    public Integer getItems() {
        return items;
    }

    /**
     * Sets the items
     *
     * @param items the items to set
     */
    public void setItems(Integer items) {
        this.items = items;
    }

    /**
     * Returns the total
     *
     * @return the total
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Sets the total
     *
     * @param total the total to set
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

}
