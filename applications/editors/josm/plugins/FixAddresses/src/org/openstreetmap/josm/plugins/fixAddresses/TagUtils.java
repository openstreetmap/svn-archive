/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Contains the tags used within OSM. FIXME: Maybe there is a class or similar
 * within JOSM which already defines them, but I have not found it so far.
 *
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public final class TagUtils {
	private static String COUNTRIES_REQUIRE_STATE[] = {
		"en_US",    /* USA */
		"en_AU" /* Australia */
	};

	/**
	 * Checks if the given OSM object has a (non-empty) value for the given tag.
	 *
	 * @param osm the osm object to inspect.
	 * @param tag the tag to look for.
	 * @return true, if osm object has a non-empty value for this tag
	 */
	public static boolean hasTag(OsmPrimitive osm, String tag) {
		return osm != null && !StringUtils.isNullOrEmpty(osm.get(tag));
	}

	/**
	 * Checks if the given OSM primitive is an address node.
	 * @return
	 */
	public static boolean isAddress(OsmPrimitive osmObject) {
		return  TagUtils.hasAddrCityTag(osmObject) || TagUtils.hasAddrCountryTag(osmObject) ||
				TagUtils.hasAddrHousenumberTag(osmObject) || TagUtils.hasAddrPostcodeTag(osmObject) ||
				TagUtils.hasAddrStateTag(osmObject) || TagUtils.hasAddrStreetTag(osmObject);
	}

	/**
	 * Check if OSM primitive has a tag 'parking'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasParkingTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(PARKING_TAG) : false;
	}

	/**
	 * Gets the value of tag 'parking'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getParkingValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(PARKING_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'shop'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasShopTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SHOP_TAG) : false;
	}

	/**
	 * Gets the value of tag 'shop'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getShopValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SHOP_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'craft'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCraftTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CRAFT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'craft'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCraftValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CRAFT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'surface'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSurfaceTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SURFACE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'surface'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSurfaceValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SURFACE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'cuisine'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCuisineTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CUISINE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'cuisine'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCuisineValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CUISINE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'wood'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWoodTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WOOD_TAG) : false;
	}

	/**
	 * Gets the value of tag 'wood'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWoodValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WOOD_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'foot'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasFootTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(FOOT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'foot'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getFootValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(FOOT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'name:de'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNameDeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NAME_DE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'name:de'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNameDeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NAME_DE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'nat_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNatRefTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NAT_REF_TAG) : false;
	}

	/**
	 * Gets the value of tag 'nat_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNatRefValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NAT_REF_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'note:de'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNoteDeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NOTE_DE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'note:de'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNoteDeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NOTE_DE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:street'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrStreetTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_STREET_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:street'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrStreetValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_STREET_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'type'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasTypeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(TYPE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'type'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getTypeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(TYPE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:city'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrCityTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_CITY_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:city'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrCityValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_CITY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'boundary'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBoundaryTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BOUNDARY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'boundary'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBoundaryValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BOUNDARY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'smoothness'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSmoothnessTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SMOOTHNESS_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'smoothness'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSmoothnessValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SMOOTHNESS_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'opening_hours'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasOpeningHoursTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(OPENING_HOURS_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'opening_hours'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getOpeningHoursValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(OPENING_HOURS_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'bicycle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBicycleTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BICYCLE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'bicycle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBicycleValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BICYCLE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'religion'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasReligionTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(RELIGION_TAG) : false;
	}

	/**
	 * Gets the value of tag 'religion'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getReligionValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(RELIGION_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'barrier'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBarrierTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BARRIER_TAG) : false;
	}

	/**
	 * Gets the value of tag 'barrier'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBarrierValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BARRIER_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'power'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasPowerTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(POWER_TAG) : false;
	}

	/**
	 * Gets the value of tag 'power'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getPowerValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(POWER_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'landuse'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLanduseTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LANDUSE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'landuse'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLanduseValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LANDUSE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'fireplace'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasFireplaceTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(FIREPLACE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'fireplace'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getFireplaceValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(FIREPLACE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'int_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasIntRefTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(INT_REF_TAG) : false;
	}

	/**
	 * Gets the value of tag 'int_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getIntRefValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(INT_REF_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'whitewater:section_grade'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWhitewaterSectionGradeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive
				.hasKey(WHITEWATER_SECTION_GRADE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'whitewater:section_grade'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWhitewaterSectionGradeValue(
			OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive
				.get(WHITEWATER_SECTION_GRADE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'denomination'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasDenominationTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(DENOMINATION_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'denomination'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getDenominationValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(DENOMINATION_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:postcode'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrPostcodeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_POSTCODE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:postcode'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrPostcodeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_POSTCODE_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'wires'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWiresTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WIRES_TAG) : false;
	}

	/**
	 * Gets the value of tag 'wires'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWiresValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WIRES_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'loc_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLocRefTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LOC_REF_TAG) : false;
	}

	/**
	 * Gets the value of tag 'loc_ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLocRefValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LOC_REF_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'width'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWidthTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WIDTH_TAG) : false;
	}

	/**
	 * Gets the value of tag 'width'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWidthValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WIDTH_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'tourism'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasTourismTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(TOURISM_TAG) : false;
	}

	/**
	 * Gets the value of tag 'tourism'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getTourismValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(TOURISM_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'leisure'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLeisureTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LEISURE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'leisure'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLeisureValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LEISURE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'electrified'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasElectrifiedTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ELECTRIFIED_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'electrified'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getElectrifiedValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ELECTRIFIED_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'junction'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasJunctionTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(JUNCTION_TAG) : false;
	}

	/**
	 * Gets the value of tag 'junction'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getJunctionValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(JUNCTION_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'railway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasRailwayTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(RAILWAY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'railway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getRailwayValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(RAILWAY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'voltage'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasVoltageTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(VOLTAGE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'voltage'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getVoltageValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(VOLTAGE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'bridge'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBridgeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BRIDGE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'bridge'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBridgeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BRIDGE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'motor_vehicle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasMotorVehicleTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(MOTOR_VEHICLE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'motor_vehicle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getMotorVehicleValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(MOTOR_VEHICLE_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'comment'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCommentTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(COMMENT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'comment'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCommentValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(COMMENT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'maxspeed'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasMaxspeedTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(MAXSPEED_TAG) : false;
	}

	/**
	 * Gets the value of tag 'maxspeed'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getMaxspeedValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(MAXSPEED_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'natural'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNaturalTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NATURAL_TAG) : false;
	}

	/**
	 * Gets the value of tag 'natural'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNaturalValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NATURAL_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'sac_scale'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSacScaleTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SAC_SCALE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'sac_scale'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSacScaleValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SAC_SCALE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'tunnel'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasTunnelTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(TUNNEL_TAG) : false;
	}

	/**
	 * Gets the value of tag 'tunnel'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getTunnelValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(TUNNEL_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'waterway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWaterwayTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WATERWAY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'waterway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWaterwayValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WATERWAY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'trail_visibility'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasTrailVisibilityTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(TRAIL_VISIBILITY_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'trail_visibility'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getTrailVisibilityValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(TRAIL_VISIBILITY_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'highway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasHighwayTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(HIGHWAY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'highway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getHighwayValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(HIGHWAY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'vehicle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasVehicleTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(VEHICLE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'vehicle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getVehicleValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(VEHICLE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'horse'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasHorseTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(HORSE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'horse'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getHorseValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(HORSE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'goods'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasGoodsTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(GOODS_TAG) : false;
	}

	/**
	 * Gets the value of tag 'goods'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getGoodsValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(GOODS_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'frequency'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasFrequencyTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(FREQUENCY_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'frequency'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getFrequencyValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(FREQUENCY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'man_made'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasManMadeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(MAN_MADE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'man_made'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getManMadeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(MAN_MADE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:housenumber'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrHousenumberTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_HOUSENUMBER_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:housenumber'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrHousenumberValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_HOUSENUMBER_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:housename'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrHousenameTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_HOUSENAME_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:housename'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrHousenameValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_HOUSENAME_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'area'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAreaTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(AREA_TAG) : false;
	}

	/**
	 * Gets the value of tag 'area'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAreaValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(AREA_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'building:levels'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBuildingLevelsTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BUILDING_LEVELS_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'building:levels'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBuildingLevelsValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BUILDING_LEVELS_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'wheelchair'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWheelchairTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WHEELCHAIR_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'wheelchair'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWheelchairValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WHEELCHAIR_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'name'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNameTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NAME_TAG) : false;
	}

	/**
	 * Gets the value of tag 'name'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNameValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NAME_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'oneway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasOnewayTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ONEWAY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'oneway'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getOnewayValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ONEWAY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'FIXME'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasFIXMETag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(FIXME_TAG) : false;
	}

	/**
	 * Gets the value of tag 'FIXME'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getFIXMEValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(FIXME_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'capacity'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCapacityTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CAPACITY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'capacity'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCapacityValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CAPACITY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'motorcycle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasMotorcycleTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(MOTORCYCLE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'motorcycle'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getMotorcycleValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(MOTORCYCLE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'hgv'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasHgvTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(HGV_TAG) : false;
	}

	/**
	 * Gets the value of tag 'hgv'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getHgvValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(HGV_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'construction'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasConstructionTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CONSTRUCTION_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'construction'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getConstructionValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CONSTRUCTION_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:state'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrStateTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_STATE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:state'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrStateValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_STATE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'lanes'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLanesTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LANES_TAG) : false;
	}

	/**
	 * Gets the value of tag 'lanes'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLanesValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LANES_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'note'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNoteTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NOTE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'note'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNoteValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NOTE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'lit'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLitTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LIT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'lit'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLitValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LIT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'building'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasBuildingTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(BUILDING_TAG) : false;
	}

	/**
	 * Gets the value of tag 'building'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getBuildingValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(BUILDING_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'segregated'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSegregatedTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SEGREGATED_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'segregated'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSegregatedValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SEGREGATED_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:inclusion'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrInclusionTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_INCLUSION_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:inclusion'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrInclusionValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_INCLUSION_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'layer'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasLayerTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(LAYER_TAG) : false;
	}

	/**
	 * Gets the value of tag 'layer'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getLayerValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(LAYER_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'sport'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSportTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SPORT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'sport'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSportValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SPORT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:interpolation'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrInterpolationTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive
				.hasKey(ADDR_INTERPOLATION_TAG) : false;
	}

	/**
	 * Gets the value of tag 'addr:interpolation'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrInterpolationValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_INTERPOLATION_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'cutting'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCuttingTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CUTTING_TAG) : false;
	}

	/**
	 * Gets the value of tag 'cutting'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCuttingValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CUTTING_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'amenity'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAmenityTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(AMENITY_TAG) : false;
	}

	/**
	 * Gets the value of tag 'amenity'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAmenityValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(AMENITY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'access'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAccessTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ACCESS_TAG) : false;
	}

	/**
	 * Gets the value of tag 'access'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAccessValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ACCESS_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'agricultural'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAgriculturalTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(AGRICULTURAL_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'agricultural'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAgriculturalValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(AGRICULTURAL_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'capacity:disabled'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCapacityDisabledTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive
				.hasKey(CAPACITY_DISABLED_TAG) : false;
	}

	/**
	 * Gets the value of tag 'capacity:disabled'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCapacityDisabledValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CAPACITY_DISABLED_TAG)
				: null;
	}

	/**
	 * Check if OSM primitive has a tag 'operator'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasOperatorTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(OPERATOR_TAG) : false;
	}

	/**
	 * Gets the value of tag 'operator'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getOperatorValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(OPERATOR_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasRefTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(REF_TAG) : false;
	}

	/**
	 * Gets the value of tag 'ref'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getRefValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(REF_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'noexit'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasNoexitTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(NOEXIT_TAG) : false;
	}

	/**
	 * Gets the value of tag 'noexit'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getNoexitValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(NOEXIT_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'admin_level'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAdminLevelTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADMIN_LEVEL_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'admin_level'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAdminLevelValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADMIN_LEVEL_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'source'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasSourceTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SOURCE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'source'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getSourceValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SOURCE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'tracktype'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasTracktypeTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(TRACKTYPE_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'tracktype'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getTracktypeValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(TRACKTYPE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'addr:country'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasAddrCountryTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_COUNTRY_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'addr:country'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getAddrCountryValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ADDR_COUNTRY_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'route'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasRouteTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(ROUTE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'route'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getRouteValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(ROUTE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'cables'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasCablesTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(CABLES_TAG) : false;
	}

	/**
	 * Gets the value of tag 'cables'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getCablesValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(CABLES_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'service'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasServiceTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(SERVICE_TAG) : false;
	}

	/**
	 * Gets the value of tag 'service'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getServiceValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(SERVICE_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'motorcar'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasMotorcarTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(MOTORCAR_TAG) : false;
	}

	/**
	 * Gets the value of tag 'motorcar'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getMotorcarValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(MOTORCAR_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'whitewater'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasWhitewaterTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(WHITEWATER_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'whitewater'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getWhitewaterValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(WHITEWATER_TAG) : null;
	}

	/**
	 * Check if OSM primitive has a tag 'embankment'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean hasEmbankmentTag(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.hasKey(EMBANKMENT_TAG)
				: false;
	}

	/**
	 * Gets the value of tag 'embankment'.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static String getEmbankmentValue(OsmPrimitive osmPrimitive) {
		return osmPrimitive != null ? osmPrimitive.get(EMBANKMENT_TAG) : null;
	}

	/**
	 * Checks if the given street supporting housenumbers. Usually motor ways and primary roads have
	 * no addresses, also no paths or tracks.
	 *
	 * @param w the w
	 * @return true, if is street supporting housenumbers
	 */
	public static boolean isStreetSupportingHousenumbers(Way w) {
		if (w == null) return false;
		if (!hasHighwayTag(w)) {
			return false;
		}

		// TODO: Should be configurable

		/* Allow everything until this can be configured */
		return true;
		/*
		String hwType = getHighwayValue(w);
		return  !(TagUtils.HIGHWAY_MOTORWAY_LINK_VALUE.equals(hwType) ||
				TagUtils.HIGHWAY_MOTORWAY_VALUE.equals(hwType) ||
				TagUtils.HIGHWAY_FOOTWAY_VALUE.equals(hwType) ||
				TagUtils.HIGHWAY_TRACK_VALUE.equals(hwType)
				);*/
	}

	// Relation support

	/**
	 * Check if OSM relation is a 'associatedStreet' relation.
	 *
	 * @param osmPrimitive
	 *            The OSM entity to check.
	 */
	public static boolean isAssociatedStreetRelation(Relation rel) {
		return rel != null &&
			rel.hasKey(RELATION_TYPE) &&
			ASSOCIATEDSTREET_RELATION_TYPE.equals(rel.get(RELATION_TYPE));
	}

	/**
	 * Checks if given relation member has role "street".
	 *
	 * @param relMember the relation member
	 * @return true, if is street member
	 */
	public static boolean isStreetMember(RelationMember relMember) {
		return relMember != null && STREET_RELATION_ROLE.equals(relMember.getRole());
	}

	/**
	 * Checks if given relation member has role "house".
	 *
	 * @param relMember the relation member
	 * @return true, if is street member
	 */
	public static boolean isHouseMember(RelationMember relMember) {
		return relMember != null && STREET_RELATION_ROLE.equals(relMember.getRole());
	}

	/**
	 * Checks if "addr:state" tag is required.
	 *
	 * @return true, if is state required
	 */
	public static boolean isStateRequired() {
		String loc = OsmUtils.getLocale();

		for (int i = 0; i < COUNTRIES_REQUIRE_STATE.length; i++) {
			if (COUNTRIES_REQUIRE_STATE[i].equals(loc)) {
				return true;
			}
		}

		return false;
	}

	public static final String PARKING_TAG = "parking";
	public static final String SHOP_TAG = "shop";
	public static final String CRAFT_TAG = "craft";
	public static final String SURFACE_TAG = "surface";
	public static final String CUISINE_TAG = "cuisine";
	public static final String WOOD_TAG = "wood";
	public static final String FOOT_TAG = "foot";
	public static final String NAME_DE_TAG = "name:de";
	public static final String NAT_REF_TAG = "nat_ref";
	public static final String NOTE_DE_TAG = "note:de";
	public static final String ADDR_STREET_TAG = "addr:street";
	public static final String TYPE_TAG = "type";
	public static final String ADDR_CITY_TAG = "addr:city";
	public static final String BOUNDARY_TAG = "boundary";
	public static final String SMOOTHNESS_TAG = "smoothness";
	public static final String OPENING_HOURS_TAG = "opening_hours";
	public static final String BICYCLE_TAG = "bicycle";
	public static final String RELIGION_TAG = "religion";
	public static final String BARRIER_TAG = "barrier";
	public static final String POWER_TAG = "power";
	public static final String LANDUSE_TAG = "landuse";
	public static final String FIREPLACE_TAG = "fireplace";
	public static final String INT_REF_TAG = "int_ref";
	public static final String WHITEWATER_SECTION_GRADE_TAG = "whitewater:section_grade";
	public static final String DENOMINATION_TAG = "denomination";
	public static final String ADDR_POSTCODE_TAG = "addr:postcode";
	public static final String WIRES_TAG = "wires";
	public static final String LOC_REF_TAG = "loc_ref";
	public static final String WIDTH_TAG = "width";
	public static final String TOURISM_TAG = "tourism";
	public static final String LEISURE_TAG = "leisure";
	public static final String ELECTRIFIED_TAG = "electrified";
	public static final String JUNCTION_TAG = "junction";
	public static final String RAILWAY_TAG = "railway";
	public static final String VOLTAGE_TAG = "voltage";
	public static final String BRIDGE_TAG = "bridge";
	public static final String MOTOR_VEHICLE_TAG = "motor_vehicle";
	public static final String COMMENT_TAG = "comment";
	public static final String MAXSPEED_TAG = "maxspeed";
	public static final String NATURAL_TAG = "natural";
	public static final String BUILDING_HEIGHT_TAG = "building:height";
	public static final String SAC_SCALE_TAG = "sac_scale";
	public static final String TUNNEL_TAG = "tunnel";
	public static final String WATERWAY_TAG = "waterway";
	public static final String TRAIL_VISIBILITY_TAG = "trail_visibility";
	public static final String HIGHWAY_TAG = "highway";
	public static final String VEHICLE_TAG = "vehicle";
	public static final String HORSE_TAG = "horse";
	public static final String GOODS_TAG = "goods";
	public static final String FREQUENCY_TAG = "frequency";
	public static final String MAN_MADE_TAG = "man_made";
	public static final String ADDR_HOUSENUMBER_TAG = "addr:housenumber";
	public static final String AREA_TAG = "area";
	public static final String BUILDING_LEVELS_TAG = "building:levels";
	public static final String WHEELCHAIR_TAG = "wheelchair";
	public static final String NAME_TAG = "name";
	public static final String ONEWAY_TAG = "oneway";
	public static final String FIXME_TAG = "FIXME";
	public static final String CAPACITY_TAG = "capacity";
	public static final String MOTORCYCLE_TAG = "motorcycle";
	public static final String HGV_TAG = "hgv";
	public static final String CONSTRUCTION_TAG = "construction";
	public static final String ADDR_STATE_TAG = "addr:state";
	public static final String LANES_TAG = "lanes";
	public static final String NOTE_TAG = "note";
	public static final String LIT_TAG = "lit";
	public static final String BUILDING_TAG = "building";
	public static final String SEGREGATED_TAG = "segregated";
	public static final String ADDR_INCLUSION_TAG = "addr:inclusion";
	public static final String LAYER_TAG = "layer";
	public static final String SPORT_TAG = "sport";
	public static final String ADDR_INTERPOLATION_TAG = "addr:interpolation";
	public static final String CUTTING_TAG = "cutting";
	public static final String AMENITY_TAG = "amenity";
	public static final String ACCESS_TAG = "access";
	public static final String AGRICULTURAL_TAG = "agricultural";
	public static final String CAPACITY_DISABLED_TAG = "capacity:disabled";
	public static final String OPERATOR_TAG = "operator";
	public static final String REF_TAG = "ref";
	public static final String NOEXIT_TAG = "noexit";
	public static final String ADMIN_LEVEL_TAG = "admin_level";
	public static final String SOURCE_TAG = "source";
	public static final String TRACKTYPE_TAG = "tracktype";
	public static final String ADDR_COUNTRY_TAG = "addr:country";
	public static final String ROUTE_TAG = "route";
	public static final String CABLES_TAG = "cables";
	public static final String SERVICE_TAG = "service";
	public static final String MOTORCAR_TAG = "motorcar";
	public static final String WHITEWATER_TAG = "whitewater";
	public static final String EMBANKMENT_TAG = "embankment";
	public static final String ADDR_HOUSENAME_TAG = "addr:housename";

	/* Highway types */
	public static final String HIGHWAY_CYCLEWAY_VALUE = "cycleway";
	public static final String HIGHWAY_FOOTWAY_VALUE = "footway";
	public static final String HIGHWAY_MOTORWAY_LINK_VALUE = "motorway_link";
	public static final String HIGHWAY_MOTORWAY_VALUE = "motorway";
	public static final String HIGHWAY_PATH_VALUE = "path";
	public static final String HIGHWAY_RESIDENTIAL_VALUE = "residential";
	public static final String HIGHWAY_LIVING_STREET_VALUE = "living_street";
	public static final String HIGHWAY_ROAD_VALUE = "road";
	public static final String HIGHWAY_SECONDARY_VALUE = "secondary";
	public static final String HIGHWAY_SERVICE_VALUE = "service";
	public static final String HIGHWAY_STEPS_VALUE = "steps";
	public static final String HIGHWAY_TERTIARY_VALUE = "tertiary";
	public static final String HIGHWAY_TRACK_VALUE = "track";
	public static final String HIGHWAY_TRUNK_LINK_VALUE = "trunk_link";
	public static final String HIGHWAY_TRUNK_VALUE = "trunk";
	public static final String HIGHWAY_UNCLASSIFIED_VALUE = "unclassified";

	/* Relation keys */

	// Associated street: See http://wiki.openstreetmap.org/wiki/Proposed_features/De:Hausnummern
	public static final String RELATION_TYPE = "type";
	public static final String ASSOCIATEDSTREET_RELATION_TYPE = "associatedStreet";
	public static final String STREET_RELATION_ROLE = "street";
	public static final String HOUSE_RELATION_ROLE = "house";
}
