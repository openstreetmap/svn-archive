// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.pgsql.v0_5;

import java.io.File;

import org.openstreetmap.osmosis.core.container.v0_5.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityContainer;
import org.openstreetmap.osmosis.core.container.v0_5.EntityProcessor;
import org.openstreetmap.osmosis.core.container.v0_5.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_5.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_5.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_5.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_5.Node;
import org.openstreetmap.osmosis.core.domain.v0_5.Relation;
import org.openstreetmap.osmosis.core.domain.v0_5.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_5.Tag;
import org.openstreetmap.osmosis.core.domain.v0_5.Way;
import org.openstreetmap.osmosis.core.domain.v0_5.WayNode;
import org.openstreetmap.osmosis.core.lifecycle.CompletableContainer;
import org.openstreetmap.osmosis.core.pgsql.common.CopyFileWriter;
import org.openstreetmap.osmosis.core.pgsql.common.PointBuilder;
import org.openstreetmap.osmosis.core.task.v0_5.Sink;


/**
 * An OSM data sink for storing all data to a database dump file. This task is
 * intended for populating an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlDatasetDumpWriter implements Sink, EntityProcessor {
	
	private static final String NODE_SUFFIX = "nodes.txt";
	private static final String NODE_TAG_SUFFIX = "node_tags.txt";
	private static final String WAY_SUFFIX = "ways.txt";
	private static final String WAY_TAG_SUFFIX = "way_tags.txt";
	private static final String WAY_NODE_SUFFIX = "way_nodes.txt";
	private static final String RELATION_SUFFIX = "relations.txt";
	private static final String RELATION_TAG_SUFFIX = "relation_tags.txt";
	private static final String RELATION_MEMBER_SUFFIX = "relation_members.txt";
	
	
	private CompletableContainer writerContainer;
	private CopyFileWriter nodeWriter;
	private CopyFileWriter nodeTagWriter;
	private CopyFileWriter wayWriter;
	private CopyFileWriter wayTagWriter;
	private CopyFileWriter wayNodeWriter;
	private CopyFileWriter relationWriter;
	private CopyFileWriter relationTagWriter;
	private CopyFileWriter relationMemberWriter;
	private PointBuilder pointBuilder;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param filePrefix
	 *            The prefix to prepend to all generated file names.
	 */
	public PostgreSqlDatasetDumpWriter(File filePrefix) {
		writerContainer = new CompletableContainer();
		
		nodeWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, NODE_SUFFIX)));
		nodeTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, NODE_TAG_SUFFIX)));
		wayWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_SUFFIX)));
		wayTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_TAG_SUFFIX)));
		wayNodeWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, WAY_NODE_SUFFIX)));
		relationWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_SUFFIX)));
		relationTagWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_TAG_SUFFIX)));
		relationMemberWriter = writerContainer.add(new CopyFileWriter(new File(filePrefix, RELATION_MEMBER_SUFFIX)));
		
		pointBuilder = new PointBuilder();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
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
		nodeWriter.writeField(node.getUser().getId());
		nodeWriter.writeField(node.getUser().getName());
		nodeWriter.writeField(node.getTimestamp());
		nodeWriter.writeField(pointBuilder.createPoint(node.getLatitude(), node.getLongitude()));
		nodeWriter.endRecord();
		
		for (Tag tag : node.getTagList()) {
			nodeTagWriter.writeField(node.getId());
			nodeTagWriter.writeField(tag.getKey());
			nodeTagWriter.writeField(tag.getValue());
			nodeTagWriter.endRecord();
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
			wayWriter.writeField(way.getUser().getId());
			wayWriter.writeField(way.getUser().getName());
			wayWriter.writeField(way.getTimestamp());
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
		EntityType[] entityTypes;
		
		entityTypes = EntityType.values();
		
		relation = relationContainer.getEntity();
		
		relationWriter.writeField(relation.getId());
		relationWriter.writeField(relation.getUser().getId());
		relationWriter.writeField(relation.getUser().getName());
		relationWriter.writeField(relation.getTimestamp());
		relationWriter.endRecord();
		
		for (Tag tag : relation.getTagList()) {
			relationTagWriter.writeField(relation.getId());
			relationTagWriter.writeField(tag.getKey());
			relationTagWriter.writeField(tag.getValue());
			relationTagWriter.endRecord();
		}
		
		for (RelationMember member : relation.getMemberList()) {
			relationMemberWriter.writeField(relation.getId());
			relationMemberWriter.writeField(member.getMemberId());
			relationMemberWriter.writeField(member.getMemberRole());
			for (byte i = 0; i < entityTypes.length; i++) {
				if (entityTypes[i].equals(member.getMemberType())) {
					relationMemberWriter.writeField(i);
				}
			}
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
	}
}
