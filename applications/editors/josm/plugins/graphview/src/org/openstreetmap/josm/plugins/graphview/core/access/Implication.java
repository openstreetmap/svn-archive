package org.openstreetmap.josm.plugins.graphview.core.access;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.data.MapBasedTagGroup;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.util.TagCondition;

/**
 * immutable representation of a tag implication rule.
 */
public final class Implication {

	private final TagCondition condition;
	private final Collection<Tag> impliedTags;

	public Implication(TagCondition condition, Collection<Tag> impliedTags) {
		this.condition = condition;
		this.impliedTags = impliedTags;
	}

	/**
	 * applies this implication to a tag group.
	 * The resulting tag group will contain all tags from the original group
	 * and all implied tags with a key that didn't occur in the original group.
	 *
	 * @param tags  tag group to apply implications to; != null
	 */
	public TagGroup apply(TagGroup tags) {

		if (condition.matches(tags)) {

			Map<String, String> newTagMap = new HashMap<String, String>();

			for (Tag tag : tags) {
				newTagMap.put(tag.key, tag.value);
			}

			for (Tag impliedTag : impliedTags) {
				if (!newTagMap.containsKey(impliedTag.key)) {
					newTagMap.put(impliedTag.key, impliedTag.value);
				}
			}

			return new MapBasedTagGroup(newTagMap);

		} else {
			return tags;
		}

	}

	@Override
	public String toString() {
		return condition.toString() + " => " + impliedTags.toString();
	}

}
