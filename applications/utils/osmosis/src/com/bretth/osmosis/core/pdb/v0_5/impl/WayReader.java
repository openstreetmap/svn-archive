// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.bretth.osmosis.core.domain.v0_5.Way;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBWayNode;
import com.bretth.osmosis.core.mysql.v0_5.impl.WayNodeComparator;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.PeekableIterator;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Reads all ways from a database ordered by their identifier. It combines the
 * output of the way table readers to produce fully configured way objects.
 * 
 * @author Brett Henderson
 */
public class WayReader implements ReleasableIterator<Way> {
	
	private ReleasableIterator<Way> wayReader;
	private PeekableIterator<DBEntityTag> wayTagReader;
	private PeekableIterator<DBWayNode> wayNodeReader;
	private Way nextValue;
	private boolean nextValueLoaded;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public WayReader(DatabaseContext dbCtx) {
		wayReader = new WayTableReader(dbCtx);
		wayTagReader = new PeekableIterator<DBEntityTag>(
			new EntityTagTableReader(dbCtx, "way_tag", "way_id")
		);
		wayNodeReader = new PeekableIterator<DBWayNode>(
			new WayNodeTableReader(dbCtx)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext() {
		if (!nextValueLoaded && wayReader.hasNext()) {
			long wayId;
			Way way;
			List<DBWayNode> wayNodes;
			
			way = wayReader.next();
			wayId = way.getId();
			
			// Skip all way tags that are from a lower way.
			while (wayTagReader.hasNext()) {
				DBEntityTag wayTag;
				
				wayTag = wayTagReader.next();
				
				if (wayTag.getEntityId() < wayId) {
					wayTagReader.next();
				} else {
					break;
				}
			}
			
			// Load all tags matching this version of the way.
			while (wayTagReader.hasNext() && wayTagReader.peekNext().getEntityId() == wayId) {
				way.addTag(wayTagReader.next().getTag());
			}
			
			// Skip all way nodes that are from a lower way.
			while (wayNodeReader.hasNext()) {
				DBWayNode wayNode;
				
				wayNode = wayNodeReader.peekNext();
				
				if (wayNode.getWayId() < wayId) {
					wayNodeReader.next();
				} else {
					break;
				}
			}
			
			// Load all nodes matching this version of the way.
			wayNodes = new ArrayList<DBWayNode>();
			while (wayNodeReader.hasNext() && wayNodeReader.peekNext().getWayId() == wayId) {
				wayNodes.add(wayNodeReader.next());
			}
			// The underlying query sorts node references by way id but not
			// by their sequence number.
			Collections.sort(wayNodes, new WayNodeComparator());
			for (DBWayNode dbWayNode : wayNodes) {
				way.addWayNode(dbWayNode.getWayNode());
			}
			
			nextValue = way;
			nextValueLoaded = true;
		}
		
		return nextValueLoaded;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Way next() {
		Way result;
		
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
		wayReader.release();
		wayTagReader.release();
		wayNodeReader.release();
	}
}
