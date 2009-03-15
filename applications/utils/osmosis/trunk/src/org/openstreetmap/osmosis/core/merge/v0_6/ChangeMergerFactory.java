// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.merge.v0_6;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.MultiChangeSinkRunnableChangeSourceManager;


/**
 * The task manager factory for a change merger.
 * 
 * @author Brett Henderson
 */
public class ChangeMergerFactory extends TaskManagerFactory {
	private static final String ARG_CONFLICT_RESOLUTION_METHOD = "conflictResolutionMethod";
	private static final String DEFAULT_CONFLICT_RESOLUTION_METHOD = "version";
	private static final String ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1 = "timestamp";
	private static final String ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_2 = "lastSource";
	private static final Map<String, ConflictResolutionMethod> CONFLICT_RESOLUTION_METHOD_MAP =
		new HashMap<String, ConflictResolutionMethod>();
	
	static {
		CONFLICT_RESOLUTION_METHOD_MAP.put(
				DEFAULT_CONFLICT_RESOLUTION_METHOD, ConflictResolutionMethod.Version);
		CONFLICT_RESOLUTION_METHOD_MAP.put(
				ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_1, ConflictResolutionMethod.Timestamp);
		CONFLICT_RESOLUTION_METHOD_MAP.put(
				ALTERNATIVE_CONFLICT_RESOLUTION_METHOD_2, ConflictResolutionMethod.LatestSource);
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
					"Argument " + ARG_CONFLICT_RESOLUTION_METHOD + " for task " + taskConfig.getId() +
					" has value \"" + conflictResolutionMethod + "\" which is unrecognised.");
		}
		
		return new MultiChangeSinkRunnableChangeSourceManager(
			taskConfig.getId(),
			new ChangeMerger(CONFLICT_RESOLUTION_METHOD_MAP.get(conflictResolutionMethod), 10),
			taskConfig.getPipeArgs()
		);
	}
}
