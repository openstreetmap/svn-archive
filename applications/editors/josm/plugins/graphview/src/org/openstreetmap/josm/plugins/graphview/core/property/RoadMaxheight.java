package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.HEIGHT;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxheight extends RoadValueLimit {
    public RoadMaxheight() {
        super("maxheight", HEIGHT, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseMeasure(valueString);
    }
}
