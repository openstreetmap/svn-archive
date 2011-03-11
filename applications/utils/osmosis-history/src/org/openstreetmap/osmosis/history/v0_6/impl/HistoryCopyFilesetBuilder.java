// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history.v0_6.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.pgsnapshot.common.CopyFileWriter;
import org.openstreetmap.osmosis.pgsnapshot.common.PointBuilder;
import org.openstreetmap.osmosis.history.common.MinorEntityProcessor;
import org.openstreetmap.osmosis.history.domain.MinorWay;
import org.openstreetmap.osmosis.history.domain.MinorWayContainer;
import org.openstreetmap.osmosis.history.store.ExampleHistoryNodeStore;
import org.openstreetmap.osmosis.history.store.HistoryNodeStore;
import org.openstreetmap.osmosis.history.store.HistoryNodeStoreType;
import org.openstreetmap.osmosis.history.v0_6.impl.HistoryWayGeometryBuilder;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.MemberTypeValueMapper;
import org.openstreetmap.osmosis.pgsnapshot.v0_6.impl.CopyFileset;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.hstore.PGHStore;


/**
 * An OSM data sink for storing all data to a set of database dump files. These
 * files can be used for populating an empty database.
 * 
 * @author Peter Koerner
 */
public class HistoryCopyFilesetBuilder implements Sink, MinorEntityProcessor {
	private static final Logger LOG = Logger.getLogger(HistoryCopyFilesetBuilder.class.getName());
	
	private boolean enableBboxBuilder;
	private boolean enableLinestringBuilder;
	private boolean enableWayNodeVersionBuilder;
	private HistoryWayGeometryBuilder wayGeometryBuilder;
	private MemberTypeValueMapper memberTypeValueMapper;
	private PointBuilder pointBuilder;
	private Set<Integer> userSet;
	private HistoryNodeStore nodeStore;
	private MinorVersionBuilder minorVersionBuilder;
	private final boolean enableMinorVersionBuilder;

