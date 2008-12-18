// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.xml.v0_5;

import com.bretth.osmosis.core.cli.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.common.TaskManagerFactory;
import com.bretth.osmosis.core.pipeline.v0_5.RunnableSourceManager;
import com.bretth.osmosis.core.xml.v0_5.impl.XmlConstants;


/**
 * The task manager factory for an xml reader
 * that downloads the map instead of reading it from
 * a file..
 *
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class XmlDownloaderFactory extends TaskManagerFactory {
	private static final String ARG_LEFT = "left";
	private static final String ARG_RIGHT = "right";
	private static final String ARG_TOP = "top";
	private static final String ARG_BOTTOM = "bottom";
	private static final String ARG_URL = "url";
	private static final double DEFAULT_LEFT = -180;
	private static final double DEFAULT_RIGHT = 180;
	private static final double DEFAULT_TOP = 90;
	private static final double DEFAULT_BOTTOM = -90;
	
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		double left;
		double right;
		double top;
		double bottom;
		String url;
		
		// Get the task arguments.
		left = getDoubleArgument(taskConfig, ARG_LEFT, DEFAULT_LEFT);
		right = getDoubleArgument(taskConfig, ARG_RIGHT, DEFAULT_RIGHT);
		top = getDoubleArgument(taskConfig, ARG_TOP, DEFAULT_TOP);
		bottom = getDoubleArgument(taskConfig, ARG_BOTTOM, DEFAULT_BOTTOM);
		url = getStringArgument(taskConfig, ARG_URL, XmlConstants.DEFAULT_URL);
        
		// Create and return the task and associated manager.
		return new RunnableSourceManager(
			taskConfig.getId(),
			new XmlDownloader(left, right, top, bottom, url),
			taskConfig.getPipeArgs()
		);
    }
}
