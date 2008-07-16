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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Relation;
import org.openstreetmap.josmng.osm.Visitor;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.utils.MenuPosition;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 * DeleteAction deletes selected nodes and ways. The behavior is different
 * for different kinds of primitives and their referrers:<ul>
 * <li>Whatever referenced from a relation: gets deleted from the relation/
 * <li>Wayed node gets removed from the way. If the way degenerates, the way
 * gets deleted as well.
 * <li>Deleting way will delete all its nodes unless they are referenced by
 * other primitive too.
 * 
 * </ul>
 * @author nenik
 */
@MenuPosition(value="Tools", shortcut="DELETE")
public class DeleteAction extends AtomicDataSetAction {
    public DeleteAction() {
        super("Delete");
    }

    public @Override void perform(OsmLayer layer, DataSet ds, ActionEvent ae) {
        boolean deleteWithNodes = true;

        Set<Node> deletedWaysNodes = new HashSet<Node>();
        // delete the primitives and their back references
        // while collecting way nodes for further cleanup
        for (OsmPrimitive prim : layer.getSelection()) {
            Collection<OsmPrimitive> uses = prim.getReferrers();
            if (uses != null) {
                new CleanerVisitor(prim).visitCollection(uses);
            }
            if (prim instanceof Way) {
                deletedWaysNodes.addAll(((Way)prim).getNodes());
            }
            prim.delete();
        }
        
        // delete the unreferenced nodes too
        if (deleteWithNodes && !deletedWaysNodes.isEmpty()) {
            for (Node n : deletedWaysNodes) {
                Collection<OsmPrimitive> use = n.getReferrers();
                if (use.isEmpty()) n.delete();
            }
        }
        
    }
    
    private static void removeNodes(Way w, Set<Node> nodes) {
        if (w.isDeleted()) return;
        boolean changed = false;
        List<Node> wayNodes = new ArrayList();
        for (Node act : w.getNodes()) {
            if (nodes.contains(act)) {
                changed = true;
            } else {
                wayNodes.add(act);
            }
        }
        if (changed) w.setNodes(wayNodes);
    }
    
    private static class CleanerVisitor extends Visitor {
        private final OsmPrimitive toBeDeleted;

        public CleanerVisitor(OsmPrimitive toBeDeleted) {
            this.toBeDeleted = toBeDeleted;
        }
        
        protected @Override void visit(Way w) {
            removeNodes(w, Collections.singleton((Node)toBeDeleted));
        }

        protected @Override void visit(Relation r) {
            r.removeMember(toBeDeleted);
        }
    }
            
}
