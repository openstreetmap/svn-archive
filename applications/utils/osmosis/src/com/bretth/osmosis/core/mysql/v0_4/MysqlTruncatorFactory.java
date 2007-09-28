package com.bretth.osmosis.core.mysql.v0_4;

import java.util.Map;

import com.bretth.osmosis.core.mysql.common.MysqlTaskManagerFactory;
import com.bretth.osmosis.core.pipeline.common.RunnableTaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManager;


/**
 * The task manager factory for a database table truncator.
 * 
 * @author Brett Henderson
 */
public class MysqlTruncatorFactory extends MysqlTaskManagerFactory {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(String taskId, Map<String, String> taskArgs, Map<String, String> pipeArgs) {
		return new RunnableTaskManager(
			taskId,
			new MysqlTruncator(
				getDatabaseLoginCredentials(taskId, taskArgs),
				getDatabasePreferences(taskId, taskArgs)
			),
			pipeArgs
		);
	}
}
