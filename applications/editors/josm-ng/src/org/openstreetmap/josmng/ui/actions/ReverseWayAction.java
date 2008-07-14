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

package org.openstreetmap.josmng.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.utils.MenuPosition;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 *
 * @author nenik
 */
@MenuPosition(value="Tools",shortcut="R")
public class ReverseWayAction extends AtomicDataSetAction {
    public ReverseWayAction() {
        super("Reverse way");
    }

    public @Override void perform(OsmLayer layer, DataSet ds, ActionEvent ae) {
        new Visitor() {
            protected @Override void visit(Way w) {
                List<Node> nodes = new ArrayList<Node>(w.getNodes());
                Collections.reverse(nodes);
                w.setNodes(nodes);
            }
        }.visitCollection(layer.getSelection());
    }

    
}
