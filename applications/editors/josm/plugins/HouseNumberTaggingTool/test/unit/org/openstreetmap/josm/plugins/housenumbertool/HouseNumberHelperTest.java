// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.housenumbertool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests of {@link TagDialog}.
 */
public class HouseNumberHelperTest {

    /**
     * Unit test of {@link HouseNumberHelper#incrementHouseNumber}
     */
    @Test
    public void testIncrementHouseNumber() {
        assertEquals("2", HouseNumberHelper.incrementHouseNumber("1", 1));
        assertEquals("12", HouseNumberHelper.incrementHouseNumber("10", 2));
        assertEquals("2A", HouseNumberHelper.incrementHouseNumber("1A", 1));
        assertEquals("E2", HouseNumberHelper.incrementHouseNumber("E1", 1));
        //assertEquals("۲", HouseNumberHelper.incrementHouseNumber("۱", 1)); // FIXME: how to increment persian numbers ?
        assertEquals("2", HouseNumberHelper.incrementHouseNumber("۱", 1));
        assertNull(HouseNumberHelper.incrementHouseNumber(null, 1));
    }
}
