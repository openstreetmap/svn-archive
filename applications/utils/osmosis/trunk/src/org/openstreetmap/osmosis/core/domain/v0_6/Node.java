// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.domain.v0_6;

import java.util.Collection;
import java.util.Date;

import org.openstreetmap.osmosis.core.domain.common.SimpleTimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;
import org.openstreetmap.osmosis.core.util.FixedPrecisionCoordinateConvertor;


/**
 * A data class representing a single OSM node.
 * 
 * @author Brett Henderson
 */
public class Node extends Entity implements Comparable<Node> {
	
	private double latitude;
	private double longitude;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestamp
	 *            The last updated timestamp.
	 * @param user
	 *            The user that last modified this entity.
	 * @param tags
	 *            The tags to apply to the object.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, int version, Date timestamp, OsmUser user, Collection<Tag> tags, double latitude, double longitude) {
		// Chain to the more-specific constructor
		this(id, version, new SimpleTimestampContainer(timestamp), user, tags, latitude, longitude);
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 *            The unique identifier.
	 * @param version
	 *            The version of the entity.
	 * @param timestampContainer
	 *            The container holding the timestamp in an alternative
	 *            timestamp representation.
	 * @param user
	 *            The name of the user that last modified this entity.
	 * @param tags
	 *            The tags to apply to the object.
	 * @param latitude
	 *            The geographic latitude.
	 * @param longitude
	 *            The geographic longitude.
	 */
	public Node(long id, int version, TimestampContainer timestampContainer, OsmUser user, Collection<Tag> tags, double latitude, double longitude) {
		super(id, timestampContainer, user, version, tags);
		
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers
	 *            within the store.
	 */
	public Node(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);
		
		this.latitude = FixedPrecisionCoordinateConvertor.convertToDouble(sr.readInteger());
		this.longitude = FixedPrecisionCoordinateConvertor.convertToDouble(sr.readInteger());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);
		
		sw.writeInteger(FixedPrecisionCoordinateConvertor.convertToFixed(latitude));
		sw.writeInteger(FixedPrecisionCoordinateConvertor.convertToFixed(longitude));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getType() {
		return EntityType.Node;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Node) {
			return compareTo((Node) o) == 0;
		} else {
			return false;
		}
	}


	/**
	 * Compares this node to the specified node. The node comparison is based on
	 * a comparison of id, version, latitude, longitude, timestamp and tags in that
	 * order.
	 * 
	 * @param comparisonNode
	 *            The node to compare to.
	 * @return 0 if equal, <0 if considered "smaller", and >0 if considered
	 *         "bigger".
	 */
	public int compareTo(Node comparisonNode) {
		if (this.getId() < comparisonNode.getId()) {
			return -1;
		}
		
		if (this.getId() > comparisonNode.getId()) {
			return 1;
		}
		
		if (this.getVersion() < comparisonNode.getVersion()) {
			return -1;
		}
		
		if (this.getVersion() > comparisonNode.getVersion()) {
			return 1;
		}
		
		if (this.latitude < comparisonNode.latitude) {
			return -1;
		}
		
		if (this.latitude > comparisonNode.latitude) {
			return 1;
		}
		
		if (this.longitude < comparisonNode.longitude) {
			return -1;
		}
		
		if (this.longitude > comparisonNode.longitude) {
			return 1;
		}
		
		if (this.getTimestamp() == null && comparisonNode.getTimestamp() != null) {
			return -1;
		}
		if (this.getTimestamp() != null && comparisonNode.getTimestamp() == null) {
			return 1;
		}
		if (this.getTimestamp() != null && comparisonNode.getTimestamp() != null) {
			int result;
			
			result = this.getTimestamp().compareTo(comparisonNode.getTimestamp());
			
			if (result != 0) {
				return result;
			}
		}
		
		return compareTags(comparisonNode.getTags());
	}
	
	
	/**
	 * @return The latitude. 
	 */
	public double getLatitude() {
		return latitude;
	}
	
	
	/**
	 * @return The longitude. 
	 */
	public double getLongitude() {
		return longitude;
	}
}
