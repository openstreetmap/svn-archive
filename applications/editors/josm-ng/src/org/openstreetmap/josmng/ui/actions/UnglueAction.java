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
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.osm.visitors.CollectVisitor;
import org.openstreetmap.josmng.utils.MenuPosition;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 *
 * @author nenik
 */
@MenuPosition(value="Tools",shortcut="G")
public class UnglueAction extends AtomicDataSetAction {
    public UnglueAction() {
        super("Unglue nodes");
    }

    public @Override void perform(OsmLayer layer, DataSet ds, ActionEvent ae) {
        CollectVisitor cv = layer.visitSelection(new CollectVisitor());
        if (cv.getWays().size() > 0 || cv.getRelations().size() > 0 || cv.getNodes().size() != 1) {
            JOptionPane.showMessageDialog(null, "The current selection cannot be used for unglueing.");
            return;
        }
        
        Node splitPoint = cv.getNodes().iterator().next();
        
        CollectVisitor refs = new CollectVisitor();
        refs.visitCollection(splitPoint.getReferrers());
        
        if (refs.getWays().size() < 2) {
            JOptionPane.showMessageDialog(null, "You must select a node that is used by at least 2 ways.");
            return;
        }
        
        List<Node> newNodes = new ArrayList<Node>();
        
        Iterator<Way> ways = refs.getWays().iterator();
        ways.next(); // leave the first way alone
        
        // every other way get its own copy of the node
        while (ways.hasNext()) { 
            Way act = ways.next();
            Node copy = cloneNode(ds, splitPoint);
            newNodes.add(copy);
            List<Node> nodes = new ArrayList<Node>(act.getNodes());
            Collections.replaceAll(nodes, splitPoint, copy);
            act.setNodes(nodes);
        }
        
        // every relation using splitPoint gets all its clones as members with the same role
        for (Relation rel : refs.getRelations()) {
            String role = rel.getMembers().get(splitPoint);
            for (Node n : newNodes) {
                rel.addMember(n, role);
            }
        }
    }

    private Node cloneNode(DataSet ds, Node source) {
        Node copy = ds.createNode(source.getLatitude(), source.getLongitude());
        for (String key : source.getTags()) {
            copy.putTag(key, source.getTag(key));
        }
        return copy;
    }
}
