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

public class SortingTestTest extends AbstractTest {

    @Test
    public void sortingTestBeforeFile() {
        File file = new File(AbstractTest.PATH_TO_DL131_BEFORE);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();

        List<TestError> errors = new ArrayList<>();

        for (Relation r: ds.getRelations()) {
            RouteChecker routeChecker = new RouteChecker(r, test);
            routeChecker.performSortingTest();
            errors.addAll(routeChecker.getErrors());

        }

        assertEquals(errors.size(), 1);
        assertEquals(errors.iterator().next().getCode(), PTAssistantValidatorTest.ERROR_CODE_SORTING);
        assertEquals(errors.iterator().next().getTester().getClass().getName(), PTAssistantValidatorTest.class.getName());
    }

    @Test
    public void sortingTestAfterFile() {
        File file = new File(AbstractTest.PATH_TO_DL131_AFTER);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();

        List<TestError> errors = new ArrayList<>();

        for (Relation r: ds.getRelations()) {
            RouteChecker routeChecker = new RouteChecker(r, test);
            routeChecker.performSortingTest();
            errors.addAll(routeChecker.getErrors());

        }


        assertEquals(errors.size(), 0);
    }

    // TODO: this test will only pass after the functionality for recognizing
    // and closing the gap is implemented.
//    @Test
//    public void overshootTestBeforeFile() {
//        File file = new File(AbstractTest.PATH_TO_DL286_BEFORE);
//        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
//
//        GapTest gapTest = new GapTest();
//        for (Relation r : ds.getRelations()) {
//            gapTest.visit(r);
//        }
//
//        List<TestError> errors = gapTest.getErrors();
//
//        assertEquals(errors.size(), 1);
//        assertEquals(errors.get(0).getCode(), GapTest.ERROR_CODE_OVERSHOOT);
//
//    }

    @Test
    public void overshootTestAfterFile() {
        File file = new File(AbstractTest.PATH_TO_DL286_AFTER);
        DataSet ds = ImportUtils.importOsmFile(file, "testLayer");

        PTAssistantValidatorTest test = new PTAssistantValidatorTest();

        List<TestError> errors = new ArrayList<>();

        for (Relation r: ds.getRelations()) {
            RouteChecker routeChecker = new RouteChecker(r, test);
            routeChecker.performSortingTest();
            errors.addAll(routeChecker.getErrors());
        }

        assertEquals(errors.size(), 0);
    }
}
