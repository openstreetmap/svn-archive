package org.openstreetmap.osmosis.history.domain;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.store.StoreClassRegister;
import org.openstreetmap.osmosis.core.store.StoreReader;
import org.openstreetmap.osmosis.core.store.StoreWriter;

public class MinorWay extends Way {

	private int minorVersion = 0;
	
	public MinorWay(CommonEntityData entityData, List<WayNode> wayNodes) {
		super(entityData, wayNodes);
	}
	
	public MinorWay(CommonEntityData entityData, List<WayNode> wayNodes, int minorVersion) {
		super(entityData, wayNodes);
		init(minorVersion);
	}

	public MinorWay(CommonEntityData entityData) {
		super(entityData);
	}
	
	public MinorWay(CommonEntityData entityData, int minorVersion) {
		super(entityData);
		init(minorVersion);
	}

	public MinorWay(long id, int version, Date timestamp, OsmUser user,
			long changesetId, Collection<Tag> tags, List<WayNode> wayNodes) {
		super(id, version, timestamp, user, changesetId, tags, wayNodes);
	}
	
	public MinorWay(long id, int version, int minorVersion, Date timestamp, OsmUser user,
			long changesetId, Collection<Tag> tags, List<WayNode> wayNodes) {
		super(id, version, timestamp, user, changesetId, tags, wayNodes);
		init(minorVersion);
	}

	public MinorWay(long id, int version, Date timestamp, OsmUser user,
			long changesetId) {
		super(id, version, timestamp, user, changesetId);
	}
	
	public MinorWay(long id, int version, int minorVersion, Date timestamp, OsmUser user,
			long changesetId) {
		super(id, version, timestamp, user, changesetId);
		init(minorVersion);
	}

	public MinorWay(long id, int version,
			TimestampContainer timestampContainer, OsmUser user,
			long changesetId, Collection<Tag> tags, List<WayNode> wayNodes) {
		super(id, version, timestampContainer, user, changesetId, tags, wayNodes);
	}

	public MinorWay(long id, int version, int minorVersion,
			TimestampContainer timestampContainer, OsmUser user,
			long changesetId, Collection<Tag> tags, List<WayNode> wayNodes) {
		super(id, version, timestampContainer, user, changesetId, tags, wayNodes);
		init(minorVersion);
	}

	public MinorWay(long id, int version,
			TimestampContainer timestampContainer, OsmUser user,
			long changesetId) {
		super(id, version, timestampContainer, user, changesetId);
	}
	
	public MinorWay(long id, int version, int minorVersion,
			TimestampContainer timestampContainer, OsmUser user,
			long changesetId) {
		super(id, version, timestampContainer, user, changesetId);
		init(minorVersion);
	}
	
	public MinorWay(Way way) {
		this(way, 0);
	}
	
	public MinorWay(Way way, int minorVersion) {
		super(way.getId(), way.getVersion(), way.getTimestampContainer(), way.getUser(), way.getChangesetId(), way.getTags(), way.getWayNodes());
		this.minorVersion = minorVersion;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sr
	 *            The store to read state from.
	 * @param scr
	 *            Maintains the mapping between classes and their identifiers within the store.
	 */
	public MinorWay(StoreReader sr, StoreClassRegister scr) {
		super(sr, scr);

		this.minorVersion = sr.readInteger();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void store(StoreWriter sw, StoreClassRegister scr) {
		super.store(sw, scr);

		sw.writeInteger(minorVersion);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof MinorWay) {
			return compareTo((MinorWay) o) == 0;
		} else {
			return super.equals(o);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		/*
		 * As per the hashCode definition, this doesn't have to be unique it just has to return the
		 * same value for any two objects that compare equal. Using both id and version will provide
		 * a good distribution of values but is simple to calculate.
		 */
		return (int) getId() + getVersion() + getMinorVersion();
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(MinorWay comparisonWay) {
		int comp = super.compareTo(comparisonWay);
		
		if(comp != 0)
		{
			return comp;
		}
		
		if(comparisonWay.getMinorVersion() < minorVersion)
		{
			return -1;
		}
		
		if(comparisonWay.getMinorVersion() > minorVersion)
		{
			return 1;
		}
		
		return 0;
	}


	private void init(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		assertWriteable();
		
		this.minorVersion = minorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MinorWay getWriteableInstance() {
		if (isReadOnly()) {
			return new MinorWay(getId(), getVersion(), getMinorVersion(), getTimestampContainer(), getUser(), getChangesetId(), getTags(), getWayNodes());
		} else {
			return this;
		}
	}
	
}
