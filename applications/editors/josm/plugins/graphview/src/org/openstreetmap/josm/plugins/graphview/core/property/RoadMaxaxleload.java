package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.AXLELOAD;

public class RoadMaxaxleload extends RoadValueLimit {
	public RoadMaxaxleload() {
		super("maxaxleload", AXLELOAD, LimitType.MAXIMUM);
	}
}
