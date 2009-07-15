package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.HEIGHT;

public class RoadMaxheight extends RoadValueLimit {
	public RoadMaxheight() {
		super("maxheight", HEIGHT, LimitType.MAXIMUM);
	}
}
