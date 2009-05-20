// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.merge.v0_6.impl.DataPostboxChangeSink;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_6.MultiChangeSinkRunnableChangeSource;

/**
 * Combines multiple change sources into a single data set. It is done by writing the contents of
 * each of the input sources to the sink in order.
 * 
 * @author Brett Henderson
 */
public class ChangeAppender implements MultiChangeSinkRunnableChangeSource {
	
	private List<DataPostbox<ChangeContainer>> sources;
	private ChangeSink changeSink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sourceCount
	 *            The number of sources to be appended.
	 */
	public ChangeAppender(int sourceCount) {
		sources = new ArrayList<DataPostbox<ChangeContainer>>(sourceCount);
		
		for (int i = 0; i < sourceCount; i++) {
			sources.add(new DataPostbox<ChangeContainer>(10));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	
	public ChangeSink getChangeSink(int instance) {
		if (instance < 0 || instance >= sources.size()) {
			throw new OsmosisRuntimeException("Sink instance " + instance + " is not valid.");
		}
		
		return new DataPostboxChangeSink(sources.get(instance));
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public int getChangeSinkCount() {
		return sources.size();
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}

	
	/**
	 * {@inheritDoc}
	 */
	
	public void run() {
		boolean completed = false;
		
		try {
			// Write the data from each source to the sink in turn.
			for (DataPostbox<ChangeContainer> source : sources) {
				while (source.hasNext()) {
					changeSink.process(source.getNext());
				}
			}
			
			changeSink.complete();
			
			completed = true;
		
		} finally {
			if (!completed) {
				for (DataPostbox<ChangeContainer> source : sources) {
					source.setOutputError();
				}
			}
			
			changeSink.release();
		}
	}
}
