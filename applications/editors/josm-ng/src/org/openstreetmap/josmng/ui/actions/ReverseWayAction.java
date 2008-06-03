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
import javax.swing.AbstractAction;

import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.ui.Main;
import org.openstreetmap.josmng.view.EditableLayer;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 *
 * @author nenik
 */
public class ReverseWayAction extends AbstractAction {
    public ReverseWayAction() {
        super("Reverse way");
    }

    public void actionPerformed(ActionEvent e) {
        final EditableLayer layer = Main.main.getMapView().getCurrentEditLayer();
        if (layer instanceof OsmLayer) {
            ((OsmLayer)layer).getDataSet().atomicEdit(new Runnable() {
                public void run() {
                    new Visitor() {
                        protected @Override void visit(Way w) {
                            List<Node> nodes = new ArrayList<Node>(w.getNodes());
                            Collections.reverse(nodes);
                            w.setNodes(nodes);
                        }
                    }.visitCollection(layer.getSelection());
                }
            }, null);
        }
    }
}
