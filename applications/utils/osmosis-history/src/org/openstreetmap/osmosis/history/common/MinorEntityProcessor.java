package org.openstreetmap.osmosis.history.common;

import org.openstreetmap.osmosis.core.container.v0_6.EntityProcessor;
import org.openstreetmap.osmosis.history.domain.MinorWayContainer;

public interface MinorEntityProcessor extends EntityProcessor {
	/**
	 * Process the way.
	 * 
	 * @param way
	 *            The way to be processed.
	 */
	void process(MinorWayContainer way);
}
