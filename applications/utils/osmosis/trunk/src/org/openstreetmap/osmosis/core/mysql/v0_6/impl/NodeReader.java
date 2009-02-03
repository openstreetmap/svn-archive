// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.mysql.v0_6.impl;

import java.util.NoSuchElementException;

import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.domain.v0_6.NodeBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.lifecycle.ReleasableIterator;
import org.openstreetmap.osmosis.core.store.PeekableIterator;
import org.openstreetmap.osmosis.core.store.PersistentIterator;
import org.openstreetmap.osmosis.core.store.SingleClassObjectSerializationFactory;


/**
 * Reads all nodes from a database ordered by their identifier. It combines the
 * output of the node table readers to produce fully configured node objects.
 * 
 * @author Brett Henderson
 */
public class NodeReader implements ReleasableIterator<EntityHistory<NodeBuilder>> {
	
	private ReleasableIterator<EntityHistory<NodeBuilder>> nodeReader;
	private PeekableIterator<DbFeatureHistory<DbFeature<Tag>>> nodeTagReader;
	private EntityHistory<NodeBuilder> nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param readAllUsers
	 *            If this flag is true, all users will be read from the database
	 *            regardless of their public edits flag.
	 */
	public NodeReader(DatabaseLoginCredentials loginCredentials, boolean readAllUsers) {
		nodeReader = new PersistentIterator<EntityHistory<NodeBuilder>>(
			new SingleClassObjectSerializationFactory(EntityHistory.class),
			new NodeTableReader(loginCredentials, readAllUsers),
			"nod",
			true
		);
		nodeTagReader = new PeekableIterator<DbFeatureHistory<DbFeature<Tag>>>(
			new PersistentIterator<DbFeatureHistory<DbFeature<Tag>>>(
				new SingleClassObjectSerializationFactory(DbFeatureHistory.class),
				new EntityTagTableReader(loginCredentials, "node_tags"),
				"nodtag",
				true
			)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && nodeReader.hasNext()) {
			EntityHistory<NodeBuilder> nodeHistory;
			long nodeId;
			int nodeVersion;
			NodeBuilder node;
			
			nodeHistory = nodeReader.next();
			
			node = nodeHistory.getEntity();
			nodeId = node.getId();
			nodeVersion = node.getVersion();
			
			// Skip all node tags that are from lower id or lower version of the same id.
			while (nodeTagReader.hasNext()) {
				DbFeatureHistory<DbFeature<Tag>> nodeTagHistory;
				DbFeature<Tag> nodeTag;
				
				nodeTagHistory = nodeTagReader.peekNext();
				nodeTag = nodeTagHistory.getDbFeature();
				
				if (nodeTag.getEntityId() < nodeId) {
					nodeTagReader.next();
				} else if (nodeTag.getEntityId() == nodeId) {
					if (nodeTagHistory.getVersion() < nodeVersion) {
						nodeTagReader.next();
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the node.
			while (
					nodeTagReader.hasNext()
					&& nodeTagReader.peekNext().getDbFeature().getEntityId() == nodeId
					&& nodeTagReader.peekNext().getVersion() == nodeVersion) {
				node.addTag(nodeTagReader.next().getDbFeature().getFeature());
			}
			
			nextValue = nodeHistory;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public EntityHistory<NodeBuilder> next() {
		EntityHistory<NodeBuilder> result;
		
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		result = nextValue;
		nextValueLoaded = false;
		
		return result;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		nodeReader.release();
		nodeTagReader.release();
	}
}
