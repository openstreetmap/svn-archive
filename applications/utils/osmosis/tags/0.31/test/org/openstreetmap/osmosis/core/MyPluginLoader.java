// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.misc.v0_5.NullChangeWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * A simple plugin loader to validate plugin functionality.
 * 
 * @author Brett Henderson
 */
public class MyPluginLoader implements PluginLoader {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> taskFactories;
		
		taskFactories = new HashMap<String, TaskManagerFactory>();
		
		// Register a task under a new name.  We can use an existing task implementation for simplicity.
		taskFactories.put("my-plugin-task", new NullChangeWriterFactory());
		
		return taskFactories;
	}
}
