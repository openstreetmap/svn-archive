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

package org.openstreetmap.josmng.osm;

import javax.swing.undo.*;

/**
 * A representation of a single OSM node. Contains a (mutable) coordinate
 * and all the node metadata.
 * 
 * @author nenik
 */
public final class Node extends OsmPrimitive implements Coordinate {
    private double lat, lon;

    Node(DataSet source, long id, double lat, double lon, int stamp, String user, boolean vis) {
        super(source, id, stamp, user, vis);
        this.lat = lat;
        this.lon = lon;
    }
    
    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }
    
    public void setCoordinate(Coordinate coor) {
        UndoableEdit edit = new ChangeCoordinatesEdit();
        setCoordinateImpl(coor.getLatitude(), coor.getLongitude());
        source.postEdit(edit);
    }

    @Override void visit(Visitor v) {
        v.visit(this);
    }
    
    private void setCoordinateImpl(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        source.fireNodeMoved(this);
    }

    private class ChangeCoordinatesEdit extends PrimitiveToggleEdit {
        double savedLat, savedLon;
        
        public ChangeCoordinatesEdit() {
            super("move node");
            savedLat = lat;
            savedLon = lon;
        }

        protected @Override void toggle() {
            double origLat = lat;
            double origLon = lon;
            setCoordinateImpl(savedLat, savedLon);
            savedLat = origLat;
            savedLon = origLon;
        }
    }
}
