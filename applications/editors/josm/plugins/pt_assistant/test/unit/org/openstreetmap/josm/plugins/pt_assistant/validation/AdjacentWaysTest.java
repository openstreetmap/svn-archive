// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class AdjacentWaysTest extends AbstractTest {

    @Test
    public void test1() {

        File file = new File(AbstractTest.PATH_TO_ONEWAY_WRONG_DIRECTION);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();
        long id = 24215210;
        Way way = (Way) ds.getPrimitiveById(id, OsmPrimitiveType.WAY);

        assertEquals(RouteUtils.isOnewayForPublicTransport(way), -1);

        Relation route = null;
        for (Relation r : ds.getRelations()) {
            if (r.hasKey("route")) {
                route = r;
            }
        }

        WayChecker wayChecker = new WayChecker(route, test);
        Set<Way> set = wayChecker.checkAdjacentWays(way, new HashSet<Way>());

        assertEquals(set.size(), 1);

    }

    @Test
    public void test2() {

        File file = new File(AbstractTest.PATH_TO_ONEWAY_WRONG_DIRECTION2);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();
        long id = 24215210;
        Way way = (Way) ds.getPrimitiveById(id, OsmPrimitiveType.WAY);

        assertEquals(RouteUtils.isOnewayForPublicTransport(way), -1);

        Relation route = null;
        for (Relation r : ds.getRelations()) {
            if (r.hasKey("route")) {
                route = r;
            }
        }

        WayChecker wayChecker = new WayChecker(route, test);
        Set<Way> set = wayChecker.checkAdjacentWays(way, new HashSet<Way>());

        assertEquals(set.size(), 2);

    }

}
