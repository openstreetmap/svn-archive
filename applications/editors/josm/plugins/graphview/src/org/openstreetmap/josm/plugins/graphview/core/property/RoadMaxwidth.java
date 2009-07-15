package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WIDTH;

public class RoadMaxwidth extends RoadValueLimit {
	public RoadMaxwidth() {
		super("maxwidth", WIDTH, LimitType.MAXIMUM);
	}
}
