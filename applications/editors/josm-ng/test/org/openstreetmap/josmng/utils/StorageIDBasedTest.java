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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test all the ste-like functionality on opaque objects with ID and also
 * foreign-key behaviour based on the ID.
 *
 * @author nenik
 */
public class StorageIDBasedTest extends StorageTestHid<StorageIDBasedTest.IDBased> {

    public StorageIDBasedTest() {
    }

    protected List<IDBased> getTestData() {
        List<IDBased> data = new ArrayList<IDBased>();
        for (int i=0; i<13; i++) {
            data.add(new IDBased(128*(i+3))); // cause heavy hash aliasing
        }
        return data;
    }

    protected @Override Hash<StorageIDBasedTest.IDBased, StorageIDBasedTest.IDBased> createHash() {
        return new IDHash();
    }

    protected IDBased createEqualObject(IDBased id) {
        return new IDBased(id.id);
    }
    
    public @Test void testGetThroughForeignKey() {
        Map<Integer,IDBased> fk = storage.foreignKey(new IDIntHash());
        
        // check presence of original instances
        for (IDBased s : master) {
            int i = s.id;
            assertSame(s, fk.get(i));
        }
    }

    private class IDHash implements Hash<IDBased, IDBased> {
        public int getHashCode(IDBased k) {
            return k.id;
        }

        public boolean equals(IDBased t1, IDBased t2) {
            return t1.id == t2.id;
        }
    }

    private class IDIntHash implements Hash<Integer, IDBased> {
        public int getHashCode(Integer k) {
            return k.intValue();
        }

        public boolean equals(Integer k, IDBased t) {
            return t.id == k.intValue();
        }
    }
    
    public class IDBased {
        int id;

        IDBased(int id) {
            this.id = id;
        }
    }
}
