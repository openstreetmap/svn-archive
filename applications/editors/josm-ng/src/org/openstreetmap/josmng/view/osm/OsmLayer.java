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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;

import org.openstreetmap.josmng.view.*;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.utils.UndoHelper;

/**
 *
 * @author nenik
 */
public class OsmLayer extends EditableLayer {
    private String name;
    private DataSet data;
    private UndoHelper undo = new UndoHelper();
    ViewData mapData;

    public OsmLayer(MapView parent, String name, DataSet data) {
        super(parent);
        this.name = name;
        this.data = data;
        data.addUndoableEditListener(undo);
        mapData = new ViewData(this, data);
    }

    public @Override UndoHelper getUndoManager() {
        return undo;
    }

    void callRepaint() {
        parent.repaint();
    }

    public @Override void paint(Graphics g) {
System.err.println("scale:" + parent.getScaleFactor());
        Rectangle viewR = parent.screenToView(g.getClipBounds());
        
long time = System.currentTimeMillis();
        for (View v : mapData.getViews(viewR, parent.getScaleFactor())) {
            v.paint((Graphics2D)g.create(), parent);
        }
time = System.currentTimeMillis() - time;
System.err.println("Painted in " + time + "ms");
    }

    @Override
    public String getName() {
        return name;
    }

    public @Override Node getNearestNode(Point p) {
        Rectangle r = new Rectangle(p);
        r.grow(10, 10);
        Rectangle viewR = parent.screenToView(r);
        
        double minDistanceSq = Double.MAX_VALUE;
        ViewNode minPrimitive = null;
        
        Collection<? extends ViewCoords> col = mapData.getNodes(viewR);
        System.err.println("10-area (" + viewR + ") contains " + col.size() + " nodes");

        for (ViewCoords vn : col) {
            // if (n.deleted || n.incomplete) continue;
            Point sp = parent.getPoint(vn);
            double dist = p.distanceSq(sp);
            if (minDistanceSq > dist && dist < 100) {
                    minDistanceSq = p.distanceSq(sp);
                    minPrimitive = (ViewNode)vn;
            }
        }
        return minPrimitive == null ? null : minPrimitive.getNode();
    }   
}
