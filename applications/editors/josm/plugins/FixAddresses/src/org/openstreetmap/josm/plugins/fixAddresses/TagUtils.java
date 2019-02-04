// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ACCESS_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_CITY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_COUNTRY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_HOUSENAME_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_HOUSENUMBER_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_INCLUSION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_INTERPOLATION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_POSTCODE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_STATE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADDR_STREET_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ADMIN_LEVEL_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.AGRICULTURAL_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.AMENITY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.AREA_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ASSOCIATEDSTREET_RELATION_TYPE;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BARRIER_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BICYCLE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BOUNDARY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BRIDGE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BUILDING_LEVELS_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.BUILDING_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CABLES_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CAPACITY_DISABLED_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CAPACITY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.COMMENT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CONSTRUCTION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.COUNTRIES_REQUIRE_STATE;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CRAFT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CUISINE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.CUTTING_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.DENOMINATION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ELECTRIFIED_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.EMBANKMENT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.FIREPLACE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.FIXME_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.FOOT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.FREQUENCY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.GOODS_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.HGV_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.HIGHWAY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.HORSE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.INT_REF_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.JUNCTION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LANDUSE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LANES_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LAYER_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LEISURE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LIT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.LOC_REF_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.MAN_MADE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.MAXSPEED_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.MOTORCAR_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.MOTORCYCLE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.MOTOR_VEHICLE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NAME_DE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NAME_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NATURAL_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NAT_REF_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NOEXIT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NOTE_DE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.NOTE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ONEWAY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.OPENING_HOURS_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.OPERATOR_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.PARKING_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.POWER_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.RAILWAY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.REF_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.RELATION_TYPE;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.RELIGION_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.ROUTE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SAC_SCALE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SEGREGATED_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SERVICE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SHOP_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SMOOTHNESS_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SOURCE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SPORT_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.STREET_RELATION_ROLE;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.SURFACE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.TOURISM_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.TRACKTYPE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.TRAIL_VISIBILITY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.TUNNEL_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.TYPE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.VEHICLE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.VOLTAGE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WATERWAY_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WHEELCHAIR_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WHITEWATER_SECTION_GRADE_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WHITEWATER_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WIDTH_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WIRES_TAG;
import static org.openstreetmap.josm.plugins.fixAddresses.TagConstants.WOOD_TAG;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

// CHECKSTYLE.OFF: MethodCountCheck

