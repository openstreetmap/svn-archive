// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.change.v0_6;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.change.v0_6.impl.TimestampSetter;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.sort.v0_6.EntityByTypeThenIdComparator;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.common.ChangeAction;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.MultiSinkRunnableChangeSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * Compares two different data sources and produces a set of differences.
 * 
 * @author Brett Henderson
 */
public class ChangeDeriver implements MultiSinkRunnableChangeSource {

	private ChangeSink changeSink;
	private DataPostbox<EntityContainer> fromPostbox;
	private DataPostbox<EntityContainer> toPostbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeDeriver(int inputBufferCapacity) {
		fromPostbox = new DataPostbox<EntityContainer>(inputBufferCapacity);
		toPostbox = new DataPostbox<EntityContainer>(inputBufferCapacity);
	}


	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		final DataPostbox<EntityContainer> destinationPostbox;
		
		switch (instance) {
		case 0:
			destinationPostbox = fromPostbox;
			break;
		case 1:
			destinationPostbox = toPostbox;
			break;
		default:
			throw new OsmosisRuntimeException("Sink instance " + instance
					+ " is not valid.");
		}
		
		return new Sink() {
			private DataPostbox<EntityContainer> postbox = destinationPostbox;
			
			public void process(EntityContainer entityContainer) {
				postbox.put(entityContainer);
			}
			public void complete() {
				postbox.complete();
			}
			public void release() {
				postbox.release();
			}
		};
	}


	/**
	 * This implementation always returns 2.
	 * 
	 * @return 2
	 */
	public int getSinkCount() {
		return 2;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Processes the input sources and sends the changes to the change sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			EntityByTypeThenIdComparator comparator;
			EntityContainer fromEntityContainer = null;
			EntityContainer toEntityContainer = null;
			TimestampSetter timestampSetter;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityByTypeThenIdComparator();
			
			// Create an object for setting the current timestamp on entities being deleted.
			timestampSetter = new TimestampSetter();
			
			// We continue in the comparison loop while both sources still have data.
			while (
					(fromEntityContainer != null || fromPostbox.hasNext()) &&
					(toEntityContainer != null || toPostbox.hasNext())) {
				int comparisonResult;
				
				// Get the next input data where required.
				if (fromEntityContainer == null) {
					fromEntityContainer = fromPostbox.getNext();
				}
				if (toEntityContainer == null) {
					toEntityContainer = toPostbox.getNext();
				}
				
				// Compare the two sources.
				comparisonResult = comparator.compare(fromEntityContainer, toEntityContainer);
				
				if (comparisonResult < 0) {
					// The from entity doesn't exist on the to source therefore
					// has been deleted. We don't know when the entity was
					// deleted so set the delete time to the current time.
					changeSink.process(
							new ChangeContainer(
									timestampSetter.updateTimestamp(fromEntityContainer),
									ChangeAction.Delete));
					fromEntityContainer = null;
				} else if (comparisonResult > 0) {
					// The to entity doesn't exist on the from source therefore has
					// been created.
					changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Create));
					toEntityContainer = null;
				} else {
					// The entity exists on both sources, therefore we must
					// compare
					// the entities directly. If there is a difference, the
					// entity has been modified.
					if (!fromEntityContainer.getEntity().equals(toEntityContainer.getEntity())) {
						changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Modify));
					}
					fromEntityContainer = null;
					toEntityContainer = null;
				}
			}
			
			// Any remaining "from" entities are deletes.
			while (fromEntityContainer != null || fromPostbox.hasNext()) {
				if (fromEntityContainer == null) {
					fromEntityContainer = fromPostbox.getNext();
				}

				// The from entity doesn't exist on the to source therefore
				// has been deleted. We don't know when the entity was
				// deleted so set the delete time to the current time.
				changeSink.process(
						new ChangeContainer(
								timestampSetter.updateTimestamp(fromEntityContainer),
								ChangeAction.Delete));
				fromEntityContainer = null;
			}
			// Any remaining "to" entities are creates.
			while (toEntityContainer != null || toPostbox.hasNext()) {
				if (toEntityContainer == null) {
					toEntityContainer = toPostbox.getNext();
				}
				changeSink.process(new ChangeContainer(toEntityContainer, ChangeAction.Create));
				toEntityContainer = null;
			}
			
			changeSink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				fromPostbox.setOutputError();
				toPostbox.setOutputError();
			}
			
			changeSink.release();
		}
	}
}
