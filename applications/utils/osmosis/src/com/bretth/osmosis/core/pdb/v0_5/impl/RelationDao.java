// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pdb.v0_5.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_5.EntityType;
import com.bretth.osmosis.core.domain.v0_5.Relation;
import com.bretth.osmosis.core.domain.v0_5.RelationMember;
import com.bretth.osmosis.core.domain.v0_5.Tag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBEntityTag;
import com.bretth.osmosis.core.mysql.v0_5.impl.DBRelationMember;
import com.bretth.osmosis.core.pdb.common.DatabaseContext;
import com.bretth.osmosis.core.store.Releasable;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao implements Releasable {
	private static final String SQL_SELECT_SINGLE_RELATION = "SELECT id, tstamp, user_name FROM relations WHERE id=?";
	private static final String SQL_SELECT_SINGLE_RELATION_TAG = "SELECT relation_id AS entity_id, k, v FROM relation_tags WHERE relation_id=?";
	private static final String SQL_SELECT_SINGLE_RELATION_MEMBER = "SELECT relation_id, member_id, member_role, member_type FROM relation_members WHERE relation_id=?";
	
	private DatabaseContext dbCtx;
	private PreparedStatement singleRelationStatement;
	private PreparedStatement singleRelationTagStatement;
	private PreparedStatement singleRelationMemberStatement;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public RelationDao(DatabaseContext dbCtx) {
		this.dbCtx = dbCtx;
	}
	
	
	/**
	 * Builds a tag from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded tag.
	 */
	private DBEntityTag buildTag(ResultSet resultSet) {
		try {
			return new DBEntityTag(
				resultSet.getLong("entity_id"),
				new Tag(
					resultSet.getString("k"),
					resultSet.getString("v")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a tag from the current recordset row.", e);
		} 
	}
	
	
	/**
	 * Builds a relation member from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded relation member.
	 */
	private DBRelationMember buildRelationMember(ResultSet resultSet) {
		try {
			EntityType memberType;
			
			// Use the member type as an offset into the entity types to
			// determine the type of the member.
			memberType = EntityType.values()[resultSet.getByte("member_type")];
			
			return new DBRelationMember(
				resultSet.getLong("relation_id"),
				new RelationMember(
					resultSet.getLong("member_id"),
					memberType,
					resultSet.getString("member_role")
				)
			);
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a relation member from the current recordset row.", e);
		} 
	}
	
	
	/**
	 * Builds a relation from the current result set row.
	 * 
	 * @param resultSet
	 *            The result set.
	 * @return The newly loaded relation.
	 */
	private Relation buildRelation(ResultSet resultSet) {
		try {
			return new Relation(
				resultSet.getLong("id"),
				new Date(resultSet.getTimestamp("tstamp").getTime()),
				resultSet.getString("user_name")
			);
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to build a relation from the current recordset row.", e);
		}
	}
	
	
	/**
	 * Loads the specified relation from the database.
	 * 
	 * @param relationId
	 *            The unique identifier of the relation.
	 * @return The loaded relation.
	 */
	public Relation getRelation(long relationId) {
		ResultSet resultSet = null;
		Relation relation;
		
		if (singleRelationStatement == null) {
			singleRelationStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_RELATION);
		}
		if (singleRelationTagStatement == null) {
			singleRelationTagStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_RELATION_TAG);
		}
		if (singleRelationMemberStatement == null) {
			singleRelationMemberStatement = dbCtx.prepareStatement(SQL_SELECT_SINGLE_RELATION_MEMBER);
		}
		
		try {
			singleRelationStatement.setLong(1, relationId);
			singleRelationTagStatement.setLong(1, relationId);
			singleRelationMemberStatement.setLong(1, relationId);
			
			resultSet = singleRelationStatement.executeQuery();
			
			if (!resultSet.next()) {
				throw new OsmosisRuntimeException("Relation " + relationId + " doesn't exist.");
			}
			relation = buildRelation(resultSet);
			
			resultSet.close();
			resultSet = null;
			
			resultSet = singleRelationTagStatement.executeQuery();
			while (resultSet.next()) {
				relation.addTag(buildTag(resultSet).getTag());
			}
			
			resultSet.close();
			resultSet = null;
			
			resultSet = singleRelationMemberStatement.executeQuery();
			while (resultSet.next()) {
				relation.addMember(buildRelationMember(resultSet).getRelationMember());
			}
			
			resultSet.close();
			resultSet = null;
			
			return relation;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Query failed for relation " + relationId + ".");
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					// Do nothing.
				}
			}
		}
	}
	
	
	/**
	 * Returns an iterator providing access to all nodes in the database.
	 * 
	 * @return The node iterator.
	 */
	public ReleasableIterator<Relation> iterate() {
		return new RelationReader(dbCtx);
	}
	
	
	/**
	 * Allows all data within a bounding box to be iterated across.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 * @param completeRelations
	 *            If true, all relations within the relations will be returned even if
	 *            they lie outside the box.
	 * @return An iterator pointing to the start of the result data.
	 */
	public ReleasableIterator<Relation> iterateBoundingBox(double left, double right, double top, double bottom, boolean completeRelations) {
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (singleRelationStatement != null) {
			try {
				singleRelationStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleRelationStatement = null;
		}
		if (singleRelationTagStatement != null) {
			try {
				singleRelationTagStatement.close();
			} catch (SQLException e) {
				// Do nothing.
			}
			
			singleRelationTagStatement = null;
		}
	}
}