/**
 * Contains the tags used within OSM. FIXME: Maybe there is a class or similar
 * within JOSM which already defines them, but I have not found it so far.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public final class TagUtils {

    private TagUtils() {
        // Hide default constructor for utilities classes
    }

    /**
     * Checks if the given OSM object has a (non-empty) value for the given tag.
     * @param osm the osm object to inspect.
     * @param tag the tag to look for.
     * @return true, if osm object has a non-empty value for this tag
     */
    public static boolean hasTag(OsmPrimitive osm, String tag) {
        return osm != null && !StringUtils.isNullOrEmpty(osm.get(tag));
    }

    /**
     * Checks if the given OSM primitive is an address node.
     * @param osmObject OSM primitive
     * @return {@code true} if the given OSM primitive is an address node
     */
    public static boolean isAddress(OsmPrimitive osmObject) {
        return TagUtils.hasAddrCityTag(osmObject) || TagUtils.hasAddrCountryTag(osmObject) ||
               TagUtils.hasAddrHousenumberTag(osmObject) || TagUtils.hasAddrPostcodeTag(osmObject) ||
               TagUtils.hasAddrStateTag(osmObject) || TagUtils.hasAddrStreetTag(osmObject);
    }

    /**
     * Check if OSM primitive has a tag 'parking'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'parking'
     */
    public static boolean hasParkingTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(PARKING_TAG) : false;
    }

    /**
     * Gets the value of tag 'parking'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'parking'
     */
    public static String getParkingValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(PARKING_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'shop'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'shop'
     */
    public static boolean hasShopTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SHOP_TAG) : false;
    }

    /**
     * Gets the value of tag 'shop'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'shop'
     */
    public static String getShopValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SHOP_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'craft'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'craft'
     */
    public static boolean hasCraftTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CRAFT_TAG) : false;
    }

    /**
     * Gets the value of tag 'craft'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'craft'
     */
    public static String getCraftValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CRAFT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'surface'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'surface'
     */
    public static boolean hasSurfaceTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SURFACE_TAG) : false;
    }

    /**
     * Gets the value of tag 'surface'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'surface'
     */
    public static String getSurfaceValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SURFACE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'cuisine'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'cuisine'
     */
    public static boolean hasCuisineTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CUISINE_TAG) : false;
    }

    /**
     * Gets the value of tag 'cuisine'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'cuisine'
     */
    public static String getCuisineValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CUISINE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'wood'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'wood'
     */
    public static boolean hasWoodTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WOOD_TAG) : false;
    }

    /**
     * Gets the value of tag 'wood'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'wood'
     */
    public static String getWoodValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WOOD_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'foot'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'foot'
     */
    public static boolean hasFootTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(FOOT_TAG) : false;
    }

    /**
     * Gets the value of tag 'foot'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'foot'
     */
    public static String getFootValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(FOOT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'name:de'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'name:de'
     */
    public static boolean hasNameDeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NAME_DE_TAG) : false;
    }

    /**
     * Gets the value of tag 'name:de'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'name:de'
     */
    public static String getNameDeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NAME_DE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'nat_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'nat_ref'
     */
    public static boolean hasNatRefTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NAT_REF_TAG) : false;
    }

    /**
     * Gets the value of tag 'nat_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'nat_ref'
     */
    public static String getNatRefValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NAT_REF_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'note:de'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'note:de'
     */
    public static boolean hasNoteDeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NOTE_DE_TAG) : false;
    }

    /**
     * Gets the value of tag 'note:de'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'note:de'
     */
    public static String getNoteDeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NOTE_DE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:street'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:street'
     */
    public static boolean hasAddrStreetTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_STREET_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'addr:street'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:street'
     */
    public static String getAddrStreetValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_STREET_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'type'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'type'
     */
    public static boolean hasTypeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(TYPE_TAG) : false;
    }

    /**
     * Gets the value of tag 'type'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'type'
     */
    public static String getTypeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(TYPE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:city'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:city'
     */
    public static boolean hasAddrCityTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_CITY_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'addr:city'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:city'
     */
    public static String getAddrCityValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_CITY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'boundary'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'boundary'
     */
    public static boolean hasBoundaryTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BOUNDARY_TAG) : false;
    }

    /**
     * Gets the value of tag 'boundary'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'boundary'
     */
    public static String getBoundaryValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BOUNDARY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'smoothness'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'smoothness'
     */
    public static boolean hasSmoothnessTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SMOOTHNESS_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'smoothness'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'smoothness'
     */
    public static String getSmoothnessValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SMOOTHNESS_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'opening_hours'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'opening_hours'
     */
    public static boolean hasOpeningHoursTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(OPENING_HOURS_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'opening_hours'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'opening_hours'
     */
    public static String getOpeningHoursValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(OPENING_HOURS_TAG)
                : null;
    }

    /**
     * Check if OSM primitive has a tag 'bicycle'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'bicycle'
     */
    public static boolean hasBicycleTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BICYCLE_TAG) : false;
    }

    /**
     * Gets the value of tag 'bicycle'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'bicycle'
     */
    public static String getBicycleValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BICYCLE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'religion'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'religion'
     */
    public static boolean hasReligionTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(RELIGION_TAG) : false;
    }

    /**
     * Gets the value of tag 'religion'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'religion'
     */
    public static String getReligionValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(RELIGION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'barrier'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'barrier'
     */
    public static boolean hasBarrierTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BARRIER_TAG) : false;
    }

    /**
     * Gets the value of tag 'barrier'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'barrier'
     */
    public static String getBarrierValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BARRIER_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'power'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'power'
     */
    public static boolean hasPowerTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(POWER_TAG) : false;
    }

    /**
     * Gets the value of tag 'power'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'power'
     */
    public static String getPowerValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(POWER_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'landuse'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'landuse'
     */
    public static boolean hasLanduseTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LANDUSE_TAG) : false;
    }

    /**
     * Gets the value of tag 'landuse'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'landuse'
     */
    public static String getLanduseValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LANDUSE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'fireplace'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'fireplace'
     */
    public static boolean hasFireplaceTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(FIREPLACE_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'fireplace'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'fireplace'
     */
    public static String getFireplaceValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(FIREPLACE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'int_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'int_ref'
     */
    public static boolean hasIntRefTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(INT_REF_TAG) : false;
    }

    /**
     * Gets the value of tag 'int_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'int_ref'
     */
    public static String getIntRefValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(INT_REF_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'whitewater:section_grade'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'whitewater:section_grade'
     */
    public static boolean hasWhitewaterSectionGradeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WHITEWATER_SECTION_GRADE_TAG) : false;
    }

    /**
     * Gets the value of tag 'whitewater:section_grade'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'whitewater:section_grade'
     */
    public static String getWhitewaterSectionGradeValue(
            OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive
                .get(WHITEWATER_SECTION_GRADE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'denomination'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'denomination'
     */
    public static boolean hasDenominationTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(DENOMINATION_TAG) : false;
    }

    /**
     * Gets the value of tag 'denomination'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'denomination'
     */
    public static String getDenominationValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(DENOMINATION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:postcode'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:postcode'
     */
    public static boolean hasAddrPostcodeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_POSTCODE_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:postcode'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:postcode'
     */
    public static String getAddrPostcodeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_POSTCODE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'wires'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'wires'
     */
    public static boolean hasWiresTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WIRES_TAG) : false;
    }

    /**
     * Gets the value of tag 'wires'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'wires'
     */
    public static String getWiresValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WIRES_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'loc_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'loc_ref'
     */
    public static boolean hasLocRefTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LOC_REF_TAG) : false;
    }

    /**
     * Gets the value of tag 'loc_ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'loc_ref'
     */
    public static String getLocRefValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LOC_REF_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'width'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'width'
     */
    public static boolean hasWidthTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WIDTH_TAG) : false;
    }

    /**
     * Gets the value of tag 'width'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'width'
     */
    public static String getWidthValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WIDTH_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'tourism'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'tourism'
     */
    public static boolean hasTourismTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(TOURISM_TAG) : false;
    }

    /**
     * Gets the value of tag 'tourism'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'tourism'
     */
    public static String getTourismValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(TOURISM_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'leisure'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'leisure'
     */
    public static boolean hasLeisureTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LEISURE_TAG) : false;
    }

    /**
     * Gets the value of tag 'leisure'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'leisure'
     */
    public static String getLeisureValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LEISURE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'electrified'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'electrified'
     */
    public static boolean hasElectrifiedTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ELECTRIFIED_TAG) : false;
    }

    /**
     * Gets the value of tag 'electrified'.
     *
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'electrified'
     */
    public static String getElectrifiedValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ELECTRIFIED_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'junction'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'junction'
     */
    public static boolean hasJunctionTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(JUNCTION_TAG) : false;
    }

    /**
     * Gets the value of tag 'junction'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'junction'
     */
    public static String getJunctionValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(JUNCTION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'railway'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'railway'
     */
    public static boolean hasRailwayTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(RAILWAY_TAG) : false;
    }

    /**
     * Gets the value of tag 'railway'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'railway'
     */
    public static String getRailwayValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(RAILWAY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'voltage'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'voltage'
     */
    public static boolean hasVoltageTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(VOLTAGE_TAG) : false;
    }

    /**
     * Gets the value of tag 'voltage'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'voltage
     */
    public static String getVoltageValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(VOLTAGE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'bridge'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'bridge'
     */
    public static boolean hasBridgeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BRIDGE_TAG) : false;
    }

    /**
     * Gets the value of tag 'bridge'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'bridge'
     */
    public static String getBridgeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BRIDGE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'motor_vehicle'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'motor_vehicle'
     */
    public static boolean hasMotorVehicleTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(MOTOR_VEHICLE_TAG) : false;
    }

    /**
     * Gets the value of tag 'motor_vehicle'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'motor_vehicle'
     */
    public static String getMotorVehicleValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(MOTOR_VEHICLE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'comment'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'comment'
     */
    public static boolean hasCommentTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(COMMENT_TAG) : false;
    }

    /**
     * Gets the value of tag 'comment'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'comment'
     */
    public static String getCommentValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(COMMENT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'maxspeed'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'maxspeed'
     */
    public static boolean hasMaxspeedTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(MAXSPEED_TAG) : false;
    }

    /**
     * Gets the value of tag 'maxspeed'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'maxspeed'
     */
    public static String getMaxspeedValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(MAXSPEED_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'natural'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'natural'
     */
    public static boolean hasNaturalTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NATURAL_TAG) : false;
    }

    /**
     * Gets the value of tag 'natural'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'natural'
     */
    public static String getNaturalValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NATURAL_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'sac_scale'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'sac_scale'
     */
    public static boolean hasSacScaleTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SAC_SCALE_TAG) : false;
    }

    /**
     * Gets the value of tag 'sac_scale'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'sac_scale'
     */
    public static String getSacScaleValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SAC_SCALE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'tunnel'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'tunnel'
     */
    public static boolean hasTunnelTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(TUNNEL_TAG) : false;
    }

    /**
     * Gets the value of tag 'tunnel'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'tunnel'
     */
    public static String getTunnelValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(TUNNEL_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'waterway'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'waterway'
     */
    public static boolean hasWaterwayTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WATERWAY_TAG) : false;
    }

    /**
     * Gets the value of tag 'waterway'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'waterway'
     */
    public static String getWaterwayValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WATERWAY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'trail_visibility'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'trail_visibility'
     */
    public static boolean hasTrailVisibilityTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(TRAIL_VISIBILITY_TAG) : false;
    }

    /**
     * Gets the value of tag 'trail_visibility'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'trail_visibility'
     */
    public static String getTrailVisibilityValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(TRAIL_VISIBILITY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'highway'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'highway'
     */
    public static boolean hasHighwayTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(HIGHWAY_TAG) : false;
    }

    /**
     * Gets the value of tag 'highway'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'highway'
     */
    public static String getHighwayValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(HIGHWAY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'vehicle'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'vehicle'
     */
    public static boolean hasVehicleTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(VEHICLE_TAG) : false;
    }

    /**
     * Gets the value of tag 'vehicle'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'vehicle'
     */
    public static String getVehicleValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(VEHICLE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'horse'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'horse'
     */
    public static boolean hasHorseTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(HORSE_TAG) : false;
    }

    /**
     * Gets the value of tag 'horse'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'horse'
     */
    public static String getHorseValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(HORSE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'goods'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'goods'
     */
    public static boolean hasGoodsTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(GOODS_TAG) : false;
    }

    /**
     * Gets the value of tag 'goods'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'goods'
     */
    public static String getGoodsValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(GOODS_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'frequency'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'frequency'
     */
    public static boolean hasFrequencyTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(FREQUENCY_TAG) : false;
    }

    /**
     * Gets the value of tag 'frequency'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'frequency'
     */
    public static String getFrequencyValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(FREQUENCY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'man_made'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'man_made'
     */
    public static boolean hasManMadeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(MAN_MADE_TAG) : false;
    }

    /**
     * Gets the value of tag 'man_made'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'man_made'
     */
    public static String getManMadeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(MAN_MADE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:housenumber'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:housenumber'
     */
    public static boolean hasAddrHousenumberTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_HOUSENUMBER_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:housenumber'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:housenumber'
     */
    public static String getAddrHousenumberValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_HOUSENUMBER_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:housename'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:housename'
     */
    public static boolean hasAddrHousenameTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_HOUSENAME_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:housename'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:housename'
     */
    public static String getAddrHousenameValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_HOUSENAME_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'area'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'area'
     */
    public static boolean hasAreaTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(AREA_TAG) : false;
    }

    /**
     * Gets the value of tag 'area'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'area'
     */
    public static String getAreaValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(AREA_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'building:levels'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'building:levels'
     */
    public static boolean hasBuildingLevelsTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BUILDING_LEVELS_TAG) : false;
    }

    /**
     * Gets the value of tag 'building:levels'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'building:levels'
     */
    public static String getBuildingLevelsValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BUILDING_LEVELS_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'wheelchair'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'wheelchair'
     */
    public static boolean hasWheelchairTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WHEELCHAIR_TAG) : false;
    }

    /**
     * Gets the value of tag 'wheelchair'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'wheelchair'
     */
    public static String getWheelchairValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WHEELCHAIR_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'name'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'name'
     */
    public static boolean hasNameTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NAME_TAG) : false;
    }

    /**
     * Gets the value of tag 'name'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'name'
     */
    public static String getNameValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NAME_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'oneway'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'oneway'
     */
    public static boolean hasOnewayTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ONEWAY_TAG) : false;
    }

    /**
     * Gets the value of tag 'oneway'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'oneway'
     */
    public static String getOnewayValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ONEWAY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'FIXME'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'FIXME'
     */
    public static boolean hasFIXMETag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(FIXME_TAG) : false;
    }

    /**
     * Gets the value of tag 'FIXME'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'FIXME'
     */
    public static String getFIXMEValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(FIXME_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'capacity'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'capacity'
     */
    public static boolean hasCapacityTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CAPACITY_TAG) : false;
    }

    /**
     * Gets the value of tag 'capacity'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'capacity'
     */
    public static String getCapacityValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CAPACITY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'motorcycle'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'motorcycle'
     */
    public static boolean hasMotorcycleTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(MOTORCYCLE_TAG) : false;
    }

    /**
     * Gets the value of tag 'motorcycle'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'motorcycle'
     */
    public static String getMotorcycleValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(MOTORCYCLE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'hgv'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'hgv'
     */
    public static boolean hasHgvTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(HGV_TAG) : false;
    }

    /**
     * Gets the value of tag 'hgv'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'hgv'
     */
    public static String getHgvValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(HGV_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'construction'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'construction'
     */
    public static boolean hasConstructionTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CONSTRUCTION_TAG) : false;
    }

    /**
     * Gets the value of tag 'construction'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'construction'
     */
    public static String getConstructionValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CONSTRUCTION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:state'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:state'
     */
    public static boolean hasAddrStateTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_STATE_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:state'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:state'
     */
    public static String getAddrStateValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_STATE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'lanes'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'lanes'
     */
    public static boolean hasLanesTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LANES_TAG) : false;
    }

    /**
     * Gets the value of tag 'lanes'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'lanes'
     */
    public static String getLanesValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LANES_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'note'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'note'
     */
    public static boolean hasNoteTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NOTE_TAG) : false;
    }

    /**
     * Gets the value of tag 'note'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'note'
     */
    public static String getNoteValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NOTE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'lit'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'lit'
     */
    public static boolean hasLitTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LIT_TAG) : false;
    }

    /**
     * Gets the value of tag 'lit'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'lit'
     */
    public static String getLitValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LIT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'building'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'building'
     */
    public static boolean hasBuildingTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(BUILDING_TAG) : false;
    }

    /**
     * Gets the value of tag 'building'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'building'
     */
    public static String getBuildingValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(BUILDING_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'segregated'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'segregated'
     */
    public static boolean hasSegregatedTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SEGREGATED_TAG) : false;
    }

    /**
     * Gets the value of tag 'segregated'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'segregated'
     */
    public static String getSegregatedValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SEGREGATED_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:inclusion'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:inclusion'
     */
    public static boolean hasAddrInclusionTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_INCLUSION_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:inclusion'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:inclusion'
     */
    public static String getAddrInclusionValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_INCLUSION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'layer'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'layer'
     */
    public static boolean hasLayerTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(LAYER_TAG) : false;
    }

    /**
     * Gets the value of tag 'layer'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'layer'
     */
    public static String getLayerValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(LAYER_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'sport'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'sport'
     */
    public static boolean hasSportTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SPORT_TAG) : false;
    }

    /**
     * Gets the value of tag 'sport'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'sport'
     */
    public static String getSportValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SPORT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:interpolation'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:interpolation'
     */
    public static boolean hasAddrInterpolationTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_INTERPOLATION_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:interpolation'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:interpolation'
     */
    public static String getAddrInterpolationValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_INTERPOLATION_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'cutting'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'cutting'
     */
    public static boolean hasCuttingTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CUTTING_TAG) : false;
    }

    /**
     * Gets the value of tag 'cutting'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'cutting'
     */
    public static String getCuttingValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CUTTING_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'amenity'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'amenity'
     */
    public static boolean hasAmenityTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(AMENITY_TAG) : false;
    }

    /**
     * Gets the value of tag 'amenity'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'amenity'
     */
    public static String getAmenityValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(AMENITY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'access'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'access'
     */
    public static boolean hasAccessTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ACCESS_TAG) : false;
    }

    /**
     * Gets the value of tag 'access'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'access'
     */
    public static String getAccessValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ACCESS_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'agricultural'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'agricultural'
     */
    public static boolean hasAgriculturalTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(AGRICULTURAL_TAG)
                : false;
    }

    /**
     * Gets the value of tag 'agricultural'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'agricultural'
     */
    public static String getAgriculturalValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(AGRICULTURAL_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'capacity:disabled'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'capacity:disabled'
     */
    public static boolean hasCapacityDisabledTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive
                .hasKey(CAPACITY_DISABLED_TAG) : false;
    }

    /**
     * Gets the value of tag 'capacity:disabled'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'capacity:disabled'
     */
    public static String getCapacityDisabledValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CAPACITY_DISABLED_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'operator'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'operator'
     */
    public static boolean hasOperatorTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(OPERATOR_TAG) : false;
    }

    /**
     * Gets the value of tag 'operator'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'operator'
     */
    public static String getOperatorValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(OPERATOR_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'ref'
     */
    public static boolean hasRefTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(REF_TAG) : false;
    }

    /**
     * Gets the value of tag 'ref'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'ref'
     */
    public static String getRefValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(REF_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'noexit'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'noexit'
     */
    public static boolean hasNoexitTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(NOEXIT_TAG) : false;
    }

    /**
     * Gets the value of tag 'noexit'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'noexit'
     */
    public static String getNoexitValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(NOEXIT_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'admin_level'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'admin_level'
     */
    public static boolean hasAdminLevelTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADMIN_LEVEL_TAG) : false;
    }

    /**
     * Gets the value of tag 'admin_level'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'admin_level'
     */
    public static String getAdminLevelValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADMIN_LEVEL_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'source'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'source'
     */
    public static boolean hasSourceTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SOURCE_TAG) : false;
    }

    /**
     * Gets the value of tag 'source'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'source'
     */
    public static String getSourceValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SOURCE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'tracktype'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'tracktype'
     */
    public static boolean hasTracktypeTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(TRACKTYPE_TAG) : false;
    }

    /**
     * Gets the value of tag 'tracktype'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'tracktype'
     */
    public static String getTracktypeValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(TRACKTYPE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'addr:country'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'addr:country'
     */
    public static boolean hasAddrCountryTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ADDR_COUNTRY_TAG) : false;
    }

    /**
     * Gets the value of tag 'addr:country'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'addr:country'
     */
    public static String getAddrCountryValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ADDR_COUNTRY_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'route'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'route'
     */
    public static boolean hasRouteTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(ROUTE_TAG) : false;
    }

    /**
     * Gets the value of tag 'route'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'route'
     */
    public static String getRouteValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(ROUTE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'cables'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'cables'
     */
    public static boolean hasCablesTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(CABLES_TAG) : false;
    }

    /**
     * Gets the value of tag 'cables'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'cables'
     */
    public static String getCablesValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(CABLES_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'service'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'service'
     */
    public static boolean hasServiceTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(SERVICE_TAG) : false;
    }

    /**
     * Gets the value of tag 'service'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'service'
     */
    public static String getServiceValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(SERVICE_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'motorcar'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'motorcar'
     */
    public static boolean hasMotorcarTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(MOTORCAR_TAG) : false;
    }

    /**
     * Gets the value of tag 'motorcar'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'motorcar'
     */
    public static String getMotorcarValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(MOTORCAR_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'whitewater'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'whitewater'
     */
    public static boolean hasWhitewaterTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(WHITEWATER_TAG) : false;
    }

    /**
     * Gets the value of tag 'whitewater'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'whitewater'
     */
    public static String getWhitewaterValue(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.get(WHITEWATER_TAG) : null;
    }

    /**
     * Check if OSM primitive has a tag 'embankment'.
     * @param osmPrimitive The OSM entity to check.
     * @return {@code true} if OSM primitive has a tag 'embankment'
     */
    public static boolean hasEmbankmentTag(OsmPrimitive osmPrimitive) {
        return osmPrimitive != null ? osmPrimitive.hasKey(EMBANKMENT_TAG) : false;
    }

    /**
     * Gets the value of tag 'embankment'.
     * @param osmPrimitive The OSM entity to check.
     * @return the value of tag 'embankment'
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
     * @param rel The relation to check.
     * @return {@code true} if OSM relation is a 'associatedStreet' relation
     */
    public static boolean isAssociatedStreetRelation(Relation rel) {
        return rel != null &&
            rel.hasKey(RELATION_TYPE) &&
            ASSOCIATEDSTREET_RELATION_TYPE.equals(rel.get(RELATION_TYPE));
    }

    /**
     * Checks if given relation member has role "street".
     * @param relMember the relation member
     * @return true, if is street member
     */
    public static boolean isStreetMember(RelationMember relMember) {
        return relMember != null && STREET_RELATION_ROLE.equals(relMember.getRole());
    }

    /**
     * Checks if given relation member has role "house".
     * @param relMember the relation member
     * @return true, if is street member
     */
    public static boolean isHouseMember(RelationMember relMember) {
        return relMember != null && STREET_RELATION_ROLE.equals(relMember.getRole());
    }

    /**
     * Checks if "addr:state" tag is required.
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
}
