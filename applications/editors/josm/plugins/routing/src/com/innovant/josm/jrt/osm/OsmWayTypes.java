/*
 *
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */

package com.innovant.josm.jrt.osm;

/**
 * @author jvidal
 *
 */
public enum OsmWayTypes {
    MOTORWAY ("motorway",120),
    MOTORWAY_LINK ("motorway_link",120),
    TRUNK ("trunk",120),
    TRUNK_LINK ("trunk_link",120),
    PRIMARY  ("primary",100),
    PRIMARY_LINK ("primary_link",100),
    SECONDARY ("secondary",90),
    TERTIARY ("tertiary",90),
    UNCLASSIFIED ("unclassified",50),
    ROAD ("road",100),
    RESIDENTIAL ("residential",50),
    LIVING_STREET ("living_street",30),
    SERVICE ("service",30),
    TRACK ("track",50),
    PEDESTRIAN ("pedestrian",30),
    BUS_GUIDEWAY ("bus_guideway",50),
    PATH ("path",40),
    CYCLEWAY ("cycleway",40),
    FOOTWAY ("footway",20),
    BRIDLEWAY ("bridleway",40),
    BYWAY ("byway",50),
    STEPS ("steps",10);

    /**
     * Default Constructor
     * @param tag
     */
    OsmWayTypes(String tag,int speed) {
        this.tag = tag;
        this.speed = speed;
    }

    /**
     * Tag
     */
    private final String tag;
    private final int speed;

    /**
     * @return
     */
    public String getTag() {return tag;};
    public int getSpeed() {return speed;};
}
