package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.LENGTH;

public class RoadMaxlength extends RoadValueLimit {
	public RoadMaxlength() {
		super("maxlength", LENGTH, LimitType.MAXIMUM);
	}
}
