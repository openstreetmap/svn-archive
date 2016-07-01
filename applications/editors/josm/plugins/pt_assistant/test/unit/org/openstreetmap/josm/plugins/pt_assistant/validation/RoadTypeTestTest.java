package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
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
        
        RoadTypeTest roadTypeTest = new RoadTypeTest();
        for (Relation r: ds.getRelations()) {
            roadTypeTest.visit(r);
        }
        
        List<TestError> errors = roadTypeTest.getErrors();
        assertEquals(errors.size(), 2);
        
        for (TestError e: errors) {
            assertEquals(e.getCode(), RoadTypeTest.ERROR_CODE_ROAD_TYPE);
            @SuppressWarnings("unchecked")
            List<OsmPrimitive> highlighted = (List<OsmPrimitive>) e.getHighlighted();
            Way way = (Way) highlighted.get(0);
            assertTrue(way.getId() == 8169083 || way.getId() == 8034569);
        }
    }


}
