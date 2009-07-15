package org.openstreetmap.josm.plugins.graphview.core.property;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

/**
 * abstract superclass for road property types that define a limit for a vehicle property
 */
abstract public class RoadValueLimit implements RoadPropertyType<Float> {

	protected static enum LimitType {MINIMUM, MAXIMUM};

	private final String keyName;
	private final VehiclePropertyType<Float> vehicleProperty;

	private final LimitType upperLimit;

	/**
	 * @param keyName          key that is used to add this property to a way; value must be in
	 *                         format readable by {@link ValueStringParser#parseOsmDecimal(String)};
	 *                         != null
	 * @param vehicleProperty  vehicle property that is limited by this road property; != null
	 * @param upperLimit       type of limit; != null
	 */
	protected RoadValueLimit(String keyName, VehiclePropertyType<Float> vehicleProperty,
			LimitType upperLimit) {
		assert keyName != null && vehicleProperty != null && upperLimit != null;

		this.keyName = keyName;
		this.vehicleProperty = vehicleProperty;
		this.upperLimit = upperLimit;
	}

	public <N, W, R> Float evaluateW(W way, boolean forward,
			AccessParameters accessParameters, DataSource<N, W, R> dataSource) {
		assert way != null && accessParameters != null && dataSource != null;
		return evaluateTags(dataSource.getTagsW(way));
	}

	public <N, W, R> Float evaluateN(N node,
			AccessParameters accessParameters, DataSource<N, W, R> dataSource) {
		assert node != null && accessParameters != null && dataSource != null;
		return evaluateTags(dataSource.getTagsN(node));
	}

	private final Float evaluateTags(TagGroup tags) {
		String valueString = tags.getValue(keyName);
		if (valueString != null) {
			Float value = ValueStringParser.parseOsmDecimal(valueString, false);
			return value;
		} else {
			return null;
		}
	}

	public boolean isUsable(Object propertyValue, AccessParameters accessParameters) {
		assert propertyValue instanceof Float;

		Float vehicleValue = accessParameters.getVehiclePropertyValue(vehicleProperty);

		if (vehicleValue != null) {
			switch(upperLimit) {
				case MINIMUM: return vehicleValue >= (Float) propertyValue;
				case MAXIMUM: return vehicleValue <= (Float) propertyValue;
				default:      throw new Error("unhandled LimitType");
			}
		} else {
			return true;
		}
	}

}
