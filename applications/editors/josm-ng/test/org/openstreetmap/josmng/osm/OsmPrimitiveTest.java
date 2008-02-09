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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nenik
 */
public class OsmPrimitiveTest {

    private static final String DEF_KEY = "created_by"; // interned instance
    private static final String DEF_KEY2 = new String(DEF_KEY); // equivalent one
    private static final String SOME_KEY = "some"; // interned instance
    private static final String SOME_KEY2 = new String(SOME_KEY); // equivalent one

    public OsmPrimitiveTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testEmptyTags() {
        System.out.println("testEmptyTags");
        OsmPrimitive instance;
        instance = new Prim();
        assertEquals(0, instance.getTags().length);
        assertNull(instance.getTag(DEF_KEY));
        assertNull(instance.getTag(DEF_KEY2));
        assertNull(instance.getTag(SOME_KEY));
        assertNull(instance.getTag(SOME_KEY2));
        assertNull(instance.removeTag(DEF_KEY));
        assertNull(instance.removeTag(DEF_KEY2));
        assertNull(instance.removeTag(SOME_KEY));
        assertNull(instance.removeTag(SOME_KEY2));
    }

    @Test
    public void testPutDefAndSome() {
        System.out.println("testPutDefAndSome");
        OsmPrimitive instance = new Prim();

        // (re)put default
        assertNull(instance.putTag(DEF_KEY, "aaa"));
        assertEquals("aaa", instance.putTag(DEF_KEY2, "bbb"));
        assertEquals(new String[] {DEF_KEY}, instance.getTags());
        assertEquals("bbb", instance.getTag(DEF_KEY));

        // (re)put some
        assertNull(instance.putTag(SOME_KEY, "ccc"));
        assertEquals("ccc", instance.putTag(SOME_KEY2, "ddd"));
        assertEquals(new String[] {DEF_KEY, SOME_KEY}, instance.getTags());
        assertEquals("ddd", instance.getTag(SOME_KEY2));
        assertEquals("bbb", instance.getTag(DEF_KEY));
        
        // remove default
        assertEquals("bbb", instance.removeTag(DEF_KEY2));
        assertNull(instance.getTag(DEF_KEY2));
        assertEquals(new String[] {SOME_KEY}, instance.getTags());
        
        // remove some
        assertEquals("ddd", instance.removeTag(SOME_KEY2));
        assertNull(instance.getTag(SOME_KEY2));
        assertEquals(0, instance.getTags().length);
    }

    @Test
    public void testPutSomeAndDef() {
        System.out.println("testPutSomeAndDef");
        OsmPrimitive instance = new Prim();

        // (re)put some
        assertNull(instance.putTag(SOME_KEY, "ccc"));
        assertEquals("ccc", instance.putTag(SOME_KEY2, "ddd"));
        assertEquals(new String[] {SOME_KEY}, instance.getTags());
        assertEquals("ddd", instance.getTag(SOME_KEY2));
        assertEquals(null, instance.getTag(DEF_KEY));

        // (re)put default
        assertNull(instance.putTag(DEF_KEY, "aaa"));
        assertEquals("aaa", instance.putTag(DEF_KEY2, "bbb"));
        assertEquals(new String[] {DEF_KEY, SOME_KEY}, instance.getTags());
        assertEquals("bbb", instance.getTag(DEF_KEY));

        
        // remove default
        assertEquals("bbb", instance.removeTag(DEF_KEY2));
        assertNull(instance.getTag(DEF_KEY2));
        assertEquals(new String[] {SOME_KEY}, instance.getTags());
        
        // remove some
        assertEquals("ddd", instance.removeTag(SOME_KEY2));
        assertNull(instance.getTag(SOME_KEY2));
        assertEquals(0, instance.getTags().length);
    }

    @Test
    public void testAddRemoveMany() {
        System.out.println("testAddMany");
        OsmPrimitive instance = new Prim();
        
        for (int i=0; i<10; i++) {
            assertNull(instance.putTag("k" + i, "v" + i));
            assertEquals(i+1, instance.getTags().length);
        }
        
        for (int i=0; i<10; i++) {
            assertEquals("v" + i, instance.getTag("k" + i));
        }
        
        for (int i=0; i<10; i++) {
            assertEquals("v" + i, instance.removeTag("k" + i));
            assertEquals(9-i, instance.getTags().length);
        }
    }

    private static DataSet testing = new DataSet();
    
    private static class Prim extends OsmPrimitive {
        public Prim() {
            super(testing, 1, "", "", true);
        }        
    }
}