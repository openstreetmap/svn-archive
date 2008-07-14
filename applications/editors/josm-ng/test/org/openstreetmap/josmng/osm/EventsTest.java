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
import java.util.Collections;
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
    
    private DataSet data = DataSet.empty();
    private Node n1 = data.createNode(10, 11);
    private Node n2 = data.createNode(20, 21); 
    private Node n3 = data.createNode(30, 31); 
    private Way w1 = data.createWay(n1, n2, n3);
    private Way w2 = data.createWay(n3, n1);
    private Relation r1 = data.createRelation(Collections.singletonMap((OsmPrimitive)n1, "n"));
    private Relation r2 = data.createRelation(Collections.singletonMap((OsmPrimitive)w1, "w"));
    UndoManager undo = new UndoManager();
    Listener l = new Listener();
    
    {
        data.addUndoableEditListener (undo);
        data.addDataSetListener(l);
    }

    public EventsTest() {
    }

    public @Test void testAdd() {
        data.createNode(1, 1);
        l.check(1, 0, 0, 0, 0, 0);
        
        undo.undo();
        l.check(1, 1, 0, 0, 0, 0);

        undo.redo();
        l.check(2, 1, 0, 0, 0, 0);
    }
    
    public @Test void testRemove() {
        w2.delete();
        l.check(0, 1, 0, 0, 0, 0);
        
        undo.undo();
        l.check(1, 1, 0, 0, 0, 0);

        undo.redo();
        l.check(1, 2, 0, 0, 0, 0);
    }
    
    public @Test void testMembers() {
        r1.removeMember(n1);
        l.check(0, 0, 0, 0, 0, 1);
        
        undo.undo();
        l.check(0, 0, 0, 0, 0, 2);

        undo.redo();
        l.check(0, 0, 0, 0, 0, 3);
        
        r2.addMember(w2, "w");
        l.check(0, 0, 0, 0, 0, 4);
        
        undo.undo();
        l.check(0, 0, 0, 0, 0, 5);

        undo.redo();
        l.check(0, 0, 0, 0, 0, 6);
    }
    

    private class Listener implements DataSetListener {
        List<OsmPrimitive> added = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> removed = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> changed = new ArrayList<OsmPrimitive>();
        List<Node> movedNodes = new ArrayList<Node>();
        List<Way> waysChanged = new ArrayList<Way>();
        List<Relation> relationsChanged = new ArrayList<Relation>();

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

        public void relationMembersChanged(Relation r) {
            relationsChanged.add(r);
        }

        void check(int add, int rem, int change, int nodes, int ways, int relations) {
            assertEquals(add, added.size());
            assertEquals(rem, removed.size());
            assertEquals(change, changed.size());
            assertEquals(nodes, movedNodes.size());
            assertEquals(ways, waysChanged.size());
            assertEquals(relations, relationsChanged.size());
        }
    }
    
}