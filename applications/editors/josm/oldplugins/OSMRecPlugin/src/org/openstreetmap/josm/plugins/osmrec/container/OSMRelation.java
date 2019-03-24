// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class containing information about the OSM relations.
 *
 * @author imis-nkarag
 */

public class OSMRelation implements Serializable {

    private String id;
    private Set<Integer> classIDs;
    private final List<String> memberReferences = new ArrayList<>();
    private final Map<String, String> tags = new HashMap<>();

    public String getID() {
        return id;
    }

    public List<String> getMemberReferences() {
        return memberReferences;
    }

    public Set<Integer> getClassIDs() {
        return this.classIDs;
    }

    public Map<String, String> getTagKeyValue() {
        return tags;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setClassIDs(Set<Integer> classIDs) {
        this.classIDs = classIDs;
    }

    public void addMemberReference(String memberReference) {
        memberReferences.add(memberReference);
    }

    public void setTagKeyValue(String tagKey, String tagValue) {
        this.tags.put(tagKey, tagValue);
    }
}
