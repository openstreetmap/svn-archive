// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.pgsql.v0_6.impl;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.database.DatabaseLoginCredentials;
import com.bretth.osmosis.core.database.ReleasableStatementContainer;
import com.bretth.osmosis.core.domain.v0_6.Entity;
import com.bretth.osmosis.core.domain.v0_6.Node;
import com.bretth.osmosis.core.domain.v0_6.OsmUser;
import com.bretth.osmosis.core.domain.v0_6.Relation;
import com.bretth.osmosis.core.domain.v0_6.Way;
import com.bretth.osmosis.core.pgsql.common.DatabaseContext;
import com.bretth.osmosis.core.pgsql.common.NoSuchRecordException;
import com.bretth.osmosis.core.task.common.ChangeAction;


/**
 * Writes changes to a database.
 * 
 * @author Brett Henderson
 */
public class ChangeWriter {
	
	private DatabaseContext dbCtx;
	private UserDao userDao;
	private NodeDao nodeDao;
	private WayDao wayDao;
	private RelationDao relationDao;
	private Set<Integer> userSet;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 */
	public ChangeWriter(DatabaseLoginCredentials loginCredentials) {
		dbCtx = new DatabaseContext(loginCredentials);

		userDao = new UserDao(dbCtx);
		nodeDao = new NodeDao(dbCtx);
		wayDao = new WayDao(dbCtx);
		relationDao = new RelationDao(dbCtx);
		
		userSet = new HashSet<Integer>();
	}


	/**
	 * Writes the specified user to the database.
	 * 
	 * @param user
	 *            The user to write.
	 */
	private void writeUser(OsmUser user) {
		// Entities without a user assigned should not be written.
		if (OsmUser.NONE != user) {
			// Users will only be updated in the database once per changeset
			// run.
			if (userSet.contains(user.getId())) {
				int userId;
				OsmUser existingUser;

				userId = user.getId();

				try {
					existingUser = userDao.getUser(userId);

					if (!user.equals(existingUser)) {
						userDao.updateUser(user);
					}

				} catch (NoSuchRecordException e) {
					userDao.addUser(user);
				}

				userSet.add(user.getId());
			}
		}
	}


	/**
	 * Performs any validation and pre-processing required for all entity types.
	 */
	private void processEntityPrerequisites(Entity entity) {
		// We can't write an entity with a null timestamp.
		if (entity.getTimestamp() == null) {
			throw new OsmosisRuntimeException("Entity(" + entity.getType()
					+ ") " + entity.getId() + " does not have a timestamp set.");
		}
		
		// Process the user data.
		writeUser(entity.getUser());
	}


	/**
	 * Writes the specified node change to the database.
	 * 
	 * @param node
	 *            The node to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Node node, ChangeAction action) {
		processEntityPrerequisites(node);

		// If this is a create or modify, we must create or modify the records
		// in the database. Note that we don't use the input source to
		// distinguish between create and modify, we make this determination
		// based on our current data set.
		if (ChangeAction.Create.equals(action)
				|| ChangeAction.Modify.equals(action)) {
			if (nodeDao.exists(node.getId())) {
				nodeDao.modifyEntity(node);
			} else {
				nodeDao.addEntity(node);
			}

		} else {
			// Remove the node from the database.
			nodeDao.removeEntity(node.getId());
		}
	}


	/**
	 * Writes the specified way change to the database.
	 * 
	 * @param way
	 *            The way to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Way way, ChangeAction action) {
		processEntityPrerequisites(way);

		// If this is a create or modify, we must create or modify the records
		// in the database. Note that we don't use the input source to
		// distinguish between create and modify, we make this determination
		// based on our current data set.
		if (ChangeAction.Create.equals(action)
				|| ChangeAction.Modify.equals(action)) {
			if (wayDao.exists(way.getId())) {
				wayDao.modifyEntity(way);
			} else {
				wayDao.addEntity(way);
			}

		} else {
			// Remove the way from the database.
			wayDao.removeEntity(way.getId());
		}
	}


	/**
	 * Writes the specified relation change to the database.
	 * 
	 * @param relation
	 *            The relation to be written.
	 * @param action
	 *            The change to be applied.
	 */
	public void write(Relation relation, ChangeAction action) {
		processEntityPrerequisites(relation);

		// If this is a create or modify, we must create or modify the records
		// in the database. Note that we don't use the input source to
		// distinguish between create and modify, we make this determination
		// based on our current data set.
		if (ChangeAction.Create.equals(action)
				|| ChangeAction.Modify.equals(action)) {
			if (relationDao.exists(relation.getId())) {
				relationDao.modifyEntity(relation);
			} else {
				relationDao.addEntity(relation);
			}

		} else {
			// Remove the relation from the database.
			relationDao.removeEntity(relation.getId());
		}
	}


	/**
	 * Flushes all changes to the database.
	 */
	public void complete() {
		ReleasableStatementContainer statementContainer;
		CallableStatement updateStatement;
		
		statementContainer = new ReleasableStatementContainer();
		try {
			updateStatement = statementContainer.add(dbCtx.prepareCall("{call osmosisUpdate()}"));
			updateStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new OsmosisRuntimeException("Unable to invoke the osmosis update stored function.", e);
		} finally {
			statementContainer.release();
		}
		
		nodeDao.purgeAndResetAction();
		wayDao.purgeAndResetAction();
		relationDao.purgeAndResetAction();
		userDao.resetAction();
		
		dbCtx.commit();
	}


	/**
	 * Releases all database resources.
	 */
	public void release() {
		dbCtx.release();
	}
}
