import org.openstreetmap.osmosis.core.filter.common.IdTrackerType;
import org.openstreetmap.osmosis.core.filter.v0_6.AreaFilterTaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkSourceManager;
import org.openstreetmap.osmosis.core.task.v0_6.SinkSource;

/**
 * Factory creates a SinkSource type of task: SimplifyTask
 * SimplifyTask is where all the magic happens
 * Its constructor needs an idTrackerType, hence extending AreaFilterTaskManagerFactory (bit odd)
 */
public class SimplifyTaskFactory extends AreaFilterTaskManagerFactory {

	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		
		IdTrackerType idTrackerType = super.getIdTrackerType(taskConfig);
		
		SinkSource ss = new SimplifyTask(idTrackerType);
		
		return new SinkSourceManager(taskConfig.getId(),
				ss,
				taskConfig.getPipeArgs());
		
	}
	
}
