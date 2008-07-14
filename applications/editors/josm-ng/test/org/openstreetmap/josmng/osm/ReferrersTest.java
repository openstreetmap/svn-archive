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
import java.util.Collection;
import java.util.Collections;
import javax.swing.undo.UndoManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the referrers integrity across DataSet creation, modification and undo.
 * 
 * @author nenik
 */
public class ReferrersTest {
    UndoManager undo = new UndoManager();

    DataSet data;
    Node n1, n2, n3, n4, n5;
    Way floor, cup, house;
    Relation r1;

    public @Test void testCreatedDirectly() {
        data = DataSet.empty();
        data.addUndoableEditListener (undo);
        
        n1 = data.createNode(10, 10);
        n2 = data.createNode(10, 20);
        n3 = data.createNode(20, 20);
        n4 = data.createNode(20, 10);
        n5 = data.createNode(15, 25);
        
        floor = data.createWay(n1, n4);
        checkReferrers(n1, floor);
        checkReferrers(n2);
        checkReferrers(n4, floor);

        cup = data.createWay(n1, n2, n3, n4);
        checkReferrers(n1, floor, cup);
        checkReferrers(n2, cup);
        checkReferrers(n4, floor, cup);
        
        house = data.createWay(); // will be house
        r1 = data.createRelation(Collections.singletonMap((OsmPrimitive)floor, "floor"));
        commonCheck();
    }
    
    private void commonCheck() {
        house.setNodes(Arrays.asList(n1, n4, n3, n5, n2, n3, n1, n2, n4));
        checkReferrers(n1, floor, cup, house);
        checkReferrers(n2, cup, house);
        checkReferrers(n3, cup, house);
        checkReferrers(n4, floor, cup, house);
        checkReferrers(n5, house);
        
        undo.undo();
        checkReferrers(n5);
        checkReferrers(n1, floor, cup);
        checkReferrers(n2, cup);
        checkReferrers(n3, cup);
        checkReferrers(n4, floor, cup);
        
        undo.redo();
        checkReferrers(n1, floor, cup, house);
        checkReferrers(n2, cup, house);
        checkReferrers(n3, cup, house);
        checkReferrers(n4, floor, cup, house);
        checkReferrers(n5, house);
        
        checkReferrers(floor, r1);
        r1.addMember(house, "house");
        checkReferrers(house, r1);

        r1.removeMember(floor);
        checkReferrers(floor);
        
        undo.undo();
        checkReferrers(floor, r1);
        checkReferrers(house, r1);

        undo.undo();
        checkReferrers(floor, r1);
        checkReferrers(house);
        
        // delete a referrer and check the rest
        cup.delete();
        checkReferrers(n2, house);
        checkReferrers(n3, house);
        
        // delete a referrer relation and check the rest
        r1.delete();
        checkReferrers(floor);
        
        // undo the first deletion
        undo.undo();
        checkReferrers(floor, r1);
        checkReferrers(n3, house);
        
        // undo the second
        undo.undo();
        checkReferrers(n3, house, cup);
        
    }

    public @Test void testFactoryCreated() {
        DataSet.Factory fact = DataSet.factory(100);

        n1 = fact.node(1, 10, 10, -1, "nobody", true);
        n2 = fact.node(2, 10, 20, -1, "nobody", true);
        n3 = fact.node(3, 20, 20, -1, "nobody", true);
        n4 = fact.node(4, 20, 10, -1, "nobody", true);
        n5 = fact.node(5, 15, 25, -1, "nobody", true);
        floor = fact.way(1, -1, "nobody", true, new Node[] { n1, n4 });
        cup = fact.way(2, -1, "nobody", true, new Node[] {n1, n2, n3, n4});        
        house = fact.way(3, -1, "nobody", true, null);
        r1 = fact.relation(1, -1, "nobody", true, Collections.singletonMap((OsmPrimitive)floor, "floor"));
        data = fact.create();
        data.addUndoableEditListener (undo);
        
        checkReferrers(n1, floor, cup);
        checkReferrers(n2, cup);
        checkReferrers(n4, floor, cup);
        commonCheck();
    }

    private void checkReferrers(OsmPrimitive prim, OsmPrimitive ... refs) {
        Collection<OsmPrimitive> r = prim.getReferrers();
        assertEquals(refs.length, r.size());
        for (OsmPrimitive act : refs) assertTrue(r.contains(act));
    }
    
}
