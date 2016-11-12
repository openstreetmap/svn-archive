// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class SegmentCheckerTest extends AbstractTest {

    @Test
    public void test() {


        File file = new File(AbstractTest.PATH_TO_SEGMENT_TEST);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
        PTAssistantValidatorTest test = new PTAssistantValidatorTest();

        Relation route = null;

        for (Relation r: ds.getRelations()) {
            if (RouteUtils.isTwoDirectionRoute(r)) {
                route = r;
                break;
            }
        }

        SegmentChecker segmentChecker = new SegmentChecker(route, test);
        segmentChecker.performStopByStopTest();
        assertEquals(SegmentChecker.getCorrectSegmentCount(), 27);
        assertEquals(segmentChecker.getErrors().size(), 0);




    }
}
