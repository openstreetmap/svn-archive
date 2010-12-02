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
package org.openstreetmap.josm.plugins.mapdust.gui.value;


import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;


/**
 * Represents the <code>MapdustAction</code> object .
 *
 * @author Bea
 *
 */
public class MapdustAction {

    /** The <code>MapdustServiceCommand</code> object */
    private MapdustServiceCommand command;

    /** The icon path */
    private String iconPath;

    /** The <code>MapdustBug</code> object */
    private MapdustBug mapdustBug;

    /** The <code>MapdustComment</code> object */
    private MapdustComment mapdustComment;

    /** The new status of the bug */
    private Integer newStatus;

    /**
     * The <code>MapdustAction</code> object
     */
    public MapdustAction() {}

    /**
     * The <code>MapdustAction</code> object.
     *
     * @param command The <code>MapdustServiceCommand</code> object
     * @param iconPath The icon path
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    public MapdustAction(MapdustServiceCommand command, String iconPath,
            MapdustBug mapdustBug) {
        this.command = command;
        this.iconPath = iconPath;
        this.mapdustBug = mapdustBug;
    }

    /**
     * The <code>MapdustAction</code> object.
     *
     * @param command The <code>MapdustServiceCommand</code> object
     * @param iconPath The icon path
     * @param mapdustBug The <code>MapdustBug</code> object
     * @param mapdustComment The <code>MapdustComment</code> object
     */
    public MapdustAction(MapdustServiceCommand command, String iconPath,
            MapdustBug mapdustBug, MapdustComment mapdustComment) {
        this.command = command;
        this.iconPath = iconPath;
        this.mapdustBug = mapdustBug;
        this.mapdustComment = mapdustComment;
    }

    /**
     * The <code>MapdustAction</code> object.
     *
     * @param command The <code>MapdustServiceCommand</code> object
     * @param iconPath The icon path
     * @param mapdustBug The <code>MapdustBug</code> object
     * @param mapdustComment The <code>MapdustComment</code> object
     * @param newStatus The new status of the object
     */
    public MapdustAction(MapdustServiceCommand command, String iconPath,
            MapdustBug mapdustBug, MapdustComment mapdustComment,
            Integer newStatus) {
        this.command = command;
        this.iconPath = iconPath;
        this.mapdustBug = mapdustBug;
        this.mapdustComment = mapdustComment;
        this.newStatus = newStatus;
    }

    /**
     * Returns the <code>MapdustServiceCommand</code> object
     *
     * @return the command
     */
    public MapdustServiceCommand getCommand() {
        return command;
    }

    /**
     * Returns the <code>MapdustComment</code> object
     *
     * @return the mapdustComment
     */
    public MapdustComment getMapdustComment() {
        return mapdustComment;
    }

    /**
     * Returns the icon path
     *
     * @return the iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Returns the <code>MapdustBug</code> object
     *
     * @return the mapdustBug
     */
    public MapdustBug getMapdustBug() {
        return mapdustBug;
    }

    /**
     * Returns the status
     *
     * @return the newStatus
     */
    public Integer getNewStatus() {
        return newStatus;
    }

}
