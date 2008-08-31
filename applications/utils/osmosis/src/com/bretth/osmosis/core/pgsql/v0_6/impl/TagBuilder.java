// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.domain.v0_6.Tag;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityFeature;


/**
 * Reads and writes tags to jdbc classes.
 * 
 * @author Brett Henderson
 */
public class TagBuilder extends EntityFeatureBuilder<DBEntityFeature<Tag>> {
	private String parentEntityName;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentEntityName
	 *            The name of the parent entity. This is used to generate SQL
	 *            statements for the correct tag table name.
	 */
	public TagBuilder(String parentEntityName) {
		this.parentEntityName = parentEntityName;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEntityName() {
		return parentEntityName + "_tags";
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlSelect(boolean filterByEntityId, boolean orderBy) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("SELECT ").append(parentEntityName).append("_id AS entity_id, k, v FROM ");
		resultSql.append(parentEntityName).append("_tags f");
		if (filterByEntityId) {
			resultSql.append(" WHERE entity_id = ?");
		}
		if (orderBy) {
			resultSql.append(getSqlDefaultOrderBy());
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlInsert() {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("INSERT INTO ").append(parentEntityName).append("_tags (");
		resultSql.append(parentEntityName).append("_id, k, v) VALUES (?, ?, ?)");
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSqlDelete(boolean filterByEntityId) {
		StringBuilder resultSql;
		
		resultSql = new StringBuilder();
		resultSql.append("DELETE FROM ").append(parentEntityName).append("_tags");
		if (filterByEntityId) {
			resultSql.append(" WHERE ").append(parentEntityName).append("_id = ?");
		}
		
		return resultSql.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public DBEntityFeature<Tag> buildEntity(ResultSet resultSet) {
		try {
			return new DBEntityFeature<Tag>(
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
	 * {@inheritDoc}
	 */
	@Override
	public int populateEntityParameters(PreparedStatement statement, int initialIndex, DBEntityFeature<Tag> entityFeature) {
		try {
			int prmIndex;
			Tag tag;
			
			tag = entityFeature.getEntityFeature();
			
			prmIndex = initialIndex;
			
			statement.setLong(prmIndex++, entityFeature.getEntityId());
			statement.setString(prmIndex++, tag.getKey());
			statement.setString(prmIndex++, tag.getValue());
			
			return prmIndex;
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException(
				"Unable to populate tag parameters for entity " +
				parentEntityName + " " + entityFeature.getEntityId() + "."
			);
		}
	}
}
