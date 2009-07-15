package org.openstreetmap.josm.plugins.graphview.core.property;

import java.util.Collection;

/**
 * utility class with publicly available instances of {@link VehiclePropertyType} implementations.
 * The implementing classes themselves aren't available to ensure that only one instance exists.
 */
public final class VehiclePropertyTypes {

	/** prevents instantiation */
	private VehiclePropertyTypes() { }

	private static final class NonnegativeFloatProperty implements VehiclePropertyType<Float> {
		public boolean isValidValue(Object value) {
			return value instanceof Float && (Float)value >= 0;
		}
	}

	/** length of a vehicle in meters; negative values are invalid */
	public static final VehiclePropertyType<Float> LENGTH = new NonnegativeFloatProperty();

	/** width of a vehicle in meters; negative values are invalid */
	public static final VehiclePropertyType<Float> WIDTH = new NonnegativeFloatProperty();

	/** height of a vehicle in meters; negative values are invalid */
	public static final VehiclePropertyType<Float> HEIGHT = new NonnegativeFloatProperty();

	/** weight of a vehicle in tons; negative values are invalid */
	public static final VehiclePropertyType<Float> WEIGHT = new NonnegativeFloatProperty();

	/** axleload of a vehicle in tons; negative values are invalid */
	public static final VehiclePropertyType<Float> AXLELOAD = new NonnegativeFloatProperty();

	/** speed a vehicle can reach in km/h; negative values are invalid */
	public static final VehiclePropertyType<Float> SPEED = new NonnegativeFloatProperty();

	/** maximum incline a vehicle can go up; negative values are invalid */
	public static final VehiclePropertyType<Float> MAX_INCLINE_UP = new NonnegativeFloatProperty();

	/** maximum incline a vehicle can go down; negative values are invalid */
	public static final VehiclePropertyType<Float> MAX_INCLINE_DOWN = new NonnegativeFloatProperty();

	/** surface types ("surface" key values) the vehicle cannot use */
	public static final VehiclePropertyType<Collection<String>> SURFACE_BLACKLIST = new VehiclePropertyType<Collection<String>>() {
		public boolean isValidValue(Object value) {

			if (!(value instanceof Collection)) {
				return false;
			}

			for (Object contentObject : (Collection<?>)value) {
				if (!(contentObject instanceof String)) {
					return false;
				}
			}

			return true;
		}
	};

	/**
	 * maximum tracktype grade the vehicle can use;
	 * values are integers from = to 5
	 * (values of key "tracktype" without "grade_" prefix, 0 is for "none")
	 */
	public static final VehiclePropertyType<Integer> MAX_TRACKTYPE = new VehiclePropertyType<Integer>() {
		public boolean isValidValue(Object value) {
			return value instanceof Integer && (Integer)value >= 0 && (Integer)value <= 5;
		}
	};

}
