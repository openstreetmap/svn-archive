package org.openstreetmap.client;

import java.io.IOException;

/**
 * Classes extending ServerCommand are able to be sheduled to the CommandManager to
 * access the server.
 * 
 * @author Imi
 */
public interface ServerCommand {
	/**
	 * The data should be modified to reflect the command in the internal data 
	 * representation. It is called synchronous in the main thread before the 
	 * data is sent to the server.
	 */
	void preConnectionModifyData();
	
	/**
	 * Establish the server connection and update the command in the osm server.
	 * If an exception is thrown, this counts as "failed".
	 * @return <code>true</code> on success or <code>false</code> on failure.
	 */
	boolean connectToServer() throws IOException;
	
	/**
	 * In case of connection failure, this undoes the modification done in 
	 * preConnectionsModifyData. The command may assume, that the data is in exact the
	 * state left after preConnectionsModifyData. After this step, the data must be in exact
	 * the state before preConnectionsModifyData.
	 */
	void undoModifyData();
	
	/**
	 * This is called after an successfull connection to the server to final adjust the
	 * data if necessary.
	 */
	void postConnectionModifyData();
}
