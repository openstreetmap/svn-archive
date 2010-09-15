package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WEIGHT;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxweight extends RoadValueLimit {
    public RoadMaxweight() {
        super("maxweight", WEIGHT, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseWeight(valueString);
    }
}
