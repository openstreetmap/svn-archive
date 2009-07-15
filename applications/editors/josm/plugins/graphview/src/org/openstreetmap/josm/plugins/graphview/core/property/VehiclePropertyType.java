package org.openstreetmap.josm.plugins.graphview.core.property;

/**
 * represents an aspect of a vehicle (such as its weight or length)
 * that can be used for comparisons with physical or legal limits of ways.
 *
 * VehiclePropertyType objects should be stateless (except for performance speedups).
 *
 * @param <V>  type of property values
 */
public interface VehiclePropertyType<V> {

	/**
	 * determines whether a value is valid.
	 * null is never a valid value and must not be used as parameter.
	 */
	public boolean isValidValue(Object value);

}
