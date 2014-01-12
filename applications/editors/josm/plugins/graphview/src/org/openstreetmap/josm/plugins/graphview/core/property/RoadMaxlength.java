package org.openstreetmap.josm.plugins.graphview.core.property;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.LENGTH;

import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxlength extends RoadValueLimit {
    public RoadMaxlength() {
        super("maxlength", LENGTH, LimitType.MAXIMUM);
    }
    @Override
    protected Float parse(String valueString) {
        return ValueStringParser.parseMeasure(valueString);
    }
}
