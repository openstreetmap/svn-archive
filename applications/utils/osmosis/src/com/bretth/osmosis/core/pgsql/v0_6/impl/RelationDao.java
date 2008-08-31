// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.util.ArrayList;
import java.util.List;

import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.RelationMember;
import com.bretth.osmosis.core.mysql.v0_6.impl.DBEntityFeature;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.store.ReleasableIterator;


/**
 * Performs all relation-specific db operations.
 * 
 * @author Brett Henderson
 */
public class RelationDao extends EntityDao<Relation> {
	
	private EntityFeatureDao<RelationMember, DBEntityFeature<RelationMember>> relationMemberDao;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param dbCtx
	 *            The database context to use for accessing the database.
	 */
	public RelationDao(DatabaseContext dbCtx) {
		super(dbCtx, new RelationBuilder());
		
		relationMemberDao = new EntityFeatureDao<RelationMember, DBEntityFeature<RelationMember>>(dbCtx, new RelationMemberBuilder());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Relation getEntity(long entityId) {
		Relation relation;
		
		relation = super.getEntity(entityId);
		
		relation.addMembers(relationMemberDao.getRawList(entityId));
		
		return relation;
	}
	
	
	/**
	 * Adds the specified relation member list to the database.
	 * 
	 * @param entityId
	 *            The identifier of the entity to add these features to.
	 * @param memberList
	 *            The list of features to add.
	 */
	private void addMemberList(long entityId, List<RelationMember> memberList) {
		List<DBEntityFeature<RelationMember>> dbList;
		
		dbList = new ArrayList<DBEntityFeature<RelationMember>>(memberList.size());
		
		for (RelationMember member : memberList) {
			dbList.add(new DBEntityFeature<RelationMember>(entityId, member));
		}
		
		relationMemberDao.addList(dbList);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addEntity(Relation entity) {
		super.addEntity(entity);
		
		addMemberList(entity.getId(), entity.getMemberList());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntity(Relation entity) {
		long relationId;
		
		super.modifyEntity(entity);
		
		relationId = entity.getId();
		relationMemberDao.removeList(relationId);
		addMemberList(entity.getId(), entity.getMemberList());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeEntity(long entityId) {
		relationMemberDao.removeList(entityId);
		
		super.removeEntity(entityId);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReleasableIterator<Relation> iterate() {
		return new RelationReader(getDatabaseContext());
	}
}
