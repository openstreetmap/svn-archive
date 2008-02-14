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
import javax.swing.undo.UndoManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test that verifies many aspects of the undo system implementation
 * in the DataSet.
 *
 * @author nenik
 */
public class DataSetTest {
    private static final double EPSILON = 1e-9;
     
    private DataSet data = new DataSet();
    private Node n1 = new Node(data, 1, 10, 11, null, null, true); 
    private Node n2 = new Node(data, 2, 20, 21, null, null, true); 
    private Node n3 = new Node(data, 3, 30, 31, null, null, true); 
    private Way w1 = new Way(data, 1, null, null, true);
    private Way w2 = new Way(data, 2, null, null, true);
    UndoManager undo = new UndoManager();
    
    {
        data.addNode(n1);
        data.addNode(n2);
        data.addNode(n3);
        w1.setNodes(Arrays.asList(n1,n2,n3));
        w2.setNodes(Arrays.asList(n3,n1));
        data.addWay(w1);
        data.addWay(w2);
        data.addUndoableEditListener (undo);
    }

    public DataSetTest() {
    }

    private void checkPrimitives(boolean modified, double ... vals) {
        assertEquals(modified, n1.isModified());
        assertEquals(n1.getLatitude(), vals[0], EPSILON);
        assertEquals(n1.getLongitude(), vals[1], EPSILON);
        if (vals.length == 2) return;
        
        assertEquals(modified, n2.isModified());
        assertEquals(n2.getLatitude(), vals[2], EPSILON);
        assertEquals(n2.getLongitude(), vals[3], EPSILON);
        if (vals.length == 4) return;
        
        assertEquals(modified, n3.isModified());
        assertEquals(n3.getLatitude(), vals[4], EPSILON);
        assertEquals(n3.getLongitude(), vals[5], EPSILON);
    }
    
