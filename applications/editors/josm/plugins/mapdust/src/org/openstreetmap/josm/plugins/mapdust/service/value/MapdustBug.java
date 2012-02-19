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


import java.util.Date;
import org.openstreetmap.josm.data.coor.LatLon;


/**
 * Defines the attributes of the <code>MapdustBug</code> object.
 *
 * @author Bea
 *
 */
public class MapdustBug {

    /** The id of the bug */
    private Long id;

    /** The coordinates of the bug */
    private LatLon latLon;

    /** The address of the bug */
    private Address address;

    /** The creation date */
    private Date dateCreated;

    /** The update date */
    private Date dateUpdated;

    /** The status of the object */
    private Status status;

    /** The type of the object */
    private BugType type;

    /** The relevance of the object */
    private MapdustRelevance relevance;

    /** The description of the bug */
    private String description;

    /** Flag indicating if the description is default or not */
    private boolean isDefaultDescription;

    /** The nickname of the user who created the bug */
    private String nickname;

    /** The skobbler user id */
    private String skoUid;

    /** The external user id */
    private String extUid;

    /** The source of the bug */
    private String source;

    /** The url of the KML file */
    private String kmlUrl;

    /** The number of comments */
    private Integer numberOfComments;

    /** The array of comments */
    private MapdustComment[] comments;

    /**
     * Builds a <code>MapdustBug</code> object.
     */
    public MapdustBug() {}

    /**
     * Builds a <code>MapdustBug</code> object based on the given arguments.
     *
     * @param latLon The coordinate
     * @param type The type of the bug
     * @param description The description of the bug
     * @param nickname The nickname of the user who created the bug
     */
    public MapdustBug(LatLon latLon, BugType type, String description,
            String nickname) {
        this.latLon = latLon;
        this.type = type;
        this.description = description;
        this.nickname = nickname;
    }

    /**
     * Builds a <code>MapdustBug</code> object based on the given arguments.
     *
     * @param id The id of the bug
     * @param latLon The coordinates of the bug
     * @param address The address of the bug
     * @param dateCreated The creation date
     * @param dateUpdated The update date
     * @param status The status of the bug
     * @param type The type of the bug
     * @param relevance The relevance of the bug
     * @param description The description of the bug
     * @param isDefaultDescription Flag indicating if the description is default
     * or not
     * @param nickname The nickname
     * @param skoUid The skobbler user id
     * @param extUid The external user id
     * @param source The source of the bug
     * @param kmlUrl The URL of the KML
     * @param numberOfComments The number of comments
     * @param comments The array of comments
     */
    public MapdustBug(Long id, LatLon latLon, Address address,
            Date dateCreated, Date dateUpdated, Status status, BugType type,
            MapdustRelevance relevance, String description,
            boolean isDefaultDescription, String nickname, String skoUid,
            String extUid, String source, String kmlUrl,
            Integer numberOfComments, MapdustComment[] comments) {
        this.id = id;
        this.latLon = latLon;
        this.address = address;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.status = status;
        this.type = type;
        this.relevance = relevance;
        this.description = description;
        this.isDefaultDescription = isDefaultDescription;
        this.nickname = nickname;
        this.skoUid = skoUid;
        this.extUid = extUid;
        this.source = source;
        this.kmlUrl = kmlUrl;
        this.numberOfComments = numberOfComments;
        this.comments = comments;
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
     * Returns the coordinates of the bug
     *
     * @return the latLon
     */
    public LatLon getLatLon() {
        return latLon;
    }

    /**
     * Sets the coordinates of the bug
     *
     * @param latLon the latLon to set
     */
    public void setLatLon(LatLon latLon) {
        this.latLon = latLon;
    }

    /**
     * Returns the date created
     *
     * @return the dateCreated
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Sets the date created
     *
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Returns the date updated
     *
     * @return the dateUpdated
     */
    public Date getDateUpdated() {
        return dateUpdated;
    }

    /**
     * Sets the date updated
     *
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    /**
     * Returns the status
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status
     *
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the type
     *
     * @return the type
     */
    public BugType getType() {
        return type;
    }

    /**
     * Sets the type
     *
     * @param type the type to set
     */
    public void setType(BugType type) {
        this.type = type;
    }

    /**
     * Returns the relevance
     *
     * @return the relevance
     */
    public MapdustRelevance getRelevance() {
        return relevance;
    }

    /**
     * Sets the relevance
     *
     * @param relevance the relevance to set
     */
    public void setRelevance(MapdustRelevance relevance) {
        this.relevance = relevance;
    }

    /**
     * Returns the description
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the isDefaultDescription flag
     *
     * @return the isDefaultDescription
     */
    public boolean getIsDefaultDescription() {
        return isDefaultDescription;
    }

    /**
     * Sets the isDefaultDescription flag
     *
     * @param isDefaultDescription the isDefaultDescription to set
     */
    public void setIsDefaultDescription(boolean isDefaultDescription) {
        this.isDefaultDescription = isDefaultDescription;
    }

    /**
     * Returns the address
     *
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address
     *
     * @param address the address to set
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Returns the nickname
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname
     *
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the skobbler user id
     *
     * @return the skoUid
     */
    public String getSkoUid() {
        return skoUid;
    }

    /**
     * Sets the skobbler user id
     *
     * @param skoUid the skoUid to set
     */
    public void setSkoUid(String skoUid) {
        this.skoUid = skoUid;
    }

    /**
     * Returns the external user id
     *
     * @return the extUid
     */
    public String getExtUid() {
        return extUid;
    }

    /**
     * Sets the external user id
     *
     * @param extUid the extUid to set
     */
    public void setExtUid(String extUid) {
        this.extUid = extUid;
    }

    /**
     * Returns the source
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source
     *
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the kml URL
     *
     * @return the kmlUrl
     */
    public String getKmlUrl() {
        return kmlUrl;
    }

    /**
     * Sets the kml URL
     *
     * @param kmlUrl the kmlUrl to set
     */
    public void setKmlUrl(String kmlUrl) {
        this.kmlUrl = kmlUrl;
    }

    /**
     * Returns the number of comments
     *
     * @return the numberOfComments
     */
    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    /**
     * Sets the number of comments
     *
     * @param numberOfComments the numberOfComments to set
     */
    public void setNumberOfComments(Integer numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    /**
     * Returns the bug comments
     *
     * @return the comments
     */
    public MapdustComment[] getComments() {
        return comments;
    }

    /**
     * Sets the bug comments
     * @param comments the comments to set
     */
    public void setComments(MapdustComment[] comments) {
        this.comments = comments;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapdustBug other = (MapdustBug) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
