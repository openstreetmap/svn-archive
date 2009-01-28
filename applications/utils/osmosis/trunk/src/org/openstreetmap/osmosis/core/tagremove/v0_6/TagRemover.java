// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.tagremove.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;


/**
 * Filters a set of tags from all entities. This allows unwanted tags to be
 * removed from the data.
 * 
 * @author Jochen Topf
 * @author Brett Henderson
 */
public class TagRemover implements SinkSource {
	private TagRemoverBuilder dropTagsBuilder;
	private Sink sink;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param keyList
	 *            Comma separated list of keys of tags to be removed.
	 * @param keyPrefixList
	 *            Comma separated list of key prefixes of tags to be removed.
	 */
	public TagRemover(String keyList, String keyPrefixList) {
		dropTagsBuilder = new TagRemoverBuilder(keyList, keyPrefixList);
	}


	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink sink) {
		this.sink = sink;
		dropTagsBuilder.setSink(sink);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void process(EntityContainer entityContainer) {
		entityContainer.process(dropTagsBuilder);
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
}
