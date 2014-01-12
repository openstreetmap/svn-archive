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
package org.openstreetmap.josm.plugins.mapdust.service;


import java.util.List;
import org.openstreetmap.josm.plugins.mapdust.service.connector.MapdustConnector;
import org.openstreetmap.josm.plugins.mapdust.service.connector.MapdustConnectorException;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugResponse;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugsResponse;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustPostResponse;
import org.openstreetmap.josm.plugins.mapdust.service.converter.MapdustConverter;
import org.openstreetmap.josm.plugins.mapdust.service.value.BoundingBox;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBugFilter;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;
import org.openstreetmap.josm.plugins.mapdust.service.value.Paging;


/**
 * This class responsibility is to handle the MapDust API HTTP
 * request/responses.
 *
 * @author Bea
 *
 */
public class MapdustServiceHandler {

    /** The <code>MapdustConnector</code> object */
    private final MapdustConnector connector;

    /**
     * Builds a <code>MapdustServiceHandler</code> object with the default
     * settings.
     *
     */
    public MapdustServiceHandler() {
        this.connector = new MapdustConnector();
    }

    /**
     * Builds a <code>MapdustServiceHandler</code> object based on the given
     * arguments.
     *
     * @param connector The <code>MapdustConnector</code> object
     */
    public MapdustServiceHandler(MapdustConnector connector) {
        this.connector = connector;
    }

    /**
     * Searches for the <code>MapdustBug</code> objects in the bounding box
     * defined by the given coordinates. If there are no bugs in the given area
     * then an empty list will be returned. If one of the coordinates is missing
     * a corresponding exception will be thrown.
     *
     * @param bBox The bounding box where the bugs are searched.
     * @param filter The MapDust bug filter. The bugs can be filtered based on
     * the status, type and description. This parameter is not required.
     * @return A list of <code>MapdustBug</code> objects.
     * @throws MapdustServiceHandlerException In the case of an error
     */
    public List<MapdustBug> getBugs(BoundingBox bBox, MapdustBugFilter filter)
            throws MapdustServiceHandlerException {
        MapdustGetBugsResponse getBugsResponse = null;
        /* validates the coordinates */
        if (bBox.getMinLon() == null || bBox.getMinLat() == null
                || bBox.getMaxLon() == null || bBox.getMaxLat() == null) {
            throw new MapdustServiceHandlerException("Invalid coordinates!");
        }
        /* executes the getBug MapDust method */
        try {
            getBugsResponse = connector.getBugs(bBox, filter);
        } catch (MapdustConnectorException e) {
            throw new MapdustServiceHandlerException(e.getMessage(), e);
        }
        /* converts the result into a list of MapDust bugs */
        List<MapdustBug> bugsList =
                MapdustConverter.buildMapdustBugList(getBugsResponse);
        return bugsList;
    }

    /**
     * Retrieves the <code>MapdustBug</code> object with the given id. If the
     * paging object is not null, then the comments of the bug will be
     * paginated. If no bug found with the given id, then an empty object will
     * be returned.
     *
     * @param id The id of the object. This parameter is a required parameter.
     * @param paging A <code>Paging</code> object. This parameter is optional.
     * If it is null, it will be ignored.
     * @return A <code>MapdustBug</code> object with the given id.
     * @throws MapdustServiceHandlerException In the case of an error
     */
    public MapdustBug getBug(Long id, Paging paging)
            throws MapdustServiceHandlerException {
        MapdustGetBugResponse getBugResponse = null;
        /* validate id */
        if (id == null) {
            String errorMessage = "Invalid id. The id cannot be null!";
            throw new MapdustServiceHandlerException(errorMessage);
        }
        /* executes the getBug MapDust method */
        try {
            getBugResponse = connector.getBug(id, paging);
        } catch (MapdustConnectorException e) {
            throw new MapdustServiceHandlerException(e.getMessage(), e);
        }
        /* converts the response into a MapdustBug object */
        MapdustBug bug = MapdustConverter.buildMapdustBug(getBugResponse);
        return bug;
    }

    /**
     * Adds a new <code>MapdustBug</code> object to the MapDust OSM bug service.
     * If the object is null, a corresponding exception will be thrown.
     *
     * @param bug A <code>BugReport</code> object
     * @return The if of the created bug.
     * @throws MapdustServiceHandlerException In the case of an error
     */
    public Long addBug(MapdustBug bug) throws MapdustServiceHandlerException {
        MapdustPostResponse postResponse = null;
        /* validates the bug */
        if (bug == null) {
            String errorMessage = "Invalid bug. The bug cannot be null!";
            throw new MapdustServiceHandlerException(errorMessage);
        }
        /* executes the addBug MapDust method */
        try {
            postResponse = connector.addBug(bug);
        } catch (MapdustConnectorException e) {
            throw new MapdustServiceHandlerException(e.getMessage(), e);
        }
        /* get the id */
        Long id = null;
        if (postResponse != null) {
            id = postResponse.getId();
        }
        return id;
    }

    /**
     * Create a comment for a given MapDust bug. If the
     * <code>MapdustComment</code> object is null, an exception will be thrown.
     * The method returns the id of the created <code>MapdustComment</code>
     * object.
     *
     * @param comment A <code>MapdustComment</code> object.
     * @return The id of the created object.
     * @throws MapdustServiceHandlerException In the case of an error.
     */
    public Long commentBug(MapdustComment comment)
            throws MapdustServiceHandlerException {
        MapdustPostResponse postResponse = null;
        /* validates comment */
        if (comment == null) {
            String errorMessage = "Invalid comment. The comment cannot be null!";
            throw new MapdustServiceHandlerException(errorMessage);
        }
        /* execute commentBug MapDust method */
        try {
            postResponse = connector.commentBug(comment);
        } catch (MapdustConnectorException e) {
            throw new MapdustServiceHandlerException(e.getMessage(), e);
        }
        /* get the id */
        Long id = null;
        if (postResponse != null) {
            id = postResponse.getId();
        }
        return id;
    }

    /**
     * Changes the status of a given MapDust bug. The status of a MapDust bug
     * can be one of the following: 1-open, 2-fixed, 3-invalid. If the statusId
     * or the comment is null an exception will be thrown.
     *
     * @param statusId The id of the status. Possible values are:1, 2, or 3.
     * @param comment The <code>MapdustComment</code> object.
     * @return The id of the bug object.
     * @throws MapdustServiceHandlerException In the case of an error.
     */
    public Long changeBugStatus(Integer statusId, MapdustComment comment)
            throws MapdustServiceHandlerException {
        MapdustPostResponse postResponse = null;
        /* validates statusId */
        if (statusId == null) {
            String errorMessage = "Invalid status id. The status id cannot ";
            errorMessage += "be null!";
            throw new MapdustServiceHandlerException(errorMessage);
        }
        /* validates comment */
        if (comment == null) {
            String errorMessage =
                    "Invalid comment. The comment cannot be null!";
            throw new MapdustServiceHandlerException(errorMessage);
        }
        /* executes changeBugStatus MapDust method */
        try {
            postResponse = connector.changeBugStatus(statusId, comment);
        } catch (MapdustConnectorException e) {
            throw new MapdustServiceHandlerException(e.getMessage(), e);
        }
        /* get the id */
        Long id = null;
        if (postResponse != null) {
            id = postResponse.getId();
        }
        return id;
    }

}
