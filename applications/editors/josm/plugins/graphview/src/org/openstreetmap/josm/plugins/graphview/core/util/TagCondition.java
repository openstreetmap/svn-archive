package org.openstreetmap.josm.plugins.graphview.core.util;

import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

/**
 * condition for a collection of tags (such as "contains highway=*, but not access=no").
 * Used for implications.
 */
public interface TagCondition {

	/**
	 * returns true if the tags match the condition
	 *
	 * @param tags  tags to check against the condition; != null
	 */
	boolean matches(TagGroup tags);

}
