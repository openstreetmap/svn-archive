// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.filter.v0_5;

import java.util.HashSet;

import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;
import org.openstreetmap.osmosis.core.task.v0_5.SinkSource;


/**
 * A class filtering everything but allowed nodes.
 *
 * @author Aurelien Jacobs
 */
public class NodeKeyFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedKeys;

	/**
	 * Creates a new instance.
	 *
	 * @param keyList
	 *            Comma-separated list of allowed key,
	 *            e.g. "place,amenity"
	 */
	public NodeKeyFilter(String keyList) {

		allowedKeys = new HashSet<String>();
		String[] keys = keyList.split(",");
		for (int i = 0; i < keys.length; i++) {
			allowedKeys.add(keys[i]);
		}

	}


	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		// Ask the entity container to invoke the appropriate processing method
		// for the entity type.
		entityContainer.process(this);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// By default, pass it on unchanged
		sink.process(boundContainer);
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer container) {
		Node node = container.getEntity();

		boolean matchesFilter = false;
		for (Tag tag : node.getTagList()) {
			if (allowedKeys.contains(tag.getKey())) {
				matchesFilter = true;
				break;
			}
		}

		if (matchesFilter) {
			sink.process(container);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer container) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer container) {
		// Do nothing.
	}


	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		sink.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	public void release() {
		sink.release();
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
}
