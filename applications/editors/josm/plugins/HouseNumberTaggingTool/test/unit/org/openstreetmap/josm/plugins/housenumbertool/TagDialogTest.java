// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.housenumbertool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests of {@link TagDialog}.
 */
public class TagDialogTest {

    /**
     * Unit test of {@link TagDialog#incrementHouseNumber}
     */
    @Test
    public void testIncrementHouseNumber() {
        assertEquals("2", TagDialog.incrementHouseNumber("1", 1));
        assertEquals("12", TagDialog.incrementHouseNumber("10", 2));
        assertEquals("2A", TagDialog.incrementHouseNumber("1A", 1));
        assertEquals("E2", TagDialog.incrementHouseNumber("E1", 1));
        //assertEquals("۲", TagDialog.incrementHouseNumber("۱", 1)); // FIXME: how to increment persian numbers ?
        assertEquals("2", TagDialog.incrementHouseNumber("۱", 1));
        assertNull(TagDialog.incrementHouseNumber(null, 1));
    }
}
