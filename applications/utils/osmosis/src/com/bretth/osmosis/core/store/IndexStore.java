// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package com.bretth.osmosis.core.store;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.bretth.osmosis.core.OsmosisRuntimeException;
import com.bretth.osmosis.core.sort.common.FileBasedSort;


/**
 * Writes data into an index file and sorts it if input data is unordered. The
 * data must be fixed width to allow index values to be randomly accessed later.
 * 
 * @param <K>
 *            The index key type.
 * @param <T>
 *            The index element type to be stored.
 * @author Brett Henderson
 */
public class IndexStore<K, T extends IndexElement<K>> implements Releasable {
	static final Logger log = Logger.getLogger(IndexStore.class.getName());
	
	private Lock completeLock;
	private ObjectSerializationFactory serializationFactory;
	private RandomAccessObjectStore<T> indexStore;
	private Comparator<K> ordering;
	private String tempFilePrefix;
	private File indexFile;
	private K previousKey;
	private boolean sorted;
	private long elementCount;
	private long elementSize;
	private boolean complete;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param elementType
	 *            The type of index element to be stored in the index.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 * @param indexFile
	 *            The file to use for storing the index.
	 */
	public IndexStore(Class<T> elementType, Comparator<K> ordering, File indexFile) {
		this.ordering = ordering;
		this.indexFile = indexFile;
		
		completeLock = new ReentrantLock();
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
		
		sorted = true;
		elementCount = 0;
		elementSize = -1;
		complete = false;
	}
	
	
	/**
	 * Creates a new instance.
	 * 
	 * 
	 * @param elementType
	 *            The type of index element to be stored in the index.
	 * @param ordering
	 *            A comparator that sorts index elements desired index key
	 *            ordering.
	 * @param tempFilePrefix
	 *            The prefix of the temporary file.
	 */
	public IndexStore(Class<T> elementType, Comparator<K> ordering, String tempFilePrefix) {
		this.ordering = ordering;
		this.tempFilePrefix = tempFilePrefix;
		
		completeLock = new ReentrantLock();
		
		serializationFactory = new SingleClassObjectSerializationFactory(elementType);
		
		indexStore = new RandomAccessObjectStore<T>(serializationFactory, tempFilePrefix);
		
		sorted = true;
		elementCount = 0;
		elementSize = -1;
		complete = false;
	}
	
	
	/**
	 * Writes the specified element to the index.
	 * 
	 * @param element
	 *            The index element which includes the identifier when stored.
	 */
	public void write(T element) {
		K key;
		long fileOffset;
		
		if (complete) {
			throw new OsmosisRuntimeException("Cannot write new data once reading has begun.");
		}
		
		fileOffset = indexStore.add(element);
		
		key = element.getKey();
		
		// If the new element contains a key that is not sequential, we need to
		// mark the index as unsorted so we can perform a sort prior to reading.
		if (previousKey != null) {
			if (ordering.compare(previousKey, key) > 0) {
				sorted = false;
			}
		}
		previousKey = key;
		
		elementCount++;
		
		// Calculate and verify the element size.
		if (elementCount < 2) {
			// Can't do anything yet.
		} else if (elementCount == 2) {
			elementSize = fileOffset;
		} else {
			long expectedOffset;
			
			expectedOffset = (elementCount - 1) * elementSize;
			
			if (expectedOffset != fileOffset) {
				throw new OsmosisRuntimeException(
					"Inconsistent element sizes, new file offset=" + fileOffset
					+ ", expected offset=" + expectedOffset
					+ ", element size="+ elementSize
					+ ", element count=" + elementCount
				);
			}
		}
	}
	
	
	/**
	 * Creates a new reader capable of accessing the contents of this store. The
	 * reader must be explicitly released when no longer required. Readers must
	 * be released prior to this store.
	 * 
	 * @return A store reader.
	 */
	public IndexStoreReader<K, T> createReader() {
		complete();
		
		return new IndexStoreReader<K, T>(indexStore.createReader(), ordering, elementCount, elementSize);
	}
	
	
	/**
	 * Sorts the file contents if necessary.
	 */
	private void complete() {
		if (!complete) {
			completeLock.lock();
			
			try {
				if (!complete) {
					if (!sorted) {
						final Comparator<K> keyOrdering = ordering;
						
						FileBasedSort<T> fileSort;
						
						// Create a new file based sort instance ordering elements by their
						// identifiers.
						fileSort = new FileBasedSort<T>(
							serializationFactory,
							new Comparator<T>() {
								private Comparator<K> elementKeyOrdering = keyOrdering;
								
								@Override
								public int compare(T o1, T o2) {
									return elementKeyOrdering.compare(o1.getKey(), o2.getKey());
								}
							},
							true
						);
						
						try {
							RandomAccessObjectStoreReader<T> indexStoreReader;
							ReleasableIterator<T> sortIterator;
							
							// Read all data from the index store into the sorting store.
							indexStoreReader = indexStore.createReader();
							try {
								Iterator<T> indexIterator;
								
								indexIterator = indexStoreReader.iterate();
								
								while (indexIterator.hasNext()) {
									fileSort.add(indexIterator.next());
								}
							} finally {
								indexStoreReader.release();
							}
							
							// Release the existing index store and create a new one.
							indexStore.release();
							if (indexFile != null) {
								indexStore = new RandomAccessObjectStore<T>(serializationFactory, indexFile);
							} else {
								indexStore = new RandomAccessObjectStore<T>(serializationFactory, tempFilePrefix);
							}
							
							// Read all data from the sorting store back into the index store.
							sortIterator = fileSort.iterate();
							try {
								while (sortIterator.hasNext()) {
									indexStore.add(sortIterator.next());
								}
							} finally {
								sortIterator.release();
							}
							
						} finally {
							fileSort.release();
						}
					}
					
					complete = true;
				}
				
			} finally {
				completeLock.unlock();
			}
		}

	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		indexStore.release();
	}
}
