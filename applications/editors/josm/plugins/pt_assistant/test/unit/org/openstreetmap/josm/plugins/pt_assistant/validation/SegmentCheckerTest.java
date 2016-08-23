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
	public void testStopByStopTest() {

		File file = new File(AbstractTest.PATH_TO_SEGMENT_TEST);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();

		Relation route = null;

		for (Relation r : ds.getRelations()) {
			if (RouteUtils.isTwoDirectionRoute(r)) {
				route = r;
				break;
			}
		}

		SegmentChecker.reset();
		SegmentChecker segmentChecker = new SegmentChecker(route, test);
		segmentChecker.performStopByStopTest();
		assertEquals(SegmentChecker.getCorrectSegmentCount(), 27);
		assertEquals(segmentChecker.getErrors().size(), 0);
	}

	/**
	 * Tests the stop-by-stop test
	 */
	@Test
	public void testRepeatLastFix() {
		File file = new File(AbstractTest.PATH_TO_REPEAT_FIX);
		DataSet ds = ImportUtils.importOsmFile(file, "testLayer");
		PTAssistantValidatorTest test = new PTAssistantValidatorTest();

		Relation route123 = null;
		Relation route130 = null;
		Relation route168 = null;
		Relation route184 = null;

		for (Relation r : ds.getRelations()) {
			if (r.getId() == 5379737) {
				route123 = r;
			} else if (r.getId() == 5379738) {
				route130 = r;
			} else if (r.getId() == 5379739) {
				route168 = r;
			} else if (r.getId() == 5379740) {
				route184 = r;
			}
		}

		SegmentChecker.reset();
		SegmentChecker segmentChecker123 = new SegmentChecker(route123, test);
		SegmentChecker segmentChecker130 = new SegmentChecker(route130, test);
		SegmentChecker segmentChecker168 = new SegmentChecker(route168, test);
		SegmentChecker segmentChecker184 = new SegmentChecker(route184, test);
		segmentChecker123.performStopByStopTest();
//		TestError error123 = segmentChecker123.getErrors().get(0);
//		PTRouteSegment wrongSegment123 = SegmentChecker.getWrongSegment(error123);
		segmentChecker130.performStopByStopTest();
		segmentChecker168.performStopByStopTest();
		segmentChecker184.performStopByStopTest();

		// Check the error number:
		assertEquals(segmentChecker123.getErrors().size(), 1);
		assertEquals(segmentChecker130.getErrors().size(), 1);
		assertEquals(segmentChecker168.getErrors().size(), 1);
		assertEquals(segmentChecker184.getErrors().size(), 0);
	}
}
