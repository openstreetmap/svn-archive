// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.merge.v0_6.ChangeAppenderFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ChangeMergerFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.ChangeSimplifierFactory;
import org.openstreetmap.osmosis.core.merge.v0_6.EntityMergerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;


/**
 * The plugin loader for the Set manipulation tasks.
 * 
 * @author Brett Henderson
 */
public class SetPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("merge", new EntityMergerFactory());
		factoryMap.put("m", new EntityMergerFactory());
		factoryMap.put("merge-change", new ChangeMergerFactory());
		factoryMap.put("mc", new ChangeMergerFactory());
		factoryMap.put("append-change", new ChangeAppenderFactory());
		factoryMap.put("apc", new ChangeAppenderFactory());
		factoryMap.put("simplify-change", new ChangeSimplifierFactory());
		factoryMap.put("simc", new ChangeSimplifierFactory());
		
		factoryMap.put("merge-0.6", new EntityMergerFactory());
		factoryMap.put("merge-change-0.6", new ChangeMergerFactory());
		factoryMap.put("append-change-0.6", new ChangeAppenderFactory());
		factoryMap.put("simplify-change-0.6", new ChangeSimplifierFactory());
		
		return factoryMap;
	}
}
