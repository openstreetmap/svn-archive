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
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * A representation of a single OSM way. Contains an ordered list of way nodes
 * and all the way metadata.
 * 
 * @author nenik
 */
public class Way extends OsmPrimitive {
    private List<Node> nodes = Collections.EMPTY_LIST;

    public Way(DataSet constructed, long id, String time, String user, boolean vis) {
        super(constructed, id, time, user, vis);
    }
    
    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }
    
    public void setNodes(List<Node> n) {
        ChangeNodesEdit ch = new ChangeNodesEdit();
        source.postEdit(ch);
        setNodesImpl(n.toArray(new Node[n.size()]));
    }
    
    private void setNodesImpl(Node[] n) {
        nodes = Arrays.asList(n);
        source.fireWayNodesChanged(this);
    }

    private class ChangeNodesEdit extends AbstractUndoableEdit {
        Node[] savedNodes;

        public ChangeNodesEdit() {
            savedNodes = nodes.toArray(new Node[nodes.size()]);
        }

        public @Override String getPresentationName() {
            return "change way nodes";
        }

        public @Override void undo() throws CannotUndoException {
            super.undo(); // to validate
            switchNodes();
        }

        public @Override void redo() throws CannotRedoException {
            super.redo(); // to validate
            switchNodes();
        }
        
        private void switchNodes() {
            Node[] orig = nodes.toArray(new Node[nodes.size()]);
            setNodesImpl(savedNodes);
            savedNodes = orig;
        }
    }

}
