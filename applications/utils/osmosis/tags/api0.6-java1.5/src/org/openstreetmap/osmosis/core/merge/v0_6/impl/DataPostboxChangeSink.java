// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_6.impl;

import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;


/**
 * A change sink that writes all of its data to a postbox to be read by another thread.
 * 
 * @author Brett Henderson
 */
public class DataPostboxChangeSink implements ChangeSink {
	private DataPostbox<ChangeContainer> postbox;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param postbox
	 *            The postbox to write all incoming data into.
	 */
	public DataPostboxChangeSink(DataPostbox<ChangeContainer> postbox) {
		this.postbox = postbox;
	}


	/**
	 * {@inheritDoc}
	 */
	
	public void process(ChangeContainer change) {
		postbox.put(change);
	}


	/**
	 * {@inheritDoc}
	 */
	
	public void complete() {
		postbox.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	
	public void release() {
		postbox.release();
	}
}
