package org.openstreetmap.osmosis.core.migrate.impl;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


/**
 * Provides conversion routines for entity classes between 0.5 and 0.6 format.
 * 
 * @author Brett Henderson
 */
public class EntityMigrater {
	
	private OsmUser migrateUser(org.openstreetmap.osmosis.core.domain.v0_5.OsmUser user) {
		if (!user.equals(org.openstreetmap.osmosis.core.domain.v0_5.OsmUser.NONE)) {
			return new OsmUser(user.getId(), user.getName());
		} else {
			return OsmUser.NONE;
		}
	}
	
	
	private List<Tag> migrateTags(org.openstreetmap.osmosis.core.domain.v0_5.Entity entity) {
		List<org.openstreetmap.osmosis.core.domain.v0_5.Tag> oldTags;
		List<Tag> newTags;
		
		oldTags = entity.getTagList();
		newTags = new ArrayList<Tag>(oldTags.size());
		
		for (org.openstreetmap.osmosis.core.domain.v0_5.Tag oldTag : oldTags) {
			newTags.add(new Tag(oldTag.getKey(), oldTag.getValue()));
		}
		
		return newTags;
	}
	
	
	private List<WayNode> migrateWayNodes(org.openstreetmap.osmosis.core.domain.v0_5.Way way) {
		List<org.openstreetmap.osmosis.core.domain.v0_5.WayNode> oldWayNodes;
		List<WayNode> newWayNodes;
		
		oldWayNodes = way.getWayNodeList();
		newWayNodes = new ArrayList<WayNode>(oldWayNodes.size());
		
		for (org.openstreetmap.osmosis.core.domain.v0_5.WayNode oldWayNode : oldWayNodes) {
			newWayNodes.add(new WayNode(oldWayNode.getNodeId()));
		}
		
		return newWayNodes;
	}
	
	
	private List<RelationMember> migrateRelationMembers(org.openstreetmap.osmosis.core.domain.v0_5.Relation relation) {
		List<org.openstreetmap.osmosis.core.domain.v0_5.RelationMember> oldRelationMembers;
		List<RelationMember> newRelationMembers;
		
		oldRelationMembers = relation.getMemberList();
		newRelationMembers = new ArrayList<RelationMember>(oldRelationMembers.size());
		
		for (org.openstreetmap.osmosis.core.domain.v0_5.RelationMember oldRelationMember : oldRelationMembers) {
			newRelationMembers.add(
				new RelationMember(
					oldRelationMember.getMemberId(),
					EntityType.valueOf(oldRelationMember.getMemberType().name()),
					oldRelationMember.getMemberRole()
				)
			);
		}
		
		return newRelationMembers;
	}
	
	
	/**
	 * Migrates a bound object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Bound migrate(org.openstreetmap.osmosis.core.domain.v0_5.Bound entity) {
		return new Bound(entity.getOrigin());
	}
	
	
	/**
	 * Migrates a node object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Node migrate(org.openstreetmap.osmosis.core.domain.v0_5.Node entity) {
		Node newEntity;
		
		newEntity = new Node(
			entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser()),
			migrateTags(entity),
			entity.getLatitude(),
			entity.getLongitude()
		);
		
		return newEntity;
	}
	
	
	/**
	 * Migrates a way object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Way migrate(org.openstreetmap.osmosis.core.domain.v0_5.Way entity) {
		Way newEntity;
		
		newEntity = new Way(
			entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser()),
			migrateTags(entity),
			migrateWayNodes(entity)
		);
		
		return newEntity;
	}
	
	
	/**
	 * Migrates a relation object from 0.5 to 0.6 format.
	 * 
	 * @param entity
	 *            The entity to migrate.
	 * @return The entity in 0.6 format.
	 */
	public Relation migrate(org.openstreetmap.osmosis.core.domain.v0_5.Relation entity) {
		Relation newEntity;
		
		newEntity = new Relation(
			entity.getId(),
			1,
			entity.getTimestamp(),
			migrateUser(entity.getUser()),
			migrateTags(entity),
			migrateRelationMembers(entity)
		);
		
		return newEntity;
	}
}
