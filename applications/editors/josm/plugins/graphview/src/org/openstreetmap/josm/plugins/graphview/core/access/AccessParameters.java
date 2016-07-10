// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.core.access;

import java.util.Collection;

import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;

/**
 * information that describes the vehicle and situation to evaluate access for.
 * AccessParameters should be immutable.
 */
public interface AccessParameters {

    String getAccessClass();

    /**
     * returns true iff a road with a given access type can be used
     * @param accessType  access type to check usablitly for; != null
     */
    boolean getAccessTypeUsable(AccessType accessType);

    /**
     * returns all {@link VehiclePropertyType}s a value is avaliable for.
     * The value can be accessed using {@link #getVehiclePropertyValue(VehiclePropertyType)}
     * @return  collection of property types; != null
     */
    Collection<VehiclePropertyType<?>> getAvailableVehicleProperties();

    /**
     * returns the value for a vehicle property.
     *
     * @param <V>              type of property value
     * @param vehicleProperty  property to get value for; != null
     * @return                 value for vehicleProperty, null if no value is available.
     *                         Guaranteed to be valid according to vehicleProperty's
     *                         {@link VehiclePropertyType#isValidValue(Object)} method.
     */
    <V> V getVehiclePropertyValue(VehiclePropertyType<V> vehicleProperty);
}
