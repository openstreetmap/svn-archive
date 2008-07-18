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

package org.openstreetmap.josmng.osm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A representation of a single OSM way. Contains an ordered list of way nodes
 * and all the way metadata.
 * 
 * @author nenik
 */
public class Way extends OsmPrimitive {
    private static final Node[] EMPTY_NODES = new Node[0];
    private Node[] nodes = EMPTY_NODES;

    Way(DataSet constructed, long id, int time, String user, boolean vis, Node[] nodes) {
        super(constructed, id, time, user, vis);
        if (nodes != null) {
            for (Node n : new HashSet<Node>(Arrays.asList(nodes))) n.addReferrer(this);
            this.nodes = nodes.clone();
        }
    }
    
    public List<Node> getNodes() {
        return Collections.unmodifiableList(Arrays.asList(nodes));
    }
    
    public void setNodes(List<Node> n) {
        ChangeNodesEdit ch = new ChangeNodesEdit();
        setNodesImpl(n.toArray(new Node[n.size()]));
        source.postEdit(ch);
    }

    @Override public void visit(Visitor v) {
        v.visit(this);
    }
    
    void setNodesImpl(Node[] n) {
        // TODO: compute minimal reference change
        for(Node act : new HashSet<Node>(Arrays.asList(nodes))) act.removeReferrer(this);
        nodes = n;
        for(Node act : new HashSet<Node>(Arrays.asList(nodes))) act.addReferrer(this);
        
        source.fireWayNodesChanged(this);
    }

    
    @Override void setDeletedImpl(boolean deleted) {
        if (deleted) {
            for(Node act : new HashSet<Node>(Arrays.asList(nodes))) act.removeReferrer(this);
        } else {
            for(Node act : new HashSet<Node>(Arrays.asList(nodes))) act.addReferrer(this);
        }
        super.setDeletedImpl(deleted);
    }

    public @Override String toString() {
        return super.toString() + '[' + nodes.length + "]";
    }

    private class ChangeNodesEdit extends PrimitiveToggleEdit {
        Node[] savedNodes;

        public ChangeNodesEdit() {
            super("change way nodes");
            savedNodes = nodes;
        }
        
        protected void toggle() {
            Node[] orig = nodes;
            setNodesImpl(savedNodes);
            savedNodes = orig;
        }
    }

}
