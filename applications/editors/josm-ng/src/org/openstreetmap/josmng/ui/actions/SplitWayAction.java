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
import java.util.List;

import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.osm.Node;
import org.openstreetmap.josmng.osm.OsmPrimitive;
import org.openstreetmap.josmng.osm.Way;
import org.openstreetmap.josmng.osm.visitors.CollectVisitor;
import org.openstreetmap.josmng.utils.MenuPosition;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 * An action that splits way(s) at the selected point(s). It autodetects the way
 * to split, but in case more ways touch given point, user have to select
 * the way too.
 * 
 * @author nenik
 */
@MenuPosition(value="Tools", shortcut="P")
public class SplitWayAction extends AtomicDataSetAction {
    public SplitWayAction() {
        super("Split way");
    }

    public @Override void perform(OsmLayer layer, DataSet ds, ActionEvent ae) {
        // collect the selection
        CollectVisitor cv = layer.visitSelection(new CollectVisitor());
        
        // verify usable elements are selected
        if (cv.getRelations().size() > 0) return;
        if (cv.getWays().size() > 1) return; // for complex cases, split only one way
        for (Node splitPoint : cv.getNodes()) {
            //verify the splitpoint
            if(usesNode(ds, splitPoint, cv.getWays()) == null) return;
        }
        
        // do the splitting
        for (Node splitPoint : cv.getNodes()) {
            Way toSplit = usesNode(ds, splitPoint, cv.getWays());
            assert toSplit != null;
            split(ds, toSplit, splitPoint);
            
        }
    }
    
    private void split(DataSet ds, Way w, Node at) {
        List<Node>[] parts = split(w.getNodes(), at);
        if (parts[0].size() > 1 && parts[1].size() > 1) {
            w.setNodes(parts[0]);
            Way w2 = ds.createWay(parts[1].toArray(new Node[parts[1].size()]));
            copyTags(w, w2);
        }
    }
    
    List<Node>[] split(List<Node> nodes, Node at) {
        List<Node>[] pair = new List[] { new ArrayList<Node>(), new ArrayList<Node>()};
        
        int idx = 0;
        for (Node act : nodes) {
            pair[idx].add(act);
            if (act == at && idx == 0) {
                idx++;
                pair[idx].add(act);
            }
        }
        return pair;
    }
    
    private void copyTags(OsmPrimitive from, OsmPrimitive to) {
        for (String key: from.getTags()) to.putTag(key, from.getTag(key));
    }
    
    
    // finds the way to split
    private Way usesNode(DataSet ds, final Node n, Collection<Way> preferred) {
        Way pref = preferred.size() > 0 ? preferred.iterator().next() : null;
        
        CollectVisitor cv = new CollectVisitor();
        cv.visitCollection(n.getReferrers());
        Collection<Way> ways = cv.getWays();
        
        if (ways.size() == 1) {
            if (pref == null || ways.contains(pref)) return ways.iterator().next();
        } if (pref != null && ways.contains(pref)) {
            return pref;
        }
        return null;
    }
}
