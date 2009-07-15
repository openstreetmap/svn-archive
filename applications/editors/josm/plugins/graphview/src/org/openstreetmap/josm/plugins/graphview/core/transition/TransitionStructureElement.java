package org.openstreetmap.josm.plugins.graphview.core.transition;

import java.util.Collection;

import org.openstreetmap.josm.plugins.graphview.core.property.RoadPropertyType;

/**
 * superinterface for {@link SegmentNode} and {@link Segment}; contains property related methods
 */
public interface TransitionStructureElement {

	/**
	 * returns the types of this object's properties
	 *
	 * @return  property type collection; != null
	 */
	public Collection<RoadPropertyType<?>> getAvailableProperties();

	/**
	 * returns the value of a property for this object
	 *
	 * @param propertyType   property type to return value for; != null
	 * @return property      value of the property for this segment; null if not available
	 */
	public <P> P getPropertyValue(RoadPropertyType<P> propertyType);

}
