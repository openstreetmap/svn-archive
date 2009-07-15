package org.openstreetmap.josm.plugins.graphview.core.property;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadIncline implements RoadPropertyType<Float> {

	public <N, W, R> Float evaluateN(N node, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		return null;
	};

	public <N, W, R> Float evaluateW(W way, boolean forward, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		assert way != null && accessParameters != null && dataSource != null;

		TagGroup tags = dataSource.getTagsW(way);
		String inclineString = tags.getValue("incline");

		if (inclineString != null) {
			Float incline = ValueStringParser.parseIncline(inclineString);
			if (incline != null) {
				if (!forward) {
					incline = -incline;
				}
				return incline;
			}
		}

		return null;
	};

	public boolean isUsable(Object propertyValue, AccessParameters accessParameters) {
		assert propertyValue instanceof Float;

		float incline = (Float)propertyValue;

		Float maxInclineUp =
			accessParameters.getVehiclePropertyValue(VehiclePropertyTypes.MAX_INCLINE_UP);
		Float maxInclineDown =
			accessParameters.getVehiclePropertyValue(VehiclePropertyTypes.MAX_INCLINE_DOWN);

		if (maxInclineUp != null && incline > maxInclineUp) {
			return false;
		} else if (maxInclineDown != null && -incline > maxInclineDown) {
			return false;
		} else {
			return true;
		}
	}

}
