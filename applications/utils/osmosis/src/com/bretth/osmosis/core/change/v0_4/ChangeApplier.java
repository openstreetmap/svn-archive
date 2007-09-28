package com.bretth.osmosis.core.change.v0_4;

import com.bretth.osmosis.core.container.v0_4.ChangeContainer;
import com.bretth.osmosis.core.container.v0_4.EntityContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.sort.v0_4.EntityByTypeThenIdComparator;
import com.bretth.osmosis.core.store.DataPostbox;
import com.bretth.osmosis.core.task.common.ChangeAction;
import com.bretth.osmosis.core.task.v0_4.ChangeSink;
import com.bretth.osmosis.core.task.v0_4.MultiSinkMultiChangeSinkRunnableSource;
import com.bretth.osmosis.core.task.v0_4.Sink;


/**
 * Applies a change set to an input source and produces an updated data set.
 * 
 * @author Brett Henderson
 */
public class ChangeApplier implements MultiSinkMultiChangeSinkRunnableSource {
	
	private Sink sink;
	private DataPostbox<EntityContainer> basePostbox;
	private DataPostbox<ChangeContainer> changePostbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param inputBufferCapacity
	 *            The size of the buffers to use for input sources.
	 */
	public ChangeApplier(int inputBufferCapacity) {
		basePostbox = new DataPostbox<EntityContainer>(inputBufferCapacity);
		changePostbox = new DataPostbox<ChangeContainer>(inputBufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Sink getSink(int instance) {
		final DataPostbox<EntityContainer> destinationPostbox = basePostbox;
		
		if (instance != 0) {
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
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public ChangeSink getChangeSink(int instance) {
		final DataPostbox<ChangeContainer> destinationPostbox = changePostbox;
		
		if (instance != 0) {
			throw new OsmosisRuntimeException("Change sink instance " + instance
					+ " is not valid.");
		}
		
		return new ChangeSink() {
			private DataPostbox<ChangeContainer> postbox = destinationPostbox;

			public void process(ChangeContainer change) {
				postbox.put(change);
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
	 * This implementation always returns 1.
	 * 
	 * @return 1
	 */
	public int getChangeSinkCount() {
		return 1;
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	
	/**
	 * Processes an entity that exists on the base source but not the change
	 * source.
	 * 
	 * @param entityContainer
	 *            The entity to be processed.
	 */
	private void processBaseOnlyEntity(EntityContainer entityContainer) {
		// The base entity doesn't exist on the change source therefore we
		// simply pass it through.
		sink.process(entityContainer);
	}
	
	
	/**
	 * Processes a change for an entity that exists on the change source but not
	 * the base source.
	 * 
	 * @param changeContainer
	 *            The change to be processed.
	 */
	private void processChangeOnlyEntity(ChangeContainer changeContainer) {
		// This entity doesn't exist in the "base" source therefore
		// we would normally expect a create.
		// However, it is possible that this is a "re-create" of a
		// previously deleted item which will come through as a
		// modify.
		// It is also possible that a delete will come through for a
		// previously deleted item which can be ignored.
		if (
				changeContainer.getAction().equals(ChangeAction.Create) ||
				changeContainer.getAction().equals(ChangeAction.Modify)) {
			sink.process(changeContainer.getEntityContainer());
		} else {
			// We don't need to do anything for delete.
		}
	}


	/**
	 * Processes the input sources and sends the updated data stream to the
	 * sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			EntityByTypeThenIdComparator comparator;
			EntityContainer base = null;
			ChangeContainer change = null;
			
			// Create a comparator for comparing two entities by type and identifier.
			comparator = new EntityByTypeThenIdComparator();
			
			// We continue in the comparison loop while both sources still have data.
			while ((base != null || basePostbox.hasNext()) && (change != null || changePostbox.hasNext())) {
				int comparisonResult;
				
				// Get the next input data where required.
				if (base == null) {
					base = basePostbox.getNext();
				}
				if (change == null) {
					change = changePostbox.getNext();
				}
				
				// Compare the two sources.
				comparisonResult = comparator.compare(base, change.getEntityContainer());
				
				if (comparisonResult < 0) {
					processBaseOnlyEntity(base);
					base = null;
					
				} else if (comparisonResult > 0) {
					processChangeOnlyEntity(change);
					change = null;
					
				} else {
					// The same entity exists in both sources therefore we are
					// expecting a modify or delete.
					if (change.getAction().equals(ChangeAction.Modify)) {
						sink.process(change.getEntityContainer());
						
					} else if (change.getAction().equals(ChangeAction.Delete)) {
						// We don't need to do anything for delete.
						
					} else {
						throw new OsmosisRuntimeException(
							"Cannot perform action " + change.getAction() + " on node with id="
							+ change.getEntityContainer().getEntity().getId()
							+ " because it exists in the base source."
						);
					}
					
					base = null;
					change = null;
				}
			}
			
			// Any remaining "base" entities are unmodified.
			while (base != null || basePostbox.hasNext()) {
				if (base == null) {
					base = basePostbox.getNext();
				}
				processBaseOnlyEntity(base);
				base = null;
			}
			
			// Any remaining "change" entities must be creates.
			while (change != null || changePostbox.hasNext()) {
				if (change == null) {
					change = changePostbox.getNext();
				}
				processChangeOnlyEntity(change);
				change = null;
			}
			
			sink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				basePostbox.setOutputError();
				changePostbox.setOutputError();
			}
			
			sink.release();
		}
	}
}
