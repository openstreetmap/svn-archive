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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test that verifies many aspects of the undo system implementation
 * in the DataSet.
 *
 * @author nenik
 */
public class ParserTest {
    private static final double EPSILON = 1e-9;
    private DataSet data;
    
    
    public ParserTest() {
        try {
            data = DataSet.fromStream(getClass().getResourceAsStream("testdata.osm"));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
    }
    
    void checkFlags(OsmPrimitive prim, boolean visible, boolean modified, boolean deleted) {
        assertNotNull(prim);
        assertEquals(visible, prim.isVisible());
        assertEquals(modified, prim.isModified());
        assertEquals(deleted, prim.isDeleted());
    }
    
    private Collection<Node> getNodes() {
        Collection<Node> nodes = new ArrayList<Node>();
        for (OsmPrimitive prim : data.getPrimitives(Bounds.WORLD)) {
            if (prim instanceof Node) nodes.add((Node)prim);
        }
        return nodes;
    }

    private Collection<Way> getWays() {
        Collection<Way> ways = new ArrayList<Way>();
        for (OsmPrimitive prim : data.getPrimitives(Bounds.WORLD)) {
            if (prim instanceof Way) ways.add((Way)prim);
        }
        return ways;
    }

    private Collection<Relation> getRelations() {
        Collection<Relation> rels = new ArrayList<Relation>();
        for (OsmPrimitive prim : data.getPrimitives(Bounds.WORLD)) {
            if (prim instanceof Relation) rels.add((Relation)prim);
        }
        return rels;
    }
    
    public @Test void testIndividualEdits() {
        assertEquals(4, getNodes().size());

        int zeros = 0;
        for (Node n : getNodes()) {
            assertFalse(n.getId() < 0);
            if (n.getId() == 0) zeros++;
        }
        assertEquals(1, zeros);

        checkFlags(data.getNode(42), true, false, false);
        checkFlags(data.getNode(43), true, true, false);
        checkFlags(data.getNode(44), true, false, true);

        zeros = 0;
        for (Way w : getWays()) {
            assertFalse(w.getId() < 0);
            if (w.getId() == 0) zeros++;
        }
        assertEquals(1, zeros);
        
        Way w314 = data.getWay(314);
        checkFlags(w314, true, true, false);
        assertEquals("Nenik", w314.getUser());
        
        checkFlags(data.getWay(315), true, false, true);
        
        zeros = 0;
        for (Relation r : getRelations()) {
            assertFalse(r.getId() < 0);
            if (r.getId() == 0) zeros++;
        }
        assertEquals(1, zeros);

        Relation r315 = data.getRelation(315);
        checkFlags(r315, true, false, false);
        assertEquals("route", r315.getTag("type"));
        assertEquals("", r315.getMembers().get(w314));
    }

}