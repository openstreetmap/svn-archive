package org.openstreetmap.josm.plugins.graphview.core.property;

import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;

public class RoadInclineTest extends RoadPropertyTest {

	private static void testIncline(Float expectedInclineForward, Float expectedInclineBackward,
			String inclineString) {

		testEvaluateW(new RoadIncline(),
				expectedInclineForward, expectedInclineBackward,
				new Tag("incline", inclineString));
	}

	@Test
	public void testEvaluate() {
		testIncline(5f, -5f, "5 %");
		testIncline(9.5f, -9.5f, "9.5 %");
		testIncline(-2.5f, 2.5f, "-2.5%");
		testIncline(null, null, "steep");
	}

}
