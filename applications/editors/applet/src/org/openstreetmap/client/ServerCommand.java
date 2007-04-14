package org.openstreetmap.client;

import java.io.IOException;

/**
 * Classes extending ServerCommand are able to be scheduled to the CommandManager 
 * to access the server.
 * 
 * The purpose of this structure is to help ensure that local map is always kept
 * consistent with what is known of server data and what we're trying to edit
 * the map to look like, without refreshing whole dataset from server, and
 * get some cohesion for map update code.
 * 
 * @author Imi
 */
public interface ServerCommand {
	/**
	 * The data should be modified to reflect the command in the internal data 
	 * representation. It is called in the event thread before the data is sent 
   * to the server.
   * 
   * Lock held on CommandManager.sleeper
	 */
	void preConnectionModifyData();
	
	/**
	 * Establish the server connection and update the command in the osm server.
   * Called on the <code>CommandManager</code> thread.
	 * If an exception is thrown, this counts as "failed".
	 * @return <code>true</code> on success or <code>false</code> on failure.
	 */
	boolean connectToServer() throws IOException;
	
	/**
	 * In case of connection failure, this undoes the modification done in 
	 * preConnectionsModifyData. The command may assume, that the data is in 
   * exact the state left after preConnectionsModifyData. After this step,
   * the data must be in exact the state before preConnectionsModifyData.
   * Called in event thread.
   * Lock held on CommandManager.sleeper
	 */
	void undoModifyData();
	
	/**
	 * This is called after an successful connection to the server to final 
   * adjust the data if necessary.
   * Called in event thread.
   * Lock held on CommandManager.sleeper
	 */
	void postConnectionModifyData();
}
