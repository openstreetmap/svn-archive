package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.WIDTH;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadWidth extends RoadValueLimit {
    public RoadWidth() {
        super("width", WIDTH, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseMeasure(valueString);
    }
}
