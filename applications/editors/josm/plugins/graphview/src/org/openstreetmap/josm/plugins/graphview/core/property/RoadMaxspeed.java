package org.openstreetmap.josm.plugins.graphview.core.property;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessParameters;
import org.openstreetmap.josm.plugins.graphview.core.data.DataSource;
import org.openstreetmap.josm.plugins.graphview.core.data.TagGroup;
import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

public class RoadMaxspeed implements RoadPropertyType<Float> {

	private DataSource<?, ?, ?> lastDataSource;

	/**
	 * (re)creates information like boundaries if data source has changed
	 * since last call to {@link #evaluate(Object, boolean, AccessParameters, DataSource)}
	 */
	private <N, W, R> void initializeIfNecessary(DataSource<N, W, R> dataSource) {

		if (dataSource != lastDataSource) {

			/*
			 *
			 * currently no activities;
			 * place boundaries or similar features can be handled here
			 * once there is consensus on the topic of implicit maxspeeds, trafficzones etc.
			 *
			 */

			lastDataSource = dataSource;
		}
	}

	public <N, W, R> Float evaluateN(N node, AccessParameters accessParameters,
			DataSource<N, W, R> dataSource) {
		assert node != null && accessParameters != null && dataSource != null;

		initializeIfNecessary(dataSource);

		return evaluateTags(dataSource.getTagsN(node));
	}

	public <N, W, R> Float evaluateW(W way, boolean forward, AccessParameters accessParameters,
			DataSource<N, W, R> dataSource) {
		assert way != null && accessParameters != null && dataSource != null;

		initializeIfNecessary(dataSource);

		return evaluateTags(dataSource.getTagsW(way));
	}

	private Float evaluateTags(TagGroup tags) {
		String maxspeedString = tags.getValue("maxspeed");

		if (maxspeedString != null) {

			Float maxspeed = ValueStringParser.parseSpeed(maxspeedString);
			if (maxspeed != null) {
				return maxspeed;
			}

		}

		return null;
	}

	public boolean isUsable(Object propertyValue, AccessParameters accessParameters) {
		assert propertyValue instanceof Float;
		return true;
	}

}
