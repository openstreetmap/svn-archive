// License: GPL. For details, see LICENSE file.
package model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import model.TagCatalog.IndoorObject;

/**
 * Unit tests of {@link PresetCounter} class.
 */
public class PresetCounterTest {

    /**
     * Test case for testing the ranking functionality.
     */
    @Test
    public void testRanking() {
        // input preparation
        PresetCounter counter = new PresetCounter();

        counter.count(IndoorObject.CONCRETE_WALL);
        counter.count(IndoorObject.CONCRETE_WALL);
        counter.count(IndoorObject.CONCRETE_WALL);
        counter.count(IndoorObject.ROOM);
        counter.count(IndoorObject.ROOM);
        counter.count(IndoorObject.STEPS);
        counter.count(IndoorObject.TOILET_MALE);

        List<IndoorObject> actualList = counter.getRanking();

        //expectation
        List<IndoorObject> expectedList = new ArrayList<>();
        expectedList.add(IndoorObject.CONCRETE_WALL);
        expectedList.add(IndoorObject.ROOM);
        expectedList.add(IndoorObject.TOILET_MALE);
        expectedList.add(IndoorObject.STEPS);

        //assertion
        assertEquals(expectedList.get(0), actualList.get(0));
        assertEquals(expectedList.get(1), actualList.get(1));
        assertEquals(expectedList.get(2), actualList.get(2));
        assertEquals(expectedList.get(3), actualList.get(3));
    }
}
