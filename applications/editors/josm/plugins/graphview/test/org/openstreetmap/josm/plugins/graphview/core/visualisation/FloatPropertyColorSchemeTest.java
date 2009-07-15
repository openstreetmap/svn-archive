package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxweight;

public class FloatPropertyColorSchemeTest {

	private FloatPropertyColorScheme subject;

	@Before
	public void setUp() {

		Map<Float, Color> colorMap = new HashMap<Float, Color>();
		colorMap.put( 5f, new Color( 42,  42,  42));
		colorMap.put(10f, new Color(100, 100, 100));
		colorMap.put(20f, new Color(200, 200, 200));

		subject = new FloatPropertyColorScheme(RoadMaxweight.class, colorMap, Color.RED);
	}

	@Test
	public void testGetColorForValue_below() {
		assertEquals(new Color(42, 42, 42), subject.getColorForValue(1f));
		assertEquals(new Color(42, 42, 42), subject.getColorForValue(5f));
	}

	@Test
	public void testGetColorForValue_above() {
		assertEquals(new Color(200, 200, 200), subject.getColorForValue(25f));
	}

	@Test
	public void testGetColorForValue_value() {
		assertEquals(new Color(100, 100, 100), subject.getColorForValue(10f));
	}

	@Test
	public void testGetColorForValue_interpolate() {
		assertEquals(new Color(150, 150, 150), subject.getColorForValue(15f));
	}

}
