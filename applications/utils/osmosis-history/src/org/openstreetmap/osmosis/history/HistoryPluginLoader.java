// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.history;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.history.v0_6.PostgreSqlHistoryCopyWriterFactory;
import org.openstreetmap.osmosis.history.v0_6.PostgreSqlHistoryDumpWriterFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * The plugin loader for the history tasks.
 * 
 * @author Peter Koerner
 */
public class HistoryPluginLoader implements PluginLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		Map<String, TaskManagerFactory> factoryMap;
		
		factoryMap = new HashMap<String, TaskManagerFactory>();
		
		factoryMap.put("write-pgsql-history-dump", new PostgreSqlHistoryDumpWriterFactory());
		factoryMap.put("wphd", new PostgreSqlHistoryDumpWriterFactory());
		
		factoryMap.put("write-pgsql-history", new PostgreSqlHistoryCopyWriterFactory());
		factoryMap.put("wph", new PostgreSqlHistoryCopyWriterFactory());
		
		return factoryMap;
	}
}
