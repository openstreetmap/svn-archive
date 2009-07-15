package org.openstreetmap.josm.plugins.graphview.core.property;

import java.util.Collection;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;

public class RoadSurface implements RoadPropertyType<String> {

	public <N, W, R> String evaluateN(N node, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		return null;
	};

	public <N, W, R> String evaluateW(W way, boolean forward, AccessParameters accessParameters,
			DataSource<N,W,R> dataSource) {
		assert way != null && accessParameters != null && dataSource != null;

		TagGroup tags = dataSource.getTagsW(way);
		return tags.getValue("surface");

	};

	public boolean isUsable(Object propertyValue, AccessParameters accessParameters) {
		assert propertyValue instanceof String;

		String surface = (String)propertyValue;

		Collection<String> surfaceBlacklist =
			accessParameters.getVehiclePropertyValue(VehiclePropertyTypes.SURFACE_BLACKLIST);

		if (surfaceBlacklist != null && surfaceBlacklist.contains(surface)) {
			return false;
		} else {
			return true;
		}
	}

}