	private final CopyFileWriterSet copyWriterSet;
	
	
	public HistoryCopyFilesetBuilder(
			CopyFileset copyFileset, 
			boolean enableBboxBuilder,
			boolean enableLinestringBuilder, 
			boolean enableWayNodeVersionBuilder, 
			boolean enableMinorVersionBuilder, 
			HistoryNodeStoreType storeType) {
	
		this(CopyFileWriterSet.createFromFileset(copyFileset), enableBboxBuilder, 
				enableLinestringBuilder, enableWayNodeVersionBuilder, 
				enableMinorVersionBuilder, storeType);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param copyFileset
	 *            The set of COPY files to be populated.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public HistoryCopyFilesetBuilder(
			CopyFileWriterSet copyWriterSet, 
			boolean enableBboxBuilder,
			boolean enableLinestringBuilder, 
			boolean enableWayNodeVersionBuilder, 
			boolean enableMinorVersionBuilder, 
			HistoryNodeStoreType storeType) {
		this.copyWriterSet = copyWriterSet;
		this.enableBboxBuilder = enableBboxBuilder;
		this.enableLinestringBuilder = enableLinestringBuilder;
		this.enableWayNodeVersionBuilder = enableWayNodeVersionBuilder;
		this.enableMinorVersionBuilder = enableMinorVersionBuilder;
		
		if(enableBboxBuilder || enableLinestringBuilder || enableWayNodeVersionBuilder || enableMinorVersionBuilder)
		{
			if (HistoryNodeStoreType.Example.equals(storeType)) {
				nodeStore = new ExampleHistoryNodeStore(); 
			} else {
				throw new OsmosisRuntimeException("The store type " + storeType + " is not recognized.");
			}
		}
		
		pointBuilder = new PointBuilder();
		wayGeometryBuilder = new HistoryWayGeometryBuilder(nodeStore);
		memberTypeValueMapper = new MemberTypeValueMapper();
		
		if(enableMinorVersionBuilder)
			minorVersionBuilder = new MinorVersionBuilder(this, nodeStore);
		
		userSet = new HashSet<Integer>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		OsmUser user;
		CopyFileWriter userWriter = copyWriterSet.getUserWriter();
			
		// Write a user entry if the user doesn't already exist.
		user = entityContainer.getEntity().getUser();
		if (!user.equals(OsmUser.NONE)) {
			if (!userSet.contains(user.getId())) {
				userWriter.writeField(user.getId());
				userWriter.writeField(user.getName());
				userWriter.endRecord();
				
				userSet.add(user.getId());
			}
		}
		
		// Process the entity itself.
		entityContainer.process(this);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(BoundContainer boundContainer) {
		// Do nothing.
	}
	
	
	private PGHStore buildTags(Entity entity) {
		PGHStore tags;
		
		tags = new PGHStore();
		for (Tag tag : entity.getTags()) {
			tags.put(tag.getKey(), tag.getValue());
		}
		
		return tags;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		Node node;
		CopyFileWriter nodeWriter = copyWriterSet.getNodeWriter();
		
		node = nodeContainer.getEntity();
		
		nodeWriter.writeField(node.getId());
		nodeWriter.writeField(node.getVersion());
		nodeWriter.writeField(node.getUser().getId());
		nodeWriter.writeField(node.getTimestamp());
		nodeWriter.writeField(node.getChangesetId());
		nodeWriter.writeField(buildTags(node));
		nodeWriter.writeField(pointBuilder.createPoint(node.getLatitude(), node.getLongitude()));
		nodeWriter.endRecord();
		
		if(enableWayNodeVersionBuilder || enableBboxBuilder || enableLinestringBuilder || enableMinorVersionBuilder)
		{
			nodeStore.addNode(node);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way = wayContainer.getEntity();
		
		if(enableMinorVersionBuilder)
		{
			minorVersionBuilder.processWay(way);
		}
		
		process(new MinorWayContainer(wayContainer));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void process(MinorWayContainer wayContainer) {
		MinorWay way;
		int sequenceId;
		List<Long> nodeIds;
		CopyFileWriter wayWriter = copyWriterSet.getWayWriter();
		CopyFileWriter wayNodeWriter = copyWriterSet.getWayNodeWriter();
		
		way = wayContainer.getEntity();

		// TODO: how to store node versions in IntArray
		nodeIds = new ArrayList<Long>(way.getWayNodes().size());
		for (WayNode wayNode : way.getWayNodes()) {
			nodeIds.add(wayNode.getNodeId());
		}
		
		// Ignore ways with a single node because they can't be loaded into postgis.
		if (way.getWayNodes().size() > 1) {

			LOG.fine("writing way "+way.getId()+" v"+way.getVersion()+"/"+way.getMinorVersion());
			wayWriter.writeField(way.getId());
			wayWriter.writeField(way.getVersion());
			wayWriter.writeField(way.getUser().getId());
			wayWriter.writeField(way.getTimestamp());
			wayWriter.writeField(way.getChangesetId());
			wayWriter.writeField(buildTags(way));
			wayWriter.writeField(nodeIds);

			if (enableBboxBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayBbox(way));
			}
			if (enableLinestringBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayLinestring(way));
			}
			if (enableMinorVersionBuilder) {
				wayWriter.writeField(way.getMinorVersion());
			}
			
			wayWriter.endRecord();
			
			sequenceId = 0;
			for (WayNode wayNode : way.getWayNodes()) {
				if(enableWayNodeVersionBuilder)
				{
					Node node = nodeStore.findNode(wayNode.getNodeId(), way.getTimestamp());
					
					if(node == null)
						continue;
					
					wayNodeWriter.writeField(way.getId());
					wayNodeWriter.writeField(node.getId());
					wayNodeWriter.writeField(sequenceId++);
					wayNodeWriter.writeField(way.getVersion());
					wayNodeWriter.writeField(node.getVersion());
					if (enableMinorVersionBuilder)
						wayNodeWriter.writeField(way.getMinorVersion());
					
					wayNodeWriter.endRecord();
				}
				else
				{
					wayNodeWriter.writeField(way.getId());
					wayNodeWriter.writeField(wayNode.getNodeId());
					wayNodeWriter.writeField(sequenceId++);
					wayNodeWriter.writeField(way.getVersion());
					wayNodeWriter.endRecord();
				}
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		int memberSequenceId;
		CopyFileWriter relationWriter = copyWriterSet.getRelationWriter();
		CopyFileWriter relationMemberWriter = copyWriterSet.getRelationMemberWriter();
		
		relation = relationContainer.getEntity();
		
		relationWriter.writeField(relation.getId());
		relationWriter.writeField(relation.getVersion());
		relationWriter.writeField(relation.getUser().getId());
		relationWriter.writeField(relation.getTimestamp());
		relationWriter.writeField(relation.getChangesetId());
		relationWriter.writeField(buildTags(relation));
		relationWriter.endRecord();
		
		memberSequenceId = 0;
		for (RelationMember member : relation.getMembers()) {
			relationMemberWriter.writeField(relation.getId());
			relationMemberWriter.writeField(member.getMemberId());
			relationMemberWriter.writeField(memberTypeValueMapper.getMemberType(member.getMemberType()));
			relationMemberWriter.writeField(member.getMemberRole());
			relationMemberWriter.writeField(memberSequenceId++);
			relationMemberWriter.endRecord();
		}
	}
	
	
	/**
	 * Writes any buffered data to the database and commits. 
	 */
	public void complete() {
		if(minorVersionBuilder != null)
			minorVersionBuilder.complete();
		
		copyWriterSet.complete();
	}
	
	
	/**
	 * Releases all resources.
	 */
	public void release() {
		if(minorVersionBuilder != null)
			minorVersionBuilder.release();
		
		copyWriterSet.release();
	}
}
