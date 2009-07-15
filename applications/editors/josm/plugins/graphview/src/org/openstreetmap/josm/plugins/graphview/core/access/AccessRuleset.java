package org.openstreetmap.josm.plugins.graphview.core.access;

import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.data.Tag;

/**
 * set of assumptions and implications for access;
 * will usually depend on local traffic laws
 */
public interface AccessRuleset {

	/**
	 * for a mode of transport, returns all transport categories it is a subset of.
	 * For example, the returned collection for "motorcycle" might include "motor_vehicle" and "vehicle".
	 *
	 * @param transportMode  mode of transport to get "supertypes" for; != null
	 * @return parameters superset categories, including the parameter itself,
	 *         in the order of decreasing specificness
	 *         empty if the parameter was no known mode of transport; != null
	 */
	public List<String> getAccessHierarchyAncestors(String transportMode);

	/**
	 * returns all base tags.
	 * Base tags are tags that make an object "eligible" for access evaluation
	 * (commonly things like highway=* or barrier=*)
	 */
	public Collection<Tag> getBaseTags();

	/**
	 * returns ruleset-specific implications
	 * @return  list of implications in the order they are expected to be applied; != null
	 */
	public List<Implication> getImplications();

}
