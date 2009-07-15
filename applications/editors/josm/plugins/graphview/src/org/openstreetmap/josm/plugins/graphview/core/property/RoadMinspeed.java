package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.SPEED;

public class RoadMinspeed extends RoadValueLimit {
	public RoadMinspeed() {
		super("minspeed", SPEED, LimitType.MINIMUM);
	}
}
