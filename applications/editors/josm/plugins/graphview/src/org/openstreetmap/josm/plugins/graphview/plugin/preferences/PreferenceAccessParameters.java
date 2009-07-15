package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.access.AccessType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;

/**
 * implementation of AccessParameters that stores not only property values,
 * but also the original property Strings just like the user entered them.
 */
public class PreferenceAccessParameters implements AccessParameters {

	private final String accessClass;
	private final Map<AccessType, Boolean> accessTypeUsableMap;
	private final Map<VehiclePropertyType<?>, String> vehiclePropertyStrings;
	private final Map<VehiclePropertyType<?>, Object> vehiclePropertyValues;

	public String getAccessClass() {
		return accessClass;
	}

	public boolean getAccessTypeUsable(AccessType accessType) {
		assert accessType != null;
		return accessTypeUsableMap.get(accessType);
	}

	public Collection<VehiclePropertyType<?>> getAvailableVehicleProperties() {
		return vehiclePropertyValues.keySet();
	}

	/**
	 * returns the value for a vehicle property.
	 *
	 * @param <D>              type of property value
	 * @param vehicleProperty  property to get value for; != null
	 * @return                 value for vehicleProperty, null if no value is available.
	 *                         Guaranteed to be valid according to vehicleProperty's
	 *                         {@link VehiclePropertyType#isValidValue(Object)} method.
	 */
	public <D> D getVehiclePropertyValue(VehiclePropertyType<D> vehicleProperty) {
		assert vehicleProperty != null;

		@SuppressWarnings("unchecked")
		D value = (D)vehiclePropertyValues.get(vehicleProperty);
		return value;
	}

	/**
	 * returns the unparsed String for a vehicle property.
	 *
	 * @param vehicleProperty  property to get String for; != null
	 * @return                 unparsed String, null if no value is available.
	 */
	public String getVehiclePropertyString(VehiclePropertyType<?> vehicleProperty) {
		assert vehicleProperty != null;

		return vehiclePropertyStrings.get(vehicleProperty);
	}

	/**
	 * @param vehiclePropertyStrings  map from vehicle properties to string representations
	 *                                that will be parsed using {@link VehiclePropertyStringParser}
	 *                                to get the property values; != null
	 *
	 * @throws VehiclePropertyStringParser.PropertyValueSyntaxException
	 *         if a String from vehiclePropertyStrings contains a syntax error
	 */
	public PreferenceAccessParameters(String accessClass,
			Collection<AccessType> usableAccessTypes,
			Map<VehiclePropertyType<?>, String> vehiclePropertyStrings)
	throws VehiclePropertyStringParser.PropertyValueSyntaxException {

		this.accessClass = accessClass;

		accessTypeUsableMap = new EnumMap<AccessType, Boolean>(AccessType.class);
		for (AccessType accessType : AccessType.values()) {
			accessTypeUsableMap.put(accessType, usableAccessTypes.contains(accessType));
		}

		/* check and use vehicle properties */

		this.vehiclePropertyStrings = Collections.unmodifiableMap(
				new HashMap<VehiclePropertyType<?>, String>(vehiclePropertyStrings));

		this.vehiclePropertyValues = new HashMap<VehiclePropertyType<?>, Object>();
		for (VehiclePropertyType<?> vehiclePropertyType : vehiclePropertyStrings.keySet()) {
			String propertyValueString = vehiclePropertyStrings.get(vehiclePropertyType);
			Object propertyValue = VehiclePropertyStringParser.parsePropertyValue(
					vehiclePropertyType, propertyValueString);
			this.vehiclePropertyValues.put(vehiclePropertyType, propertyValue);
		}

	}

}
