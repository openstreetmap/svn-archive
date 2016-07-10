// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.core.data;

/**
 * represents a group of OSM tags (e.g. all tags of a way).
 * TagGroups are expected to be immutable, so modifying the tags means creation of a new group.
 * This interface requires that keys are unique, which is guaranteed since OSM API 0.6.
 */
public interface TagGroup extends Iterable<Tag> {

    /**
     * returns the value for the given key or null if no tag in this group uses that key
     * @param key  key whose value will be returned; != null
     */
    String getValue(String key);

    /**
     * returns true if this tag group contains a tag with the given key
     * @param key  key to check for; != null
     */
    boolean containsKey(String key);

    /**
     * returns true if this tag group contains at least one tag with the given value
     * @param value  value to check for; != null
     */
    boolean containsValue(String value);

    /**
     * returns true if this tag group contains the given tag
     * @param tag  tag to check for; != null
     */
    boolean contains(Tag tag);

    /**
     * returns the number of tags in this group
     */
    int size();
}
