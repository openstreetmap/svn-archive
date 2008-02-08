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
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.view.*;

/**
 * View for a Way.
 * Caches a bbox of the way, translated nodes and the current style.
 * 
 * @author nenik
 */
class ViewWay implements View {
    Way way;
    Rectangle bbox;
    ViewNode[] nodes;

    Style style;
    
    public ViewWay(Way way, Rectangle bbox, ViewNode[] nodes) {
        this.way = way;
        this.bbox = bbox;
        this.nodes = nodes;
    }
    
    public List<ViewNode> getNodes() {
        return Arrays.asList(nodes);
    }

    Way getWay() {
        return way;
    }

    public OsmPrimitive getOsmPrimitive() {
        return way;
    }
    
    public int getSize() {
        return bbox.width + bbox.height;
    }

    public void resetStyle() {
        style = null;
    }
    
    public void paint(Graphics2D g, MapView parent) {
        if (style == null) style = Style.get(this);
        style.paint(g, parent, this);
    }

}
