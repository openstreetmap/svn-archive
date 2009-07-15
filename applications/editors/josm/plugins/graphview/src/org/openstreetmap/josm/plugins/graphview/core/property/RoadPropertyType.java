package org.openstreetmap.josm.plugins.graphview.core.property;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;

/**
 * type of road property (as opposed to individual property values that can be identified using
 * a RoadPropertyType's evaluate methods).
 *
 * RoadPropertyType objects should be stateless (except for performance speedups).
 *
 * @param <V>  property value type
 */
public interface RoadPropertyType<V> {

	/**
	 * determines the property value for way-based segments.
	 * Uses the way the segment is created from.
	 *
	 * @param way               way that is to be evaluated; != null
	 * @param forward           chooses whether the property is evaluated
	 *                          in (true) or against (false) way direction
	 * @param accessParameters  access parameters for properties that depend on vehicle/situation
	 * @param dataSource        object providing access to all data; != null
	 * @return                  value of this property for the way;
	 *                          null if property cannot be determined / does not apply
	 */
	public <N, W, R> V evaluateW(W way, boolean forward,
			AccessParameters accessParameters, DataSource<N, W, R> dataSource);

	/**
	 * determines the property value for node-based segments.
	 * Uses the node the segment is created from.
	 *
	 * @param way               node that is to be evaluated; != null
	 * @param accessParameters  access parameters for properties that depend on vehicle/situation
	 * @param dataSource        object providing access to all data; != null
	 * @return                  value of this property for the way;
	 *                          null if property cannot be determined / does not apply
	 */
	public <N, W, R> V evaluateN(N node,
			AccessParameters accessParameters, DataSource<N, W, R> dataSource);

	/**
	 * checks whether a segment with a given value for this property can be used by a vehicle
	 * with a certain set of access parameters
	 *
	 * @param object  value of this property for the segment;
	 *                MUST be of type V; != null
	 */
	public boolean isUsable(Object object, AccessParameters accessParameters);

}
