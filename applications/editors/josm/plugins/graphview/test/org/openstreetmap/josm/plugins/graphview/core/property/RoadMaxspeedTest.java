package org.openstreetmap.josm.plugins.graphview.core.property;

import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.data.Tag;

public class RoadMaxspeedTest extends RoadPropertyTest {

	private static void testMaxspeed(float expectedMaxspeed, String maxspeedString) {
		testEvaluateBoth(new RoadMaxspeed(),	expectedMaxspeed, new Tag("maxspeed", maxspeedString));
	}

	@Test
	public void testEvaluate_numeric() {
		testMaxspeed(30, "30");
		testMaxspeed(48, "48.28");
	}

	@Test
	public void testEvaluate_kmh() {
		testMaxspeed(50, "50 km/h");
		testMaxspeed(120, "120km/h");
		testMaxspeed(30, "30	km/h");
	}

	@Test
	public void testEvaluate_mph() {
		testMaxspeed(72, "45 mph");
		testMaxspeed(64, "40mph");
		testMaxspeed(24, "15	mph");
	}

}
