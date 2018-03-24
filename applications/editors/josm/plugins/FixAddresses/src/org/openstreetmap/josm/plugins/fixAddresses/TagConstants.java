// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

/**
 * Contains the tags used within OSM. FIXME: Maybe there is a class or similar
 * within JOSM which already defines them, but I have not found it so far.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de&gt;
 */
public final class TagConstants {
    static String[] COUNTRIES_REQUIRE_STATE = {
        "en_US",    /* USA */
        "en_AU" /* Australia */
    };

    private TagConstants() {
        // Hide default constructor for utilities classes
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

    // Associated street: See https://wiki.openstreetmap.org/wiki/Proposed_features/De:Hausnummern
    public static final String RELATION_TYPE = "type";
    public static final String ASSOCIATEDSTREET_RELATION_TYPE = "associatedStreet";
    public static final String STREET_RELATION_ROLE = "street";
    public static final String HOUSE_RELATION_ROLE = "house";
}
