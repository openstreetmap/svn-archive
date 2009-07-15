package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxweight;

/**
 * scheme using edge colors that depend on maximum weight.
 */
public class MaxweightColorScheme extends FloatPropertyColorScheme {

	private static final Map<Float, Color> COLOR_MAP;

	static {
		COLOR_MAP = new HashMap<Float, Color>();
		COLOR_MAP.put(0f, new Color(0, 0, 50));
		COLOR_MAP.put(20f, new Color(100, 100, 255));
		COLOR_MAP.put(50f, new Color(200, 200, 255));
	}

	public MaxweightColorScheme() {
		super(RoadMaxweight.class, COLOR_MAP, Color.WHITE);
	}
}
