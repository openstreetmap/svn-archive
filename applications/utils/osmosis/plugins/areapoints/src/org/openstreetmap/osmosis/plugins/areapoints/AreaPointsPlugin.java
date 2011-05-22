package org.openstreetmap.osmosis.plugins.areapoints;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * The AreaPointsPlugin registers a new type of task factory: AreaPointsTaskFactory
 *  to be used when '--areapoints' appears in command line args
 */
public class AreaPointsPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		AreaPointsTaskFactory aptf = new AreaPointsTaskFactory();

		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();

		tasks.put("areapoints", aptf);
		tasks.put("a2p", aptf);
		return tasks;
	}

}
