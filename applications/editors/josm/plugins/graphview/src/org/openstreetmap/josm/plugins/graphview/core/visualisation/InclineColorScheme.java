package org.openstreetmap.josm.plugins.graphview.core.visualisation;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.graphview.core.property.RoadIncline;

/**
 * scheme using edge colors that depend on incline.
 */
public class InclineColorScheme extends FloatPropertyColorScheme {

	private static final Map<Float, Color> COLOR_MAP;

	static {
		COLOR_MAP = new HashMap<Float, Color>();
		COLOR_MAP.put(-30f, Color.BLUE);
		COLOR_MAP.put(0f, Color.WHITE);
		COLOR_MAP.put(30f, Color.RED);
	}

	public InclineColorScheme() {
		super(RoadIncline.class, COLOR_MAP, Color.GRAY);
	}
}
