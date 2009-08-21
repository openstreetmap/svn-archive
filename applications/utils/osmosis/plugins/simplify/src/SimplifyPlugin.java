import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * The SimplifyPlugin registers a new type of task factory: SimplifyTaskFactory
 *  to be used when '--simplify' appears in command line args
 */
public class SimplifyPlugin implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		SimplifyTaskFactory simplifyTaskFactory = new SimplifyTaskFactory();
		
		Map<String, TaskManagerFactory> tasks = new HashMap<String, TaskManagerFactory>();
		
		tasks.put("simplify", simplifyTaskFactory); 
		return tasks;
	}

}
