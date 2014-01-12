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
package org.openstreetmap.josm.plugins.mapdust.service.converter;


import java.util.ArrayList;
import java.util.List;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.Geometry;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustBugContent;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustBugProperties;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustCommentProperties;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugResponse;
import org.openstreetmap.josm.plugins.mapdust.service.connector.response.MapdustGetBugsResponse;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;
import org.openstreetmap.josm.plugins.mapdust.service.value.Status;
import org.openstreetmap.josm.plugins.mapdust.service.value.BugType;


/**
 * The <code>MapdustConverter</code> object. Builds a MapdustBug, list of
 * MapdustBug based on the given response object.
 *
 * @author Bea
 *
 */
public class MapdustConverter {

    /**
     * Builds a <code>MapdustBug</code> object from the given
     * <code>MapdustGetBugResponse</code> object.
     *
     * @param bugResponse A <code>MapdustGetBugResponse</code> object.
     * @return A <code>MapdustBug</code> object.
     */
    public static MapdustBug buildMapdustBug(MapdustGetBugResponse bugResponse) {
        MapdustBug bug = new MapdustBug();
        if (bugResponse != null) {
            /* sets the id */
            bug.setId(bugResponse.getId());
            /* sets the coordinates */
            Geometry geometry = bugResponse.getGeometry();
            LatLon latLon = null;
            if (geometry != null && geometry.getCoordinates() != null
                    && geometry.getCoordinates().length == 2) {
                Double longitude = geometry.getCoordinates()[0];
                Double latitude = geometry.getCoordinates()[1];
                latLon = new LatLon(latitude, longitude);
            }
            bug.setLatLon(latLon);
            /* sets the properties of the bug */
            MapdustBugProperties bugProperties = bugResponse.getProperties();
            if (bugProperties != null) {
                /* sets the address */
                bug.setAddress(bugProperties.getAddress());
                /* sets the status */
                Status status = Status.getStatus(bugProperties.getStatus());
                bug.setStatus(status);
                /* sets the type */
                BugType type = BugType.getType(bugProperties.getType());
                bug.setType(type);
                /* sets the relevance */
                MapdustRelevance relevance =
                        MapdustRelevance.getMapdustRelevance(bugProperties
                                .getRelevance());
                bug.setRelevance(relevance);
                /* sets the creation date */
                bug.setDateCreated(bugProperties.getDateCreated());
                /* sets the update date */
                bug.setDateUpdated(bugProperties.getDateUpdated());
                /* sets the description */
                bug.setDescription(bugProperties.getDescription());
                /* sets the isDefaultDescription */
                bug.setIsDefaultDescription(bugProperties
                        .getIsDefaultDescription());
                /* sets the skobbler user id */
                bug.setSkoUid(bugProperties.getSkoUid());
                /* sets the external user id */
                bug.setExtUid(bugProperties.getExtUid());
                /* sets the nickname */
                bug.setNickname(bugProperties.getNickname());
                /* sets the source */
                bug.setSource(bugProperties.getSource());
                /* sets the kml url */
                bug.setKmlUrl(bugProperties.getKmlUrl());
                /* sets the number of comments */
                bug.setNumberOfComments(bugProperties.getNumberOfComments());
                /* sets the comments */
                MapdustCommentProperties[] commentProperties =
                        bugProperties.getComments();
                MapdustComment[] commentArray =
                        buildMapdustCommentArray(bug.getId(), commentProperties);
                bug.setComments(commentArray);
            }
        }
        return bug;
    }

    /**
     * Builds a list of <code>MapdustBug</code> objects based on the given
     * <code>MapdustGetBugsResponse</code> object.
     *
     * @param bugsResponse A <code>MapdustGetBugsResponse</code> object
     * @return A list of <code>MapdustBug</code> objects
     */
    public static List<MapdustBug> buildMapdustBugList(
            MapdustGetBugsResponse bugsResponse) {
        List<MapdustBug> bugsList = new ArrayList<MapdustBug>();
        if (bugsResponse != null) {
            MapdustBugContent[] bugContent = bugsResponse.getFeatures();
            if (bugContent != null) {
                for (MapdustBugContent obj : bugContent) {
                    MapdustBug bug = buildMapdustBug(obj);
                    bugsList.add(bug);
                }
            }
        }
        return bugsList;
    }

