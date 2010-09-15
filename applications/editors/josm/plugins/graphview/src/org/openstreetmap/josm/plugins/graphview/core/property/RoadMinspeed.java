package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.SPEED;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMinspeed extends RoadValueLimit {
    public RoadMinspeed() {
        super("minspeed", SPEED, LimitType.MINIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseSpeed(valueString);
    }
}
