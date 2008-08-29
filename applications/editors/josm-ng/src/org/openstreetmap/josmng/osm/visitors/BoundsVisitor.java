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

package org.openstreetmap.josmng.osm.visitors;

import org.openstreetmap.josmng.osm.Bounds;
import org.openstreetmap.josmng.osm.Coordinate;
import org.openstreetmap.josmng.osm.CoordinateImpl;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.osm.Way;

/**
 * A visitor that gathers the bounds of given set of OsmPrimitives.
 * It can also be used to count the bounds of a set of any Coordinate instances.
 * 
 * @author nenik
 */
public final class BoundsVisitor extends Visitor {
    private final double[] limits = new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
    boolean recursive;

    /**
     * Creates a Visitor that garthers the minimal rectangle containing
     * all the visited primitives.
     * 
     * @param recursive whether to follow content of ways and relations.
     * If false, only directly visited nodes are taken into account. If true,
     * also all the nodes recursively reachable from visited ways and relations
     * are counted. Use false only if you're going to visit all the nodes anyway,
     * like when visiting the whole DataSet.
     */
    public BoundsVisitor(boolean recursive) {
        this.recursive = recursive;
    }

    protected @Override void visit(Node n) {
        visitCoordinate(n);
    }

    /**
     * Extend the bounds to contain given Coordinate.
     * @param c the Coordinate
     */
    public void visitCoordinate(Coordinate c) {
        double lat = c.getLatitude();
        double lon = c.getLongitude();
        if (lat < limits[0]) limits[0] = lat;
        if (lon < limits[1]) limits[1] = lon;
        if (lat > limits[2]) limits[2] = lat;
        if (lon > limits[3]) limits[3] = lon;        
    }
    
    protected @Override void visit(Way w) {
        if (recursive) visitCollection(w.getNodes());
    }

    protected @Override void visit(Relation r) {
        if (recursive) visitCollection(r.getMembers().keySet());
    }
    
    
    /**
     * Get the resultant bounds.
     * 
     * @return the computed Bounds of visited OsmPrimitives and other Coordinates.
     */
    public Bounds getBounds() {
        return Bounds.create(new CoordinateImpl(limits[0], limits[1]),
                            new CoordinateImpl(limits[2], limits[3]));
    }
}
