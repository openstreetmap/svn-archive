package com.bretth.osmosis.core.xml.v0_6;

import java.io.File;

import com.bretth.osmosis.core.pipeline.common.TaskConfiguration;
import com.bretth.osmosis.core.pipeline.common.TaskManager;
import com.bretth.osmosis.core.pipeline.v0_6.RunnableSourceManager;
import com.bretth.osmosis.core.xml.common.CompressionMethod;
import com.bretth.osmosis.core.xml.common.XmlTaskManagerFactory;


/**
 * The task manager factory for an xml reader.
 * 
 * @author Brett Henderson
 */
public class FastXmlReaderFactory extends XmlTaskManagerFactory {

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
			FastXmlReader task;
			
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
			task = new FastXmlReader(file, enableDateParsing, compressionMethod);
			
			return new RunnableSourceManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
		}
}
