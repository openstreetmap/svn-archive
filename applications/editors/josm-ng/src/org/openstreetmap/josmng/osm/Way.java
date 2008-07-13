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
        if (nodes != null) this.nodes = nodes.clone();
    }
    
    public List<Node> getNodes() {
        return Collections.unmodifiableList(Arrays.asList(nodes));
    }
    
    public void setNodes(List<Node> n) {
        ChangeNodesEdit ch = new ChangeNodesEdit();
        setNodesImpl(n.toArray(new Node[n.size()]));
        source.postEdit(ch);
    }

    @Override void visit(Visitor v) {
        v.visit(this);
    }
    
    void setNodesImpl(Node[] n) {
        nodes = n;
        source.fireWayNodesChanged(this);
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
