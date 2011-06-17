package org.openstreetmap.osmosis.plugins.mysqlsimple;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * The MysqlSimplePlugin registers a new type of task factory: MysqlSimpleTaskFactory
 *  to be used when '--mysql-simple-dump' appears in command line args
 */
public class MysqlSimplePlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		MysqlSimpleTaskFactory mstf = new MysqlSimpleTaskFactory();

		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();

		tasks.put("mysql-simple-dump", mstf);
		tasks.put("wmsd", mstf);
		return tasks;
	}

}