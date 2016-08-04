// License: GPL. For details, see LICENSE file.
package com.innovant.josm.jrt.core;

import org.openstreetmap.josm.data.coor.LatLon;

public interface RoutingEdge {

    LatLon fromLatLon();

    LatLon toLatLon();

    Object fromV();

    Object toV();

    double getLength();

    void setLength(double length);

    double getSpeed();

    void setSpeed(double speed);

    boolean isOneway();

    void setOneway(boolean isOneway);

}
