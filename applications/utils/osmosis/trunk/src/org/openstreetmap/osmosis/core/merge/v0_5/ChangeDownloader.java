// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.merge.v0_5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.merge.common.ConflictResolutionMethod;
import org.openstreetmap.osmosis.core.merge.v0_5.impl.ChangesetFileNameFormatter;
import org.openstreetmap.osmosis.core.merge.v0_5.impl.DownloaderConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskRunner;
import org.openstreetmap.osmosis.core.task.v0_5.ChangeSink;
import org.openstreetmap.osmosis.core.task.v0_5.RunnableChangeSource;
import org.openstreetmap.osmosis.core.util.FileBasedLock;
import org.openstreetmap.osmosis.core.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.core.xml.common.DateParser;
import org.openstreetmap.osmosis.core.xml.v0_5.XmlChangeReader;
import org.openstreetmap.osmosis.extract.mysql.common.TimestampTracker;


/**
 * Downloads a set of change files from a HTTP server, and merges them into a
 * single output stream. It tracks the intervals covered by the current files
 * and stores the current timestamp between invocations forming the basis of a
 * replication mechanism.
 * 
 * @author Brett Henderson
 */
public class ChangeDownloader implements RunnableChangeSource {
	
	private static final Logger LOG = Logger.getLogger(ChangeDownloader.class.getName());
	
	
	private static final String LOCK_FILE = "download.lock";
	private static final String CONFIG_FILE = "configuration.txt";
	private static final String TSTAMP_FILE = "timestamp.txt";
	private static final String TSTAMP_NEW_FILE = "timestamp-new.txt";
	private static final String SERVER_TSTAMP_FILE = "timestamp.txt";
	
	
	private ChangeSink changeSink;
	private String taskId;
	private File workingDirectory;
	private DateParser dateParser;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param taskId
	 *            The identifier for the task, this is required because the
	 *            names of threads created by this task will use this name as a
	 *            prefix.
	 * @param workingDirectory
	 *            The directory containing configuration and tracking files.
	 */
	public ChangeDownloader(String taskId, File workingDirectory) {
		this.taskId = taskId;
		this.workingDirectory = workingDirectory;
		
		dateParser = new DateParser();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChangeSink(ChangeSink changeSink) {
		this.changeSink = changeSink;
	}
	
	
	/**
	 * Retrieves the latest timestamp from the server.
	 * 
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @return The timestamp.
	 */
	private Date getServerTimestamp(URL baseUrl) {
		URL timestampUrl;
		InputStream timestampStream = null;
		
		try {
			timestampUrl = new URL(baseUrl, SERVER_TSTAMP_FILE);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server timestamp URL could not be created.", e);
		}
		
		try {
			BufferedReader reader;
			Date result;
			
			timestampStream = timestampUrl.openStream();
			
			reader = new BufferedReader(new InputStreamReader(timestampStream));
			
			result = dateParser.parse(reader.readLine());
			
			timestampStream.close();
			timestampStream = null;
			
			return result;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the timestamp from the server.", e);
		} finally {
			try {
				if (timestampStream != null) {
					timestampStream.close();
				}
			} catch (IOException e) {
				// Do nothing.
			}
		}
	}
	
	
	/**
	 * Downloads the file from the server with the specified name and writes it
	 * to a local temporary file.
	 * 
	 * @param fileName
	 *            The name of the file to download.
	 * @param baseUrl
	 *            The url of the directory containing change files.
	 * @return The temporary file containing the downloaded data.
	 */
	private File downloadChangesetFile(String fileName, URL baseUrl) {
		URL changesetUrl;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			changesetUrl = new URL(baseUrl, fileName);
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException("The server file URL could not be created.", e);
		}
		
		try {
			BufferedInputStream source;
			BufferedOutputStream sink;
			File outputFile;
			byte[] buffer;
			
			// Open an input stream for the changeset file on the server.
			inputStream = changesetUrl.openStream();
			source = new BufferedInputStream(inputStream, 65536);
			
			// Create a temporary file to write the data to.
			outputFile = File.createTempFile("change", null);
			
			// Open a output stream for the destination file.
			outputStream = new FileOutputStream(outputFile);
			sink = new BufferedOutputStream(outputStream, 65536);
			
			// Download the file.
			buffer = new byte[65536];
			for (int bytesRead = source.read(buffer); bytesRead > 0; bytesRead = source.read(buffer)) {
				sink.write(buffer, 0, bytesRead);
			}
			sink.flush();
			
			// Clean up all file handles.
			inputStream.close();
			inputStream = null;
			outputStream.close();
			outputStream = null;
			
			return outputFile;
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to read the changeset file " + fileName + " from the server.", e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				// Do nothing.
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				// Do nothing.
			}
		}
	}
	
	
	/**
	 * Downloads the changeset files from the server and writes their contents
	 * to the output task.
	 */
	private void download() {
		DownloaderConfiguration configuration;
		TimestampTracker timestampTracker;
		ChangesetFileNameFormatter fileNameFormatter;
		Date currentTime;
		Date maximumTime;
		URL baseUrl;
		int maxDownloadCount;
		int downloadCount;
		ArrayList<File> tmpFileList;
		ArrayList<RunnableChangeSource> tasks;
		ArrayList<TaskRunner> taskRunners;
		boolean tasksSuccessful;
		
		// Instantiate utility objects.
		configuration = new DownloaderConfiguration(new File(workingDirectory, CONFIG_FILE));
		timestampTracker = new TimestampTracker(
			new File(workingDirectory, TSTAMP_FILE),
			new File(workingDirectory, TSTAMP_NEW_FILE)
		);
		fileNameFormatter = new ChangesetFileNameFormatter(
			configuration.getChangeFileBeginFormat(),
			configuration.getChangeFileEndFormat()
		);
		
		// Create the base url.
		try {
			baseUrl = new URL(configuration.getBaseUrl());
		} catch (MalformedURLException e) {
			throw new OsmosisRuntimeException(
					"Unable to convert URL string (" + configuration.getBaseUrl() + ") into a URL.", e);
		}
		
		tmpFileList = new ArrayList<File>();
		
		// Load the current time from the timestamp tracking file.
		currentTime = timestampTracker.getTime();
		
		// Load the latest timestamp from the server.
		maximumTime = getServerTimestamp(baseUrl);
		
		// Process until all files have been retrieved from the server.
		maxDownloadCount = configuration.getMaxDownloadCount();
		downloadCount = 0;
		while ((maxDownloadCount == 0 || downloadCount < maxDownloadCount) && currentTime.before(maximumTime)) {
			Date nextTime;
			String downloadFileName;
			
			// Calculate the end of the next time interval.
			nextTime = new Date(currentTime.getTime() + configuration.getIntervalLength());
			
			// Generate the filename to be retrieved from the server.
			downloadFileName = fileNameFormatter.generateFileName(currentTime, nextTime);
			
			// Download the changeset from the server.
			tmpFileList.add(downloadChangesetFile(downloadFileName, baseUrl));
			
			// Move the current time to the next interval.
			currentTime = nextTime;
			
			// Increment the current download count.
			downloadCount++;
		}
		
		// Generate a set of tasks for loading the change files and merge them
		// into a single change stream.
		tasks = new ArrayList<RunnableChangeSource>();
		for (File tmpFile : tmpFileList) {
			XmlChangeReader changeReader;
			
			// Generate a change reader task for the current task.
			changeReader = new XmlChangeReader(
				tmpFile,
				true,
				CompressionMethod.GZip
			);
			
			// If tasks already exist, a change merge task must be used to merge
			// existing output with this task output, otherwise this task can be
			// added to the list directly.
			if (tasks.size() > 0) {
				ChangeMerger changeMerger;
				
				// Create a new change merger merging the last task output with the current task.
				changeMerger = new ChangeMerger(ConflictResolutionMethod.LatestSource, 10);
				
				// Connect the inputs of this merger to the most recent change
				// output and the new change output.
				tasks.get(tasks.size() - 1).setChangeSink(changeMerger.getChangeSink(0));
				changeReader.setChangeSink(changeMerger.getChangeSink(1));
				
				tasks.add(changeReader);
				tasks.add(changeMerger);
				
			} else {
				tasks.add(changeReader);
			}
		}
		
		// We only need to execute sub-threads if tasks exist, otherwise we must
		// notify the sink that we have completed.
		if (tasks.size() > 0) {
			// Connect the last task to the change sink.
			tasks.get(tasks.size() - 1).setChangeSink(changeSink);
			
			// Create task runners for each of the tasks to provide thread
			// management.
			taskRunners = new ArrayList<TaskRunner>(tasks.size());
			for (int i = 0; i < tasks.size(); i++) {
				taskRunners.add(new TaskRunner(tasks.get(i), "Thread-" + taskId + "-worker" + i));
			}
			
			// Launch all of the tasks.
			for (int i = 0; i < taskRunners.size(); i++) {
				TaskRunner taskRunner;
				
				taskRunner = taskRunners.get(i);
				
				LOG.fine("Launching changeset worker + " + i + " in a new thread.");
				
				taskRunner.start();
			}
			
			// Wait for all the tasks to complete.
			tasksSuccessful = true;
			for (int i = 0; i < taskRunners.size(); i++) {
				TaskRunner taskRunner;
				
				taskRunner = taskRunners.get(i);
				
				LOG.fine("Waiting for changeset worker " + i + " to complete.");
				
				try {
					taskRunner.join();
				} catch (InterruptedException e) {
					// Do nothing.
				}
				
				if (!taskRunner.isSuccessful()) {
					LOG.log(Level.SEVERE, "Changeset worker " + i + " failed", taskRunner.getException());
					
					tasksSuccessful = false;
				}
			}
			
		} else {
			changeSink.complete();
			tasksSuccessful = true;
		}
		
		// Remove the temporary files.
		for (File tmpFile : tmpFileList) {
			if (!tmpFile.delete()) {
				LOG.warning("Unable to delete file " + tmpFile.getName());
			}
		}
		
		if (!tasksSuccessful) {
			throw new OsmosisRuntimeException("One or more changeset workers failed.");
		}
		
		// Update the timestamp tracker.
		timestampTracker.setTime(currentTime);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		FileBasedLock fileLock;
		
		fileLock = new FileBasedLock(new File(workingDirectory, LOCK_FILE));
		
		try {
			fileLock.lock();
			
			download();
			
			fileLock.unlock();
			
		} finally {
			changeSink.release();
			fileLock.release();
		}
	}
}
