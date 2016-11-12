// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class PlatformAsWayTest extends AbstractTest {

    @Test
    public void sortingTest() {
        File file = new File(AbstractTest.PATH_TO_PLATFORM_AS_WAY);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();

        List<TestError> errors = new ArrayList<>();

        for (Relation r: ds.getRelations()) {
            WayChecker wayChecker = new WayChecker(r, test);
            wayChecker.performDirectionTest();
            wayChecker.performRoadTypeTest();
            errors.addAll(wayChecker.getErrors());
            RouteChecker routeChecker = new RouteChecker(r, test);
            routeChecker.performSortingTest();
            errors.addAll(routeChecker.getErrors());
        }

        assertEquals(errors.size(), 0);
    }
}
