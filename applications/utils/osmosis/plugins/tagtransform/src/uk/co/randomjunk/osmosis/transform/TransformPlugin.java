// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


public class TransformPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		uk.co.randomjunk.osmosis.transform.v0_5.TransformTaskFactory v0_5 = new uk.co.randomjunk.osmosis.transform.v0_5.TransformTaskFactory();
		uk.co.randomjunk.osmosis.transform.v0_6.TransformTaskFactory v0_6 =
			new uk.co.randomjunk.osmosis.transform.v0_6.TransformTaskFactory();
		
		uk.co.randomjunk.osmosis.transform.v0_6.TransformChangeTaskFactory change_v0_6 =
			new uk.co.randomjunk.osmosis.transform.v0_6.TransformChangeTaskFactory();
		
		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();
		tasks.put("tag-transform-0.5", v0_5);
		tasks.put("tag-transform-0.6", v0_6);
		tasks.put("tag-transform", v0_6);
		tasks.put("tt", v0_6);
		tasks.put("tag-transform-change-0.6", change_v0_6);
		tasks.put("tag-transform-change", change_v0_6);
		tasks.put("ttc", change_v0_6);
		return tasks;
	}

}
