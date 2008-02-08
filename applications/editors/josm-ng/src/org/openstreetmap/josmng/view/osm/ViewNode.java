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

package org.openstreetmap.josmng.view.osm;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.openstreetmap.josmng.view.*;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;

/**
 * View for a Node.
 * Caches a projected position information and current style.
 * 
 * @author nenik
 */
final class ViewNode extends ViewCoords implements View {
    Node node;
    Style current;

    ViewNode(Node node, ViewCoords position) {
        super(position.getIntLon(), position.getIntLat());
        this.node = node;
        setFlag(resolveTagged(node));
    }
    
    public Node getNode() {
        return node;
    }
    
    public OsmPrimitive getOsmPrimitive() {
        return node;
    }
    
    public boolean isTagged() {
        return getFlag();
    }

    void updatePosition(ViewCoords newPos) {
        setCoordinates(newPos);
    }
    
    private static Collection<String> UNINTERESTING = new HashSet<String>(
            Arrays.asList(new String[] {"source", "note", "created_by"}));
    
    private static boolean resolveTagged(Node n) {
        for (String tagName : n.getTags()) {
            if (!UNINTERESTING.contains(tagName)) return true;
        }
        return false;
    }
    
    public void resetStyle() {
        current = null;
    }

    public void paint(Graphics2D g, MapView parent) {
        if (current == null) current = Style.get(this);
        current.paint(g, parent, this);
    }
}
