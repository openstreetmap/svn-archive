// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.sort.v0_5;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.container.v0_5.ChangeContainer;
import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.ChangeSinkChangeSourceManager;


/**
 * The task manager factory for a change sorter.
 * 
 * @author Brett Henderson
 */
public class ChangeSorterFactory extends TaskManagerFactory {
	private static final String ARG_COMPARATOR_TYPE = "type";
	
	private Map<String, Comparator<ChangeContainer>> comparatorMap;
	private String defaultComparatorType;
	
	
	/**
	 * Creates a new instance.
	 */
	public ChangeSorterFactory() {
		comparatorMap = new HashMap<String, Comparator<ChangeContainer>>();
	}
	
	
	/**
	 * Registers a new comparator.
	 * 
	 * @param comparatorType
	 *            The name of the comparator.
	 * @param comparator
	 *            The comparator.
	 * @param setAsDefault
	 *            If true, this will be set to be the default comparator if no
	 *            comparator is specified.
	 */
	public void registerComparator(String comparatorType, Comparator<ChangeContainer> comparator, boolean setAsDefault) {
		if (comparatorMap.containsKey(comparatorType)) {
			throw new OsmosisRuntimeException("Comparator type \"" + comparatorType + "\" already exists.");
		}
		
		if (setAsDefault) {
			defaultComparatorType = comparatorType;
		}
		
		comparatorMap.put(comparatorType, comparator);
	}
	
	
	/**
	 * Retrieves the comparator identified by the specified type.
	 * 
	 * @param comparatorType
	 *            The comparator to be retrieved.
	 * @return The comparator.
	 */
	private Comparator<ChangeContainer> getComparator(String comparatorType) {
		if (!comparatorMap.containsKey(comparatorType)) {
			throw new OsmosisRuntimeException("Comparator type " + comparatorType
					+ " doesn't exist.");
		}
		
		return comparatorMap.get(comparatorType);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		Comparator<ChangeContainer> comparator;
		
		// Get the comparator.
		comparator = getComparator(
			getStringArgument(
				taskConfig,
				ARG_COMPARATOR_TYPE,
				getDefaultStringArgument(taskConfig, defaultComparatorType)
			)
		);
		
		return new ChangeSinkChangeSourceManager(
			taskConfig.getId(),
			new ChangeSorter(comparator),
			taskConfig.getPipeArgs()
		);
	}
}
