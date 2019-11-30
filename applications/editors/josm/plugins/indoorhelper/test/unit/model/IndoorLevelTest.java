// License: GPL. For details, see LICENSE file.
package model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests of {@link IndoorLevel} class.
 */
public class IndoorLevelTest {

    /**
     * Test case for {@link IndoorLevel#isisPartOfWorkingLevel} method.
     */
    @Test
    public void testIsPartOfWorkingLevel() {
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -3));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -2));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-3--1", -1));
        assertFalse(IndoorLevel.isPartOfWorkingLevel("-3--1", 0));

        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", -1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("-1;0;1", 1));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("1;2", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1;2", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1;2", 2));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("1", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("1", 1));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("0-3", -1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 0));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("0-3", 3));

        assertFalse(IndoorLevel.isPartOfWorkingLevel("2;3;4", 1));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 2));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 3));
        assertTrue(IndoorLevel.isPartOfWorkingLevel("2;3;4", 4));
    }
}
