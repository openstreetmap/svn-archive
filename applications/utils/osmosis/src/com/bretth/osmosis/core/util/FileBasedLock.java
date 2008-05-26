// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.store.Releasable;


/**
 * This class provides a mechanism to use simple files as locks to prevent
 * multiple threads or processes from updating common files.
 * 
 * @author Brett Henderson
 */
public class FileBasedLock implements Releasable {
	
	private static final Logger log = Logger.getLogger(FileBasedLock.class.getName());
	
	private File lockFile;
	private FileOutputStream outputStream;
	private FileChannel fileChannel;
	private FileLock fileLock;
	private boolean initialized;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param lockFile
	 *            The file to use for locking.
	 */
	public FileBasedLock(File lockFile) {
		this.lockFile = lockFile;
		
		initialized = false;
	}
	
	
	/**
	 * Creates the file resources used internally for implementing the lock.
	 */
	private void initialize() {
		if (!initialized) {
			try {
				outputStream = new FileOutputStream(lockFile);
			} catch (IOException e) {
				throw new OsmosisRuntimeException("Unable to open lock file " + lockFile + ".");
			}
			
			fileChannel = outputStream.getChannel();
		}
	}
	
	
	/**
	 * Obtain an exclusive lock. This will fail if another thread or process
	 * already has a lock.
	 */
	public void lock() {
		initialize();
		
		if (fileLock != null) {
			throw new OsmosisRuntimeException("A lock has already been obtained on file " + lockFile + ".");
		}
		
		try {
			fileLock = fileChannel.tryLock();
			
			if (fileLock == null) {
				throw new OsmosisRuntimeException("A exclusive lock already exists on file " + lockFile + ".");
			}
			
		} catch (IOException e) {
			throw new OsmosisRuntimeException("An error occurred while trying to obtain an exclusive lock on file " + lockFile + ".");
		}
	}
	
	
	/**
	 * Release the lock.
	 */
	public void unlock() {
		initialize();
		
		try {
			fileLock.release();
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to release lock on file " + lockFile + ".");
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Exception e) {
				log.warning("Unable to close lock stream on file " + lockFile + ".");
			} finally {
				outputStream = null;
				fileChannel= null;
				fileLock = null;
				initialized = false;
			}
		}
	}
}
