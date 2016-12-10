// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class RoadTypeTestTest extends AbstractTest {

    @Test
    public void test() {

        File file = new File(AbstractTest.PATH_TO_ROAD_TYPE_ERROR);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();
        List<TestError> errors = new ArrayList<>();

        for (Relation r: ds.getRelations()) {
            WayChecker wayChecker = new WayChecker(r, test);
            wayChecker.performRoadTypeTest();
            errors.addAll(wayChecker.getErrors());
        }

        assertEquals(errors.size(), 2);

        for (TestError e: errors) {
            assertEquals(e.getCode(), PTAssistantValidatorTest.ERROR_CODE_ROAD_TYPE);
            Way way = (Way) e.getHighlighted().iterator().next();
            assertTrue(way.getId() == 8169083 || way.getId() == 8034569);
        }
    }
}
