package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WIDTH;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxwidth extends RoadValueLimit {
    public RoadMaxwidth() {
        super("maxwidth", WIDTH, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseMeasure(valueString);
    }
}
