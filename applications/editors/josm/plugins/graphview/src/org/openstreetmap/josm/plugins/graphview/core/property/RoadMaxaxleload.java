package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.AXLELOAD;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxaxleload extends RoadValueLimit {
    public RoadMaxaxleload() {
        super("maxaxleload", AXLELOAD, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseWeight(valueString);
    }
}
