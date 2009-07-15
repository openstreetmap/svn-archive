package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WIDTH;

public class RoadWidth extends RoadValueLimit {
	public RoadWidth() {
		super("width", WIDTH, LimitType.MAXIMUM);
	}
}
