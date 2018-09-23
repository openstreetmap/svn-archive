// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests of {@link TileRange} class.
 */
public class TileRangeTest {

    /**
     * Unit test of {@link TileRange#size}.
     */
    @Test
    public void testSize() {
        assertEquals(16, new TileRange(
                new TileXY(3, 3), 
                new TileXY(6, 6), 10).size());
    }
}
