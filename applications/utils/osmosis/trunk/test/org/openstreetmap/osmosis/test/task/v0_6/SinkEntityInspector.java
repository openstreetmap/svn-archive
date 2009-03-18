// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.test.task.v0_6;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

/**
 * Mock object for inspecting the resulting entities after passing through a pipeline task.
 * 
 * @author Karl Newman
 */
public class SinkEntityInspector implements Sink {

	private List<EntityContainer> processedEntities;


	private void initialize() {
		if (processedEntities == null) {
			processedEntities = new LinkedList<EntityContainer>();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void complete() {
		// Nothing to do here
	}


	/**
	 * Catch all passed entities and save them for later inspection.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		initialize();
		processedEntities.add(entityContainer);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		// Nothing to do here
	}


	/**
	 * Shortcut method if you only care about the most recent EntityContainer.
	 * 
	 * @return the lastEntityContainer
	 */
	public EntityContainer getLastEntityContainer() {
		initialize();
		if (processedEntities.isEmpty()) {
			return null;
		} else {
			return processedEntities.get(processedEntities.size() - 1);
		}
	}


	/**
	 * Retrieve an Iterable of all the processed EntityContainers.
	 * 
	 * @return the processedEntities
	 */
	public Iterable<EntityContainer> getProcessedEntities() {
		initialize();
		return Collections.unmodifiableList(processedEntities);
	}

}
