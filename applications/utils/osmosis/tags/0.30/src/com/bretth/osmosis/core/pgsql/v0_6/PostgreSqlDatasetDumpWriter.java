// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.bretth.osmosis.core.container.v0_6.BoundContainer;
import com.bretth.osmosis.core.container.v0_6.EntityContainer;
import com.bretth.osmosis.core.container.v0_6.EntityProcessor;
import com.bretth.osmosis.core.container.v0_6.NodeContainer;
import com.bretth.osmosis.core.container.v0_6.RelationContainer;
import com.bretth.osmosis.core.container.v0_6.WayContainer;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.domain.v0_6.WayNode;
import com.bretth.osmosis.core.lifecycle.CompletableContainer;
import com.bretth.osmosis.core.pgsql.common.CopyFileWriter;
import com.bretth.osmosis.core.pgsql.common.PointBuilder;
import com.bretth.osmosis.core.pgsql.v0_6.impl.ChangesetAction;
import com.bretth.osmosis.core.pgsql.v0_6.impl.MemberTypeValueMapper;
import com.bretth.osmosis.core.pgsql.v0_6.impl.NodeLocationStoreType;
import com.bretth.osmosis.core.pgsql.v0_6.impl.WayGeometryBuilder;
import com.bretth.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database dump file. This task is
 * intended for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetDumpWriter implements Sink, EntityProcessor {
	
	private static final String USER_SUFFIX = "users.txt";
	private static final String NODE_SUFFIX = "nodes.txt";
	private static final String NODE_TAG_SUFFIX = "node_tags.txt";
	private static final String WAY_SUFFIX = "ways.txt";
	private static final String WAY_TAG_SUFFIX = "way_tags.txt";
	private static final String WAY_NODE_SUFFIX = "way_nodes.txt";
	private static final String RELATION_SUFFIX = "relations.txt";
	private static final String RELATION_TAG_SUFFIX = "relation_tags.txt";
	private static final String RELATION_MEMBER_SUFFIX = "relation_members.txt";
	
	
	private boolean enableBboxBuilder;
	private boolean enableLinestringBuilder;
	private WayGeometryBuilder wayGeometryBuilder;
	private CompletableContainer writerContainer;
	private MemberTypeValueMapper memberTypeValueMapper;
	private CopyFileWriter userWriter;
	private CopyFileWriter nodeWriter;
	private CopyFileWriter nodeTagWriter;
	private CopyFileWriter wayWriter;
	private CopyFileWriter wayTagWriter;
	private CopyFileWriter wayNodeWriter;
	private CopyFileWriter relationWriter;
	private CopyFileWriter relationTagWriter;
	private CopyFileWriter relationMemberWriter;
	private PointBuilder pointBuilder;
	private Set<Integer> userSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filePrefix
	 *            The prefix to prepend to all generated file names.
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
	public PostgreSqlDatasetDumpWriter(File filePrefix, boolean enableBboxBuilder, boolean enableLinestringBuilder, NodeLocationStoreType storeType) {
		this.enableBboxBuilder = enableBboxBuilder;
		this.enableLinestringBuilder = enableLinestringBuilder;
		
		writerContainer = new CompletableContainer();
		
		userWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, USER_SUFFIX)));
		nodeWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, NODE_SUFFIX)));
		nodeTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, NODE_TAG_SUFFIX)));
		wayWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_SUFFIX)));
		wayTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_TAG_SUFFIX)));
		wayNodeWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_NODE_SUFFIX)));
		relationWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_SUFFIX)));
		relationTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_TAG_SUFFIX)));
		relationMemberWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_MEMBER_SUFFIX)));
		
		pointBuilder = new PointBuilder();
		wayGeometryBuilder = new WayGeometryBuilder(storeType);
		memberTypeValueMapper = new MemberTypeValueMapper();
		
		userSet = new HashSet<Integer>();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		OsmUser user;
		
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
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(NodeContainer nodeContainer) {
		Node node;
		
		node = nodeContainer.getEntity();
		
		nodeWriter.writeField(node.getId());
		nodeWriter.writeField(node.getVersion());
		nodeWriter.writeField(node.getUser().getId());
		nodeWriter.writeField(node.getTimestamp());
		nodeWriter.writeField(ChangesetAction.CREATE.getDatabaseValue());
		nodeWriter.writeField(pointBuilder.createPoint(node.getLatitude(), node.getLongitude()));
		nodeWriter.endRecord();
		
		for (Tag tag : node.getTagList()) {
			nodeTagWriter.writeField(node.getId());
			nodeTagWriter.writeField(tag.getKey());
			nodeTagWriter.writeField(tag.getValue());
			nodeTagWriter.endRecord();
		}
		
		if (enableBboxBuilder || enableLinestringBuilder) {
			wayGeometryBuilder.addNodeLocation(node);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(WayContainer wayContainer) {
		Way way;
		int sequenceId;
		
		way = wayContainer.getEntity();
		
		// Ignore ways with a single node because they can't be loaded into postgis.
		if (way.getWayNodeList().size() > 1) {
			wayWriter.writeField(way.getId());
			wayWriter.writeField(way.getVersion());
			wayWriter.writeField(way.getUser().getId());
			wayWriter.writeField(way.getTimestamp());
			wayWriter.writeField(ChangesetAction.CREATE.getDatabaseValue());
			if (enableBboxBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayBbox(way));
			}
			if (enableLinestringBuilder) {
				wayWriter.writeField(wayGeometryBuilder.createWayLinestring(way));
			}
			wayWriter.endRecord();
			
			for (Tag tag : way.getTagList()) {
				wayTagWriter.writeField(way.getId());
				wayTagWriter.writeField(tag.getKey());
				wayTagWriter.writeField(tag.getValue());
				wayTagWriter.endRecord();
			}
			
			sequenceId = 0;
			for (WayNode wayNode : way.getWayNodeList()) {
				wayNodeWriter.writeField(way.getId());
				wayNodeWriter.writeField(wayNode.getNodeId());
				wayNodeWriter.writeField(sequenceId++);
				wayNodeWriter.endRecord();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(RelationContainer relationContainer) {
		Relation relation;
		int memberSequenceId;
		
		relation = relationContainer.getEntity();
		
		relationWriter.writeField(relation.getId());
		relationWriter.writeField(relation.getVersion());
		relationWriter.writeField(relation.getUser().getId());
		relationWriter.writeField(relation.getTimestamp());
		relationWriter.writeField(ChangesetAction.CREATE.getDatabaseValue());
		relationWriter.endRecord();
		
		for (Tag tag : relation.getTagList()) {
			relationTagWriter.writeField(relation.getId());
			relationTagWriter.writeField(tag.getKey());
			relationTagWriter.writeField(tag.getValue());
			relationTagWriter.endRecord();
		}
		
		memberSequenceId = 0;
		for (RelationMember member : relation.getMemberList()) {
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
		writerContainer.complete();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		writerContainer.release();
		wayGeometryBuilder.release();
	}
}
