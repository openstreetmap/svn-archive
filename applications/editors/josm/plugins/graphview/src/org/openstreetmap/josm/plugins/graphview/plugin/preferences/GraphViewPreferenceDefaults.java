package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

import static org.openstreetmap.josm.plugins.graphview.core.access.AccessType.DESIGNATED;
import static org.openstreetmap.josm.plugins.graphview.core.access.AccessType.PERMISSIVE;
import static org.openstreetmap.josm.plugins.graphview.core.access.AccessType.UNDEFINED;
import static org.openstreetmap.josm.plugins.graphview.core.access.AccessType.YES;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.VehiclePropertyStringParser.PropertyValueSyntaxException;

/**
 * utility class generating the default preferences
 */
public final class GraphViewPreferenceDefaults {

	/** prevents instantiation */
	private GraphViewPreferenceDefaults() { }

	/** creates a default "empty" bookmark */
	public static PreferenceAccessParameters createDefaultBookmarkAccessParameters() {

		Collection<AccessType> accessTypes =
			Arrays.asList(UNDEFINED, YES, PERMISSIVE, DESIGNATED);

		Map<VehiclePropertyType<?>, String> propertyStringMap =
			new HashMap<VehiclePropertyType<?>, String>();

		try {
			return new PreferenceAccessParameters("", accessTypes, propertyStringMap);
		} catch (PropertyValueSyntaxException e) {
			throw new AssertionError(e);
		}

	}

	/** creates the default map of access parameter bookmarks */
	public static Map<String, PreferenceAccessParameters> createDefaultAccessParameterBookmarks() {

		try {

			Map<String, PreferenceAccessParameters> result =
				new HashMap<String, PreferenceAccessParameters>();

			Collection<AccessType> accessTypes =
				Arrays.asList(UNDEFINED, YES, PERMISSIVE, DESIGNATED);

			/* create motorcar bookmark */
			{
				Map<VehiclePropertyType<?>, String> propertyMap =
					new HashMap<VehiclePropertyType<?>, String>();

				PreferenceAccessParameters accessParameters =
					new PreferenceAccessParameters("motorcar", accessTypes, propertyMap);

				result.put("motorcar", accessParameters);
			}

			/* create hgv bookmark */
			{
				Map<VehiclePropertyType<?>, String> propertyMap =
					new HashMap<VehiclePropertyType<?>, String>();
				propertyMap.put(VehiclePropertyTypes.WEIGHT, "3.5");

				PreferenceAccessParameters accessParameters =
					new PreferenceAccessParameters("hgv", accessTypes, propertyMap);

				result.put("hgv (3.5 t)", accessParameters);
			}

			/* create bicycle bookmark */
			{
				Map<VehiclePropertyType<?>, String> propertyMap =
					new HashMap<VehiclePropertyType<?>, String>();

				PreferenceAccessParameters accessParameters =
					new PreferenceAccessParameters("bicycle", accessTypes, propertyMap);

				result.put("bicycle", accessParameters);
			}

			/* create pedestrian bookmark */
			{
				Map<VehiclePropertyType<?>, String> propertyMap =
					new HashMap<VehiclePropertyType<?>, String>();

				PreferenceAccessParameters accessParameters =
					new PreferenceAccessParameters("foot", accessTypes, propertyMap);

				result.put("pedestrian", accessParameters);
			}

			return result;

		} catch (PropertyValueSyntaxException e) {
			throw new AssertionError(e);
		}
	}

	public static File getDefaultRulesetFolder() {
		return new File(System.getProperty("user.home"));
	}

}