    public @Test void testIndividualEdits() {
        // empty queue at the beginning
        assertFalse(undo.canUndo());
        assertFalse(undo.canRedo());
        
        assertFalse(n1.isModified());
        n1.setCoordinate(new CoordinateImpl(15, 16));

        // check it was really set
        checkPrimitives(true, 15, 16);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // undo and verify the original state
        undo.undo();
        checkPrimitives(false, 10, 11);
        assertFalse(undo.canUndo());
        assertTrue(undo.canRedo());

        // redo and verify the changed state
        undo.redo();
        checkPrimitives(true, 15, 16);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());
    }

    public @Test void testPositionEditComposition() {
        // empty queue at the beginning
        assertFalse(undo.canUndo());
        assertFalse(undo.canRedo());
        assertFalse(n1.isModified());
        
        //perform two back-to-back edits
        n1.setCoordinate(new CoordinateImpl(15, 16));
        n1.setCoordinate(new CoordinateImpl(17, 18));

        // check the final state
        checkPrimitives(true, 17, 18);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // undo and verify the very original state
        undo.undo();
        checkPrimitives(false, 10, 11);
        assertFalse(undo.canUndo());
        assertTrue(undo.canRedo());

        // redo and verify the very final state
        undo.redo();
        checkPrimitives(true, 17, 18);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());
    }

    
    
    public @Test void testAtomicEdit() {
        // empty queue at the beginning
        assertFalse(undo.canUndo());
        assertFalse(undo.canRedo());
        checkPrimitives(false, 10, 11, 20, 21, 30, 31);
        
        // modify all and check
        data.atomicEdit(new Runnable() {public void run() {
            n1.setCoordinate(new CoordinateImpl(15, 16));
            n2.setCoordinate(new CoordinateImpl(25, 26));
            n3.setCoordinate(new CoordinateImpl(35, 36));
        }}, null);
        checkPrimitives(true, 15, 16, 25, 26, 35, 36);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // undo and verify the original state
        undo.undo();
        checkPrimitives(false, 10, 11, 20, 21, 30, 31);
        assertFalse(undo.canUndo());
        assertTrue(undo.canRedo());

        // redo and verify the changed state
        undo.redo();
        checkPrimitives(true, 15, 16, 25, 26, 35, 36);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());
    }

    public @Test void testAtomicEditComposition() {
        // empty queue at the beginning
        assertFalse(undo.canUndo());
        assertFalse(undo.canRedo());
        checkPrimitives(false, 10, 11, 20, 21, 30, 31);
        Object token = new Object();
        
        // two back-to-back multiedits
        data.atomicEdit(new Runnable() {public void run() {
            n1.setCoordinate(new CoordinateImpl(15, 16));
            n2.setCoordinate(new CoordinateImpl(25, 26));
            n3.setCoordinate(new CoordinateImpl(35, 36));
        }}, token);

        data.atomicEdit(new Runnable() {public void run() {
            n1.setCoordinate(new CoordinateImpl(17, 18));
            n2.setCoordinate(new CoordinateImpl(27, 28));
            n3.setCoordinate(new CoordinateImpl(37, 38));
        }}, token);
        
        // check the final state
        checkPrimitives(true, 17, 18, 27, 28, 37, 38);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // undo and verify the very original state
        undo.undo();
        checkPrimitives(false, 10, 11, 20, 21, 30, 31);
        assertFalse(undo.canUndo());
        assertTrue(undo.canRedo());

        // redo and verify the very final state
        undo.redo();
        checkPrimitives(true, 17, 18, 27, 28, 37, 38);
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());
    }
    
    public @Test void testIndividualEditTags() {
        // empty queue at the beginning
        assertFalse(undo.canUndo());
        assertFalse(undo.canRedo());
        assertFalse(n1.isModified());

        // change and check it was really set
        n1.putTag("foo", "bar");
        assertTrue(n1.isModified());
        assertEquals("bar", n1.getTag("foo"));
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // change once more
        n1.putTag("baz", "kaz");
        assertTrue(n1.isModified());
        assertEquals("kaz", n1.getTag("baz"));
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());

        // undo once and verify half the state
        undo.undo();
        assertTrue(n1.isModified());
        assertEquals(null, n1.getTag("baz"));
        assertEquals("bar", n1.getTag("foo"));
        assertTrue(undo.canUndo());
        assertTrue(undo.canRedo());

        // undo the second time and verify the initial state
        undo.undo();
        assertFalse(n1.isModified());
        assertEquals(null, n1.getTag("baz"));
        assertEquals(null, n1.getTag("foo"));
        assertEquals(0, n1.getTags().length);
        assertFalse(undo.canUndo());
        assertTrue(undo.canRedo());

        // redo twice and verify
        undo.redo();
        undo.redo();
        assertTrue(n1.isModified());
        assertEquals("bar", n1.getTag("foo"));
        assertEquals("kaz", n1.getTag("baz"));
        assertTrue(undo.canUndo());
        assertFalse(undo.canRedo());
    }

    public @Test void testCollisionInsideAtomic() {
        n1.putTag("key1", "val1A");
        n1.putTag("key2", "val2A");
        n1.putTag("created_by", "test");
        
        data.atomicEdit(new Runnable() {public void run() {
            n1.removeTag("key2");
            n1.putTag("key1", "val1B");
            n1.putTag("key2", "val2B");
            n1.removeTag("created_by");
            n1.removeTag("key1");
        }}, null);

        assertEquals(null, n1.getTag("created_by"));
        assertEquals(null, n1.getTag("key1"));
        assertEquals("val2B", n1.getTag("key2"));

        undo.undo();
        assertEquals("val1A", n1.getTag("key1"));
        assertEquals("val2A", n1.getTag("key2"));
        assertEquals("test", n1.getTag("created_by"));
        
        undo.redo();
        assertEquals(null, n1.getTag("created_by"));
        assertEquals(null, n1.getTag("key1"));
        assertEquals("val2B", n1.getTag("key2"));
    }
    
    public @Test void testAtomicallyDeletePrimitives() {
        data.atomicEdit(new Runnable() {public void run() {
            data.removeWay(w1);
            data.removeNode(n2);
        }}, null);
        
        assertNull(data.getWay(w1.getId()));
        assertNull(data.getNode(n2.getId()));

        undo.undo();
        assertSame(w1, data.getWay(w1.getId()));
        assertSame(n2, data.getNode(n2.getId()));

        undo.redo();
        assertNull(data.getWay(w1.getId()));
        assertNull(data.getNode(n2.getId()));
    }
    
    
    /*
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void atomicEdit() {
    }

    @Test
    public void removeUndoableEditListener() {
    }

    @Test
    public void addUndoableEditListener() {
    }

    @Test
    public void addDataSetListener() {
    }

    @Test
    public void removeDataSetListener() {
    }

    @Test
    public void addNode() {
    }

    @Test
    public void removeNode() {
    }

    @Test
    public void getNodes() {
    }

    @Test
    public void getNode() {
    }

    @Test
    public void addWay() {
    }

    @Test
    public void removeWay() {
    }

    @Test
    public void getWays() {
    }

    @Test
    public void getWay() {
    }

    @Test
    public void addRelation() {
    }

    @Test
    public void removeRelation() {
    }

    @Test
    public void getRelations() {
    }

    @Test
    public void getRelation() {
    }
*/
}