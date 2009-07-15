package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.property.RoadMaxspeed;

/**
 * scheme using edge colors that depend on maximum speed.
 */
public class MaxspeedColorScheme extends FloatPropertyColorScheme {

	private static final Map<Float, Color> COLOR_MAP;

	static {
		COLOR_MAP = new HashMap<Float, Color>();
		COLOR_MAP.put(0f, new Color(50, 0, 0));
		COLOR_MAP.put(30f, Color.RED);
		COLOR_MAP.put(60f, Color.YELLOW);
		COLOR_MAP.put(90f, Color.GREEN);
		COLOR_MAP.put(150f, Color.BLUE);
	}

	public MaxspeedColorScheme() {
		super(RoadMaxspeed.class, COLOR_MAP, Color.GRAY);
	}
}
