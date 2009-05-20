// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6;

import java.io.File;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.RunnableSourceManager;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml reader.
 * 
 * @author Brett Henderson
 */
public class XmlReaderFactory extends XmlTaskManagerFactory {
	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.osm";
	private static final String ARG_ENABLE_DATE_PARSING = "enableDateParsing";
	private static final boolean DEFAULT_ENABLE_DATE_PARSING = true;
	
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;
		File file;
		boolean enableDateParsing;
		CompressionMethod compressionMethod;
		XmlReader task;
		
		// Get the task arguments.
		fileName = getStringArgument(
			taskConfig,
			ARG_FILE_NAME,
			getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);
		enableDateParsing = getBooleanArgument(taskConfig, ARG_ENABLE_DATE_PARSING, DEFAULT_ENABLE_DATE_PARSING);
		compressionMethod = getCompressionMethodArgument(taskConfig, fileName);
		
		// Create a file object from the file name provided.
		file = new File(fileName);
		
		// Build the task object.
		task = new XmlReader(file, enableDateParsing, compressionMethod);
		
		return new RunnableSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}
}
