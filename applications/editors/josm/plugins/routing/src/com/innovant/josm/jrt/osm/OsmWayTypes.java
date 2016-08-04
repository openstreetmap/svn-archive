// License: GPL. For details, see LICENSE file.
package com.innovant.josm.jrt.osm;

/**
 * @author jvidal
 *
 */
public enum OsmWayTypes {
    MOTORWAY("motorway", 120),
    MOTORWAY_LINK("motorway_link", 120),
    TRUNK("trunk", 120),
    TRUNK_LINK("trunk_link", 120),
    PRIMARY("primary", 100),
    PRIMARY_LINK("primary_link", 100),
    SECONDARY("secondary", 90),
    TERTIARY("tertiary", 90),
    UNCLASSIFIED("unclassified", 50),
    ROAD("road", 100),
    RESIDENTIAL("residential", 50),
    LIVING_STREET("living_street", 30),
    SERVICE("service", 30),
    TRACK("track", 50),
    PEDESTRIAN("pedestrian", 30),
    BUS_GUIDEWAY("bus_guideway", 50),
    PATH("path", 40),
    CYCLEWAY("cycleway", 40),
    FOOTWAY("footway", 20),
    BRIDLEWAY("bridleway", 40),
    BYWAY("byway", 50),
    STEPS("steps", 10);

    /**
     * Default Constructor
     */
    OsmWayTypes(String tag, int speed) {
        this.tag = tag;
        this.speed = speed;
    }

    /**
     * Tag
     */
    private final String tag;
    private final int speed;

    public String getTag() {
        return tag;
    }

    public int getSpeed() {
        return speed;
    }
}
