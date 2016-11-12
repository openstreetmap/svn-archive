// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.data;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;
import org.openstreetmap.josm.plugins.pt_assistant.utils.StopToWayAssigner;

public class StopToWayAssignerTest extends AbstractTest {

    @Test
    public void test() {

        File file = new File(AbstractTest.PATH_TO_ONEWAY_BAD_MEMBER_SORTING);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        Relation route = null;
        for (Relation r: ds.getRelations()) {
            if (r.getId() == 4552871) {
                route = r;
                break;
            }
        }

        PTRouteDataManager manager = new PTRouteDataManager(route);
        StopToWayAssigner assigner = new StopToWayAssigner(manager.getPTWays());

        // test with a [correct] stop_position:
        PTStop ptstop1 = manager.getPTStop(447358573L);
//        PTWay ptway1 = assigner.get(ptstop1);
//        Way way1 = ptway1.getWays().get(0);
        Way way1 = assigner.get(ptstop1);
        assertEquals(way1.getId(), 26956744L);

        // test with a [wrong] stop_position:
        PTStop ptstop2 = manager.getPTStop(427562058L);
        Way way2 = assigner.get(ptstop2);
        assertEquals(way2.getId(), 46349880L);

        // test with a stop_area:
        PTStop ptstop3 = manager.getPTStop(2987217064L);
        Way way3 = assigner.get(ptstop3);
        assertEquals(way3.getId(), 7045925L);

        // test with a platform without a stop_area:
        PTStop ptstop4 = manager.getPTStop(3327206909L);
        Way way4 = assigner.get(ptstop4);
        assertEquals(way4.getId(), 120277227L);
    }
}
