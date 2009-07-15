package org.openstreetmap.josm.plugins.graphview.core.property;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

public class RoadTracktype implements RoadPropertyType<Integer> {

	public <N, W, R> Integer evaluateN(N node, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		return null;
	};

	public <N, W, R> Integer evaluateW(W way, boolean forward, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		assert way != null && accessParameters != null && dataSource != null;

		TagGroup tags = dataSource.getTagsW(way);
		String tracktypeString = tags.getValue("tracktype");

		if (tracktypeString != null) {
			if        ("grade1".equals(tracktypeString)) {
				return 1;
			} else if ("grade2".equals(tracktypeString)) {
				return 2;
			} else if ("grade3".equals(tracktypeString)) {
				return 3;
			} else if ("grade4".equals(tracktypeString)) {
				return 4;
			} else if ("grade5".equals(tracktypeString)) {
				return 5;
			}
		}

		return null;
	};

	public boolean isUsable(Object propertyValue, AccessParameters accessParameters) {
		assert propertyValue instanceof Integer;

		int tracktype = (Integer)propertyValue;

		Integer maxTracktype =
			accessParameters.getVehiclePropertyValue(VehiclePropertyTypes.MAX_TRACKTYPE);

		if (maxTracktype != null && tracktype > maxTracktype) {
			return false;
		} else {
			return true;
		}
	}

}
