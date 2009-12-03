// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.buffer.v0_5;

import org.openstreetmap.osmosis.core.container.v0_5.ChangeContainer;
import org.openstreetmap.osmosis.core.store.DataPostbox;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSinkRunnableChangeSource;


/**
 * Splits the pipeline so that it can be processed on multiple threads. The
 * input thread to this task stores data in a buffer which blocks if it fills
 * up. This task runs on a new thread which reads data from the buffer and
 * writes it to the destination.
 * 
 * @author Brett Henderson
 */
public class ChangeBuffer implements ChangeSinkRunnableChangeSource {
	private ChangeSink changeSink;
	private DataPostbox<ChangeContainer> buffer;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param bufferCapacity
	 *            The size of the buffer to use.
	 */
	public ChangeBuffer(int bufferCapacity) {
		buffer = new DataPostbox<ChangeContainer>(bufferCapacity);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(ChangeContainer changeContainer) {
		buffer.put(changeContainer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		buffer.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		buffer.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Sends all input data to the sink.
	 */
	public void run() {
		boolean completed = false;
		
		try {
			while (buffer.hasNext()) {
				changeSink.process(buffer.getNext());
			}
			
			changeSink.complete();
			completed = true;
			
		} finally {
			if (!completed) {
				buffer.setOutputError();
			}
			
			changeSink.release();
		}
	}
	
}
