package org.openstreetmap.osmosis.plugins.mysqlsimple;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


public class MysqlSimpleTaskFactory extends TaskManagerFactory {

	private static Logger log = Logger.getLogger(MysqlSimpleTaskFactory.class.getName());


	private static final String ARG_FILE_NAME = "file";
	private static final String DEFAULT_FILE_NAME = "dump.sql";

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		String fileName;


		// Get the task arguments.
		fileName = getStringArgument(
				taskConfig,
				ARG_FILE_NAME,
				getDefaultStringArgument(taskConfig, DEFAULT_FILE_NAME)
		);

		OutputStream outStream = null;
		BufferedWriter writer = null;
		try {
			OutputStreamWriter outStreamWriter;

			// make "-" an alias for /dev/stdout
			if (fileName.equals("-")) {
				outStream = System.out;
			} else {
				outStream = new FileOutputStream(fileName);
			}
			outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");

			writer = new BufferedWriter(outStreamWriter);

			outStream = null;
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to open file for writing.", e);
		} finally {
			if (outStream != null) {
				try {
					outStream.close();
				} catch (Exception e) {
					log.log(Level.SEVERE, "Unable to close output stream.", e);
				}
				outStream = null;
			}
		}
		Sink sink = new MysqlSimpleTask(writer);

		return new SinkManager(taskConfig.getId(),
				sink,
				taskConfig.getPipeArgs());
	}

}
