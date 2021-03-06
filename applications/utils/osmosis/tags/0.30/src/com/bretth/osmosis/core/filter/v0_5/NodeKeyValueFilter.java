// License: GPL. Copyright 2008 by Aurelien Jacobs
package com.bretth.osmosis.core.filter.v0_5;

import java.util.HashSet;

import com.bretth.osmosis.core.container.v0_5.BoundContainer;
import com.bretth.osmosis.core.container.v0_5.EntityContainer;
import com.bretth.osmosis.core.container.v0_5.EntityProcessor;
import com.bretth.osmosis.core.container.v0_5.NodeContainer;
import com.bretth.osmosis.core.container.v0_5.RelationContainer;
import com.bretth.osmosis.core.container.v0_5.WayContainer;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.domain.v0_5.Node;
import com.bretth.osmosis.core.task.v0_5.Sink;
import com.bretth.osmosis.core.task.v0_5.SinkSource;


/**
 * A class filtering everything but allowed nodes.
 *
 * @author Aurelien Jacobs
 */
public class NodeKeyValueFilter implements SinkSource, EntityProcessor {
	private Sink sink;
	private HashSet<String> allowedKeyValues;

	/**
	 * Creates a new instance.
	 *
	 * @param keyValueList
	 *            Comma-separated list of allowed key-value combinations,
	 *            e.g. "place.city,place.town"
	 */
	public NodeKeyValueFilter(String keyValueList) {

		allowedKeyValues = new HashSet<String>();
		String[] keyValues = keyValueList.split(",");
		for (int i = 0; i < keyValues.length; i++) {
			allowedKeyValues.add(keyValues[i]);
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
			String keyValue = tag.getKey() + "." + tag.getValue();
			if (allowedKeyValues.contains(keyValue)) {
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
