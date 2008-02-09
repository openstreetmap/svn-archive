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

package org.openstreetmap.josmng.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test all the set-like functionality on Strings and also foreign-key
 * behaviour based on first letter.
 * 
 * @author nenik
 */
public class StorageStringTest extends StorageTestHid<String> {

    public StorageStringTest() {
    }


    // enough for rehash
    private static String[] TEST_STRINGS = new String[] {
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m"
    };
    
    protected List<String> getTestData() {
        return Arrays.asList(TEST_STRINGS);
    }    

    protected String createEqualObject(String str) {
        return new String(str);
    }
    
    public @Test void testGetThroughForeignKey() {
        Map<Character,String> fk = this.storage.foreignKey(new CharHash());
        
        // check presence of original instances
        for (String s : master) {
            char c = s.charAt(0);
            assertSame(s, fk.get(c));
        }
    }
    
    public @Test void testManyManyPuts() {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        String failure = "fail for seed=" + seed;
        
        Storage<String> s = new Storage();
        
        for (int i=0; i<1000000; i++) {
            int nxt = random.nextInt();
            s.putUnique("" + nxt);
        }
    }

    
    private static class CharHash implements Hash<Character,String> {
        String toStr(Character c) {
            return "" + c;
        }
        
        public int getHashCode(Character k) {
            return toStr(k).hashCode();
        }

        public boolean equals(Character k, String t) {
            return t.equals(toStr(k));
        }
    } 

}
