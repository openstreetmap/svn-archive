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


import java.util.Date;
import org.openstreetmap.josm.plugins.mapdust.service.value.Address;


/**
 * Defines the attributes of the <code>MapdustBugProperties</code> object.
 * 
 * @author Bea
 * 
 */
public class MapdustBugProperties {
    
    /** The creation date */
    private Date dateCreated;
    
    /** The update date */
    private Date dateUpdated;
    
    /** The status of the bug */
    private Integer status;
    
    /** The type of the bug */
    private String type;
    
    /** The relevance value */
    private Integer relevance;
    
    /** The description of the bug */
    private String description;
    
    /** Flag indicating if the description is default or not */
    private byte isDefaultDescription;
    
    /** The nickname of the bug */
    private String nickname;
    
    /** The skobbler user id */
    private String skoUid;
    
    /** The external user id */
    private String extUid;
    
    /** The source of the bug */
    private String source;
    
    /** The url of the kml */
    private String kmlUrl;
    
    /** The address of the bug */
    private Address address;
    
    /** The number of comments */
    private Integer numberOfComments;
    
    /** The bug comments */
    private MapdustCommentProperties[] comments;
    
    /**
     * Builds a <code>MapdustBugProperties</code> object.
     */
    public MapdustBugProperties() {}
    
    /**
     * Builds a <code>MapdustBugProperties</code> object.
     * 
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
     * @param address The address of the object
     * @param numberOfComments The number of comments
     * @param comments The array of comments
     */
    public MapdustBugProperties(Date dateCreated, Date dateUpdated,
            Integer status, String type, Integer relevance, String description,
            byte isDefaultDescription, String nickname, String skoUid,
            String extUid, String source, String kmlUrl, Address address,
            Integer numberOfComments, MapdustCommentProperties[] comments) {
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
        this.address = address;
        this.numberOfComments = numberOfComments;
        this.comments = comments;
    }
    
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public Date getDateUpdated() {
        return dateUpdated;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public String getType() {
        return type;
    }
    
    public Integer getRelevance() {
        return relevance;
    }
  
    public String getDescription() {
        return description;
    }
    
    public byte getIsDefaultDescription() {
        return isDefaultDescription;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getSkoUid() {
        return skoUid;
    }
    
    public String getExtUid() {
        return extUid;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getKmlUrl() {
        return kmlUrl;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public Integer getNumberOfComments() {
        return numberOfComments;
    }
    
    public MapdustCommentProperties[] getComments() {
        return comments;
    }
}