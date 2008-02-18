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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.undo.UndoManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test that verifies every model change is properly notified, undo/redo
 * caused changes nonwithstading.
 *
 * @author nenik
 */
public class EventsTest {
    
    private DataSet data = new DataSet();
    private Node n1 = new Node(data, 1, 10, 11, null, null, true); 
    private Node n2 = new Node(data, 2, 20, 21, null, null, true); 
    private Node n3 = new Node(data, 3, 30, 31, null, null, true); 
    private Way w1 = new Way(data, 1, null, null, true);
    private Way w2 = new Way(data, 2, null, null, true);
    UndoManager undo = new UndoManager();
    Listener l = new Listener();
    
    {
        data.addNode(n1);
        data.addNode(n2);
        data.addNode(n3);
        w1.setNodes(Arrays.asList(n1,n2,n3));
        w2.setNodes(Arrays.asList(n3,n1));
        data.addWay(w1);
        data.addWay(w2);
        data.addUndoableEditListener (undo);
        data.addDataSetListener(l);
    }

    public EventsTest() {
    }

    public @Test void testAdd() {
        data.addNode(1, 1);
        l.check(1, 0, 0, 0, 0);
        
        undo.undo();
        l.check(1, 1, 0, 0, 0);

        undo.redo();
        l.check(2, 1, 0, 0, 0);
    }
    
    public @Test void testRemove() {
        data.removeWay(w2);
        l.check(0, 1, 0, 0, 0);
        
        undo.undo();
        l.check(1, 1, 0, 0, 0);

        undo.redo();
        l.check(1, 2, 0, 0, 0);
    }

    private class Listener implements DataSetListener {
        List<OsmPrimitive> added = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> removed = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> changed = new ArrayList<OsmPrimitive>();
        List<Node> movedNodes = new ArrayList<Node>();
        List<Way> waysChanged = new ArrayList<Way>();

        public void primtivesAdded(Collection<? extends OsmPrimitive> add) {
            added.addAll(add);
        }

        public void primtivesRemoved(Collection<? extends OsmPrimitive> rem) {
            removed.addAll(rem);
        }

        public void tagsChanged(OsmPrimitive prim) {
            changed.add(prim);
        }

        public void nodeMoved(Node node) {
            movedNodes.add(node);
        }

        public void wayNodesChanged(Way way) {
            waysChanged.add(way);
        }
        
        void check(int add, int rem, int change, int nodes, int ways) {
            assertEquals(add, added.size());
            assertEquals(rem, removed.size());
            assertEquals(change, changed.size());
            assertEquals(nodes, movedNodes.size());
            assertEquals(ways, waysChanged.size());
        }
    }
    
}