package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WEIGHT;

public class RoadMaxweight extends RoadValueLimit {
	public RoadMaxweight() {
		super("maxweight", WEIGHT, LimitType.MAXIMUM);
	}
}
