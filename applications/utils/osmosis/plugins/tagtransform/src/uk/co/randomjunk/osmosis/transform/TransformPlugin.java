// License: GPL. Copyright 2008 by Dave Stubbs and other contributors.
package uk.co.randomjunk.osmosis.transform;

import java.util.HashMap;
import java.util.Map;

import uk.co.randomjunk.osmosis.transform.v0_5.TransformTaskFactory;

import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.plugin.PluginLoader;

public class TransformPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		TransformTaskFactory v0_5 = new TransformTaskFactory();
		uk.co.randomjunk.osmosis.transform.v0_6.TransformTaskFactory v0_6 =
			new uk.co.randomjunk.osmosis.transform.v0_6.TransformTaskFactory();
		
		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();
		tasks.put("tag-transform-0.5", v0_5);
		tasks.put("tag-transform-0.6", v0_6);
		tasks.put("tag-transform", v0_6);
		tasks.put("tt", v0_6);
		return tasks;
	}

}
