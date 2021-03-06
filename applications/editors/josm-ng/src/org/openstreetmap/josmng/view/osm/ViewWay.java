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

import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.view.*;

/**
 * View for a Way.
 * Caches a bbox of the way, translated nodes and the current style.
 * 
 * @author nenik
 */
class ViewWay extends View<Way> {
    BBox bbox;
    ViewNode[] nodes;
    
    public ViewWay(Way way, ViewData parent) {
        super(way);
        update(parent, true);
        resetStyle();
    }

    // fake ctor for virtual relation-based ways
    ViewWay(Way way, BBox bbox, ViewNode[] nodes) {
        super(way);
        this.bbox = bbox;
        this.nodes = nodes;
    }
    
    public List<ViewNode> getNodes() {
        return Arrays.asList(nodes);
    }

    void update(ViewData parent, boolean members) {
        if (members) {
            List<Node> wNodes = getPrimitive().getNodes();
            nodes = new ViewNode[wNodes.size()];
            int i = 0;
            for (Node n : wNodes) nodes[i++] = (ViewNode)parent.add(n);
        }

        bbox = new BBox();
        for (ViewNode vn : nodes) bbox.addPoint(vn);
    }
    
    public int getSize() {
        return (int)Math.min(bbox.getWidth() + bbox.getHeight(), Integer.MAX_VALUE);
    }

    public @Override int getMaxScale() {
        return Math.min(super.getMaxScale(), getSize() / 3);
    }

    public @Override BBox getBounds() {
        return bbox;
    }    
}