    /**
     * Builds a <code>MapdustBug</code> object from the given
     * <code>MapdustBugContent</code> object.
     *
     * @param bugContent A <code>MapdustBugContent</code> object
     * @return A <code>MapdustBug</code> object.
     */
    private static MapdustBug buildMapdustBug(MapdustBugContent bugContent) {
        MapdustBug bug = new MapdustBug();
        if (bugContent != null) {
            /* set the id */
            bug.setId(bugContent.getId());
            /* set the coordinates */
            Geometry geometry = bugContent.getGeometry();
            LatLon latLon = null;
            if (geometry != null && geometry.getCoordinates() != null
                    && geometry.getCoordinates().length == 2) {
                Double longitude = geometry.getCoordinates()[0];
                Double latitude = geometry.getCoordinates()[1];
                latLon = new LatLon(latitude, longitude);
            }
            bug.setLatLon(latLon);
            /* set the bug properties */
            MapdustBugProperties bugProperties = bugContent.getProperties();
            if (bugProperties != null) {
                /* sets the address */
                bug.setAddress(bugProperties.getAddress());
                /* sets the status */
                Status status = Status.getStatus(bugProperties.getStatus());
                bug.setStatus(status);
                /* sets the type */
                BugType type = BugType.getType(bugProperties.getType());
                bug.setType(type);
                /* sets the relevance */
                MapdustRelevance relevance =
                        MapdustRelevance.getMapdustRelevance(bugProperties
                                .getRelevance());
                bug.setRelevance(relevance);
                /* sets the creation date */
                bug.setDateCreated(bugProperties.getDateCreated());
                /* sets the update date */
                bug.setDateUpdated(bugProperties.getDateUpdated());
                /* sets the description */
                bug.setDescription(bugProperties.getDescription());
                /* sets the skobbler user id */
                bug.setSkoUid(bugProperties.getSkoUid());
                /* sets the external user id */
                bug.setExtUid(bugProperties.getExtUid());
                /* sets the nickname */
                bug.setNickname(bugProperties.getNickname());
                /* sets the source */
                bug.setSource(bugProperties.getSource());
                /* sets the kml url */
                bug.setKmlUrl(bugProperties.getKmlUrl());
                /* sets the number of comments */
                bug.setNumberOfComments(bugProperties.getNumberOfComments());
                /* sets the comments */
                MapdustCommentProperties[] commentProperties =
                        bugProperties.getComments();
                MapdustComment[] commentArray =
                        buildMapdustCommentArray(bug.getId(), commentProperties);
                bug.setComments(commentArray);
            }
        }
        return bug;
    }

    /**
     * Builds a <code>MapdustComment</code> object based on the given arguments.
     *
     * @param bugId The id of the bug
     * @param commentProperties The <code>MapdustCommentProperties</code>
     * object.
     * @return A <code>MapdustComment</code> object
     */
    private static MapdustComment buildMapdustComment(Long bugId,
            MapdustCommentProperties commentProperties) {
        MapdustComment comment = new MapdustComment();
        if (bugId != null) {
            comment.setBugId(bugId);
        }
        if (commentProperties != null) {
            comment.setDateCreated(commentProperties.getDateCreated());
            comment.setCommentText(commentProperties.getComment());
            comment.setExtUid(commentProperties.getExtUid());
            comment.setNickname(commentProperties.getNickname());
            comment.setSkoUid(commentProperties.getSkoUid());
            comment.setSource(commentProperties.getSource());
        }
        return comment;
    }

    /**
     * Builds an array of <code>MapdustComment</code> objects based on the given
     * arguments.
     *
     * @param bugId the if of the bug
     * @param commentProperties The array of
     * <code>MapdusrCommentProperties</code> objects
     * @return An array of <code>MapdustComment</code> object
     */
    private static MapdustComment[] buildMapdustCommentArray(Long bugId,
            MapdustCommentProperties[] commentProperties) {
        List<MapdustComment> commentList = new ArrayList<MapdustComment>();
        if (bugId != null) {
            if (commentProperties != null) {
                for (MapdustCommentProperties obj : commentProperties) {
                    MapdustComment comment = buildMapdustComment(bugId, obj);
                    commentList.add(comment);
                }
            }
        }
        MapdustComment[] commentArray =
                commentList.toArray(new MapdustComment[0]);
        return commentArray;
    }

}
