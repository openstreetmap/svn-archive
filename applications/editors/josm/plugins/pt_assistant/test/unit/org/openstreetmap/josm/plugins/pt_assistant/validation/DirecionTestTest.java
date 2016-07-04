package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.plugins.pt_assistant.AbstractTest;
import org.openstreetmap.josm.plugins.pt_assistant.ImportUtils;

public class DirecionTestTest extends AbstractTest {
    
    @Test
    public void test() {
        
        File file = new File(AbstractTest.PATH_TO_ROUNDABOUT_ONEWAY);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
        
        PTAssitantValidatorTest test = new PTAssitantValidatorTest();
        
        Relation route = null;
        for (Relation r: ds.getRelations()) {
            if (r.hasKey("route")) {
                route = r;
            }
        }
        
        assertEquals(route.getMembersCount(), 213);
                
        List<TestError> errors = new ArrayList<>();
        
        for (Relation r: ds.getRelations()) {
        	WayChecker wayChecker = new WayChecker(r, test);
        	wayChecker.performDirectionTest();
        	errors.addAll(wayChecker.getErrors());
        }
        
        assertEquals(errors.size(), 1);
        int onewayErrorCaught = 0;
        for (TestError e: errors ) {
            if (e.getCode() == PTAssitantValidatorTest.ERROR_CODE_DIRECTION) {
                onewayErrorCaught++;
            }
        }
        
        assertEquals(onewayErrorCaught, 1);
        
        // fix the direction errors:
        
        boolean detectedErrorsAreCorrect = true;
        for (TestError e: errors) {
            if (e.getCode() == PTAssitantValidatorTest.ERROR_CODE_DIRECTION) {
                @SuppressWarnings("unchecked")
                List<OsmPrimitive> highlighted = (List<OsmPrimitive>) e.getHighlighted();
                if (highlighted.get(0).getId() != 26130630 && highlighted.get(0).getId() != 151278290)  {
                    detectedErrorsAreCorrect = false;
                }
            }
        }
        
        assertTrue(detectedErrorsAreCorrect);
        
    
    }

}
