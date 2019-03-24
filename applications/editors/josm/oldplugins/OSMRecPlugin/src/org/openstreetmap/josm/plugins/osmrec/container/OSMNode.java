// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.container;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Class containing information about the OSM nodes.
 * @author imis-nkarag
 */
public class OSMNode {

    private String id;
    private String action; //e.g modify
    private String visible;
    private Geometry geometry;
    private String timestamp;
    private String uid;
    private String user;
    private String version;
    private String changeset;
    private final Map<String, String> tags = new HashMap<>();

    //attribute getters
    public String getID() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getVisible() {
        return visible;
    }

    public Geometry getGeometry() {
        return this.geometry;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public String getVersion() {
        return version;
    }

    public String getChangeset() {
        return changeset;
    }

    public Map<String, String> getTagKeyValue() {
        return tags;
    }

    //attributes setters
    public void setID(String id) {
        this.id = id;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setVisible(String visible) {
        this.visible = visible;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setChangeset(String changeset) {
        this.changeset = changeset;
    }

    public void setTagKeyValue(String tagKey, String tagValue) {
        this.tags.put(tagKey, tagValue);
    }

}
