// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.merge.v0_5;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_5.MultiSinkRunnableSourceManager;


/**
 * The task manager factory for an entity merger.
 * 
 * @author Brett Henderson
 */
public class EntityMergerFactory extends TaskManagerFactory {
	private static final String ARG_CONFLICT_RESOLUTION_METHOD = "conflictResolutionMethod";
	private static final String DEFAULT_CONFLICT_RESOLUTION_METHOD = "timestamp";
	private static final String ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1 = "lastSource";
	private static final Map<String, ConflictResolutionMethod> CONFLICT_RESOLUTION_METHOD_MAP =
		new HashMap<String, ConflictResolutionMethod>();
	
	static {
		CONFLICT_RESOLUTION_METHOD_MAP.put(
				DEFAULT_CONFLICT_RESOLUTION_METHOD, ConflictResolutionMethod.Timestamp);
		CONFLICT_RESOLUTION_METHOD_MAP.put(
				ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1, ConflictResolutionMethod.LatestSource);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String conflictResolutionMethod;
		
		conflictResolutionMethod = getStringArgument(
				taskConfig, ARG_CONFLICT_RESOLUTION_METHOD, DEFAULT_CONFLICT_RESOLUTION_METHOD);
		
		if (!CONFLICT_RESOLUTION_METHOD_MAP.containsKey(conflictResolutionMethod)) {
			throw new OsmosisRuntimeException(
					"Argument " + ARG_CONFLICT_RESOLUTION_METHOD + " for task " + taskConfig.getId()
					+ " has value \"" + conflictResolutionMethod + "\" which is unrecognised.");
		}
		
		return new MultiSinkRunnableSourceManager(
			taskConfig.getId(),
			new EntityMerger(CONFLICT_RESOLUTION_METHOD_MAP.get(conflictResolutionMethod), 10),
			taskConfig.getPipeArgs()
		);
	}
}
