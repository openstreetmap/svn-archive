package com.bretth.osmosis.core.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import com.bretth.osmosis.core.OsmosisRuntimeException;


/**
 * Provides read-only access to a random access object store. Each thread
 * accessing the object store must create its own reader. The reader maintains
 * all references to heavyweight resources such as file handles used to access
 * the store eliminating the need for objects such as object iterators to be
 * cleaned up explicitly.
 * 
 * @param <T>
 *            The object type being stored.
 * @author Brett Henderson
 */
public class RandomAccessObjectStoreReader<T> implements Releasable {
	private RandomAccessFile randomFile;
	private ObjectReader objectReader;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param randomFile
	 *            A read-only random access file opened on the store file.
	 * @param objectReader
	 *            The reader containing the objects to be deserialized.
	 */
	public RandomAccessObjectStoreReader(RandomAccessFile randomFile, ObjectReader objectReader) {
		this.randomFile = randomFile;
		this.objectReader = objectReader;
	}
	
	
	/**
	 * Seeks to the specified location in the storage file.
	 * 
	 * @param offset
	 *            The file offset to seek to.
	 */
	private void seek(long offset) {
		try {
			randomFile.seek(offset);
		} catch (IOException e) {
			throw new OsmosisRuntimeException("Unable to seek to position " + offset + " in the storage file.");
		}
	}
	
	
	/**
	 * Reads the object at the specified file offset.
	 * 
	 * @param offset
	 *            The file offset to read an object from.
	 * @return The requested object.
	 */
	@SuppressWarnings("unchecked")
	public T get(long offset) {
		seek(offset);
		
		return (T) objectReader.readObject();
	}
	
	
	/**
	 * Iterates over the entire stream of data.
	 * 
	 * @param offset
	 *            The location in the storage file to begin reading.
	 * @return An iterator for reading objects from the data store. This
	 *         iterator must be released after use.
	 */
	public Iterator<T> iterate(long offset) {
		seek(offset);
		
		return new ObjectDataInputIterator<T>(objectReader);
	}
	
	
	/**
	 * Iterates over the entire stream of data.
	 * 
	 * @return An iterator for reading objects from the data store. This
	 *         iterator must be released after use.
	 */
	public Iterator<T> iterate() {
		return iterate(0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		if (randomFile != null) {
			try {
				randomFile.close();
			} catch (Exception e) {
				// Do nothing.
			}
			randomFile = null;
		}
	}
}
