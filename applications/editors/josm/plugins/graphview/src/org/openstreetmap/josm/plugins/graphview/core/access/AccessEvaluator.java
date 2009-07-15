package org.openstreetmap.josm.plugins.graphview.core.access;

import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;

/**
 * determines the access rights for an object under certain conditions.
 * This interface is generic, so it can be used for different representations of ways and nodes
 * (e.g. internal JOSM representation).
 *
 * Generic implementations will usually use {@link DataSource} objects
 * to implement tag evaluation in an abstract way.
 *
 * @param <N>  type of the nodes
 * @param <W>  type of the ways
 */
public interface AccessEvaluator<N, W> {

	/**
	 * checks whether a way may be accessed in the given direction
	 *
	 * @param way                object to be checked; != null
	 * @param segmentProperties  map from road property types to their values for this way's
	 *                           segments; each value must be a valid value for its property type;
	 *                           != null
	 */
	public boolean wayUsable(W way, boolean forward,
			Map<RoadPropertyType<?>, Object> roadPropertyValues);

	/**
	 * checks whether a node may be accessed/passed
	 *
	 * @param node               object to be checked; != null
	 * @param segmentProperties  map from road property types to their values for SegmentNodes
	 *                           based on this node, each value must be a valid value for its
	 *                           property type; != null
	 */
	public boolean nodeUsable(N node, Map<RoadPropertyType<?>, Object> roadPropertyValues);

}
