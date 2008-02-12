/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.view;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;

import org.openstreetmap.josmng.view.projection.Mercator;
import org.openstreetmap.josmng.view.projection.Epsg4326;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;

/**
 * TODO: pluggable projections by means of ServiceLoader
 *
 * @author nenik
 */
public final class Projection {
    private static final double MAX_LAT = 85.05112877980659;
    private static final double MAX_LON = 180;

    // lat,lon -> y,x
    private static final Coordinate[] PROBES = new Coordinate[] {
        new CoordinateImpl(MAX_LAT, -MAX_LON),
        new CoordinateImpl(MAX_LAT, MAX_LON),
        new CoordinateImpl(0, -MAX_LON),
        new CoordinateImpl(0, MAX_LON),
        new CoordinateImpl(-MAX_LAT, -MAX_LON),
        new CoordinateImpl(-MAX_LAT, MAX_LON),
    };

    public static final Projection MERCATOR = create(new Mercator());
    public static final Projection EPSG4326 = create(new Epsg4326());
    
           
    private Impl impl;
    private double factor;

    public Projection(Impl impl, double factor) {
        this.impl = impl;
        this.factor = factor;
    }

    public static Collection<Projection> getAvailableProjections() {
        return Arrays.asList(new Projection[] {
            MERCATOR, EPSG4326
        });
    }

    public static Projection create(Impl impl) {
        Rectangle2D.Double world = new Rectangle2D.Double(0, 0, -1, -1);
        for (Coordinate probe : PROBES) {
            world.add(impl.lonLatToPoint(probe.getLongitude(), probe.getLatitude()));
        }

        assert world.getCenterX() < 1e-12 && world.getCenterY() < 1e-12;
        double factor = Math.min(2/world.getWidth(), 2/world.getHeight());
        factor /= 1.000001;
        return new Projection(impl, factor);
    }
    
    public ViewCoords coordToView(Coordinate coords) {
        Point2D p = impl.lonLatToPoint(coords.getLongitude(), coords.getLatitude());
        return new ViewCoords(factor * p.getX(), factor * p.getY());
    }
    
    public Coordinate viewToCoord(ViewCoords view) {
        Point2D p = impl.pointToLonLat(view.getLon()/factor, view.getLat()/factor);
        return new CoordinateImpl(p.getY(), p.getX());
    }

    public String getName() {
        return impl.getName();
    }

    public @Override boolean equals(Object obj) {
        if (obj instanceof Projection) {
            return impl.equals(((Projection)obj).impl);
        }
        return false;
    }

    public @Override int hashCode() {
        return impl.hashCode();
    }
    
    /**
     * Projection SPI that allows native projection implementation
     * without the need to enforce particular value range of the projected
     * coordinates.
     */ 
    public interface Impl {
        Point2D lonLatToPoint(double lon, double lat);
        Point2D pointToLonLat(double east, double north);
        String getName();
    }
}
