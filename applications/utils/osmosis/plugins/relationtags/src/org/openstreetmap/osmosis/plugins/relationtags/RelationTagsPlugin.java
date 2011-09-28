package org.openstreetmap.osmosis.plugins.relationtags;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

public class RelationTagsPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		RelationTagsTaskFactory rttf = new RelationTagsTaskFactory();

		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();

		tasks.put("set-relation-tags", rttf);
		tasks.put("rt", rttf);
		return tasks;
	}

}
