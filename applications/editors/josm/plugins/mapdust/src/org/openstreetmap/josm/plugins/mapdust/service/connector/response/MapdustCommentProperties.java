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


/**
 * Defines the attributes of the <code>MapdustCommentProperties</code> object.
 *
 * @author Bea
 *
 */
public class MapdustCommentProperties {

    /** The date created */
    private Date dateCreated;

    /** The comment text */
    private String comment;

    /** The nickname */
    private String nickname;

    /** The skobbler user id */
    private String skoUid;

    /** The external user id */
    private String extUid;

    /** The source */
    private String source;

    /**
     * Builds a <code>MapdustCommentProperties</code> object
     */
    public MapdustCommentProperties() {}

    /**
     * Builds a <code>MapdustCommentProperties</code> object
     *
     * @param dateCreated The date of creation
     * @param comment The text of the comment
     * @param nickname The nickname of the user
     * @param skoUid The skobbler user id of the user
     * @param extUid The external user id of the user
     * @param source The source of the user
     */
    public MapdustCommentProperties(Date dateCreated, String comment,
            String nickname, String skoUid, String extUid, String source) {
        this.dateCreated = dateCreated;
        this.comment = comment;
        this.nickname = nickname;
        this.skoUid = skoUid;
        this.extUid = extUid;
        this.source = source;
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
     * Returns the comment text
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment text
     *
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
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

}
