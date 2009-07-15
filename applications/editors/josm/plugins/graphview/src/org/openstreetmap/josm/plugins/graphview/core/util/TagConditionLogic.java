package org.openstreetmap.josm.plugins.graphview.core.util;

import java.util.Collection;

import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

/**
 * utility class for creating and combining TagCondition objects.
 */
public final class TagConditionLogic {

	/** prevents instantiation */
	private TagConditionLogic(){ }

	/**
	 * creates a condition that is fulfilled if the set of tags contains a given tag
	 *
	 * @param tag  tag that must be in the tag collection; != null
	 */
	public static TagCondition tag(final Tag tag) {
		assert tag != null;
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				return tags.contains(tag);
			}
			@Override
			public String toString() {
				return tag.toString();
			}
		};
	}

	/**
	 * creates a condition that is fulfilled if the set of tags contains a tag with the given key
	 *
	 * @param key  the key to look for; != null
	 */
	public static TagCondition key(final String key) {
		assert key != null;
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				return tags.containsKey(key);
			}
			@Override
			public String toString() {
				return key;
			}
		};
	}

	/**
	 * combines conditions using a boolean "and"
	 *
	 * @param condition   first condition; != null
	 * @param conditions  more conditions; each != null
	 */
	public static TagCondition and(final TagCondition condition, final TagCondition... conditions) {
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				for (TagCondition c : conditions) {
					if (!c.matches(tags)) {
						return false;
					}
				}
				return condition.matches(tags);
			}
			@Override
			public String toString() {
				StringBuilder result = new StringBuilder();
				result.append("(");
				result.append(condition);
				for (TagCondition c : conditions) {
					result.append(" && ");
					result.append(c);
				}
				result.append(")");
				return result.toString();
			}
		};
	}

	/**
	 * combines conditions using a boolean "and"
	 *
	 * @param conditions   collection of conditions, must contain at least one element; != null
	 */
	public static TagCondition and(final Collection<TagCondition> conditions) {
		if (conditions.size() == 0) {
			throw new IllegalArgumentException("collection must contain at least one condition");
		}
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				for (TagCondition c : conditions) {
					if (!c.matches(tags)) {
						return false;
					}
				}
				return true;
			}
			@Override
			public String toString() {
				StringBuilder result = new StringBuilder();
				result.append("(");
				boolean firstCondition = true;
				for (TagCondition c : conditions) {
					if (!firstCondition) {
						result.append(" && ");
					}
					firstCondition = false;
					result.append(c);
				}
				result.append(")");
				return result.toString();
			}
		};
	}

	/**
	 * combines conditions using a boolean "or"
	 *
	 * @param condition   first condition; != null
	 * @param conditions  more conditions; each != null
	 */
	public static TagCondition or(final TagCondition condition, final TagCondition... conditions) {
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				for (TagCondition c : conditions) {
					if (c.matches(tags)) {
						return true;
					}
				}
				return condition.matches(tags);
			}
			@Override
			public String toString() {
				StringBuilder result = new StringBuilder();
				result.append("(");
				result.append(condition);
				for (TagCondition c : conditions) {
					result.append(" || ");
					result.append(c);
				}
				result.append(")");
				return result.toString();
			}
		};
	}

	/**
	 * combines conditions using a boolean "or"
	 *
	 * @param conditions   collection of conditions, must contain at least one element; != null
	 */
	public static TagCondition or(final Collection<TagCondition> conditions) {
		if (conditions.size() == 0) {
			throw new IllegalArgumentException("collection must contain at least one condition");
		}
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				for (TagCondition c : conditions) {
					if (c.matches(tags)) {
						return true;
					}
				}
				return false;
			}
			@Override
			public String toString() {
				StringBuilder result = new StringBuilder();
				result.append("(");
				boolean firstCondition = true;
				for (TagCondition c : conditions) {
					if (!firstCondition) {
						result.append(" || ");
					}
					firstCondition = false;
					result.append(c);
				}
				result.append(")");
				return result.toString();
			}
		};
	}

	/**
	 * inverts a condition
	 *
	 * @param condition  condition to invert, != null
	 */
	public static TagCondition not(final TagCondition condition) {
		return new TagCondition() {
			public boolean matches(TagGroup tags) {
				return !condition.matches(tags);
			}
			@Override
			public String toString() {
				return "!" + condition;
			}
		};
	}

}
