/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.shapefile;

import static org.geotools.data.shapefile.ShpFileType.SHP;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.geotools.data.DataUtilities;

/**
 * The collection of all the files that are the shapefile and its metadata and
 * indices.
 * 
 * <p>
 * This class has methods for performing actions on the files. Currently mainly
 * for obtaining read and write channels and streams. But in the future a move
 * method may be introduced.
 * </p>
 * 
 * <p>
 * Note: The method that require locks (such as getInputStream()) will
 * automatically acquire locks and the javadocs should document how to release
 * the lock. Therefore the methods {@link #acquireRead(ShpFileType, FileReader)}
 * and {@link #acquireWrite(ShpFileType, FileWriter)}svn
 * </p>
 * 
 * @author jesse
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/ShpFiles.java $
 */
public class ShpFiles {
   
    /**
     * The urls for each type of file that is associated with the shapefile. The
     * key is the type of file
     */
    protected final Map<ShpFileType, URL> urls = new ConcurrentHashMap<ShpFileType, URL>();

    /**
     * A read/write lock, so that we can have concurrent readers 
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * The set of locker sources per thread. Used as a debugging aid and to upgrade/downgrade
     * the locks
     */
    private final Map<Thread, Collection<ShpFilesLocker>> lockers = new ConcurrentHashMap<Thread, Collection<ShpFilesLocker>>();

    /**
     * A cache for read only memory mapped buffers
     */
    private final MemoryMapCache mapCache = new MemoryMapCache();
    
    private boolean memoryMapCacheEnabled;

    /**
     * Searches for all the files and adds then to the map of files.
     * 
     * @param file
     *                any one of the shapefile files
     * 
     */
    public ShpFiles(URL url) throws IllegalArgumentException {
        init(url);
    }

    private void init(URL url) {
        String base = baseName(url);
        if (base == null) {
            throw new IllegalArgumentException(
                    url.getPath()
                            + " is not one of the files types that is known to be associated with a shapefile");
        }

        String urlString = url.toExternalForm();
        char lastChar = urlString.charAt(urlString.length()-1);
        boolean upperCase = Character.isUpperCase(lastChar);

        for (ShpFileType type : ShpFileType.values()) {
            
            String extensionWithPeriod = type.extensionWithPeriod;
            if( upperCase ){
                extensionWithPeriod = extensionWithPeriod.toUpperCase();
            }else{
                extensionWithPeriod = extensionWithPeriod.toLowerCase();
            }
            
            URL newURL;
            String string = base + extensionWithPeriod;
            try {
                newURL = new URL(url, string);
            } catch (MalformedURLException e) {
                // shouldn't happen because the starting url was constructable
                throw new RuntimeException(e);
            }
            urls.put(type, newURL);
        }

        // if the files are local check each file to see if it exists
        // if not then search for a file of the same name but try all combinations of the 
        // different cases that the extension can be made up of.
        // IE Shp, SHP, Shp, ShP etc...
        if( isLocal() ){
            Set<Entry<ShpFileType, URL>> entries = urls.entrySet();
            Map<ShpFileType, URL> toUpdate = new HashMap<ShpFileType, URL>();
            for (Entry<ShpFileType, URL> entry : entries) {
                if( !exists(entry.getKey()) ){
                    url = findExistingFile(entry.getKey(), entry.getValue());
                    if( url!=null ){
                        toUpdate.put(entry.getKey(), url);
                    }
                }
            }
            
            urls.putAll(toUpdate);
            
        }
        
    }

    private URL findExistingFile(ShpFileType shpFileType, URL value) {
        final File file = DataUtilities.urlToFile(value);
        File directory = file.getParentFile();
        if( directory==null || !directory.exists() ) {
            // doesn't exist
            return null;
        }
        File[] files = directory.listFiles(new FilenameFilter(){

            public boolean accept(File dir, String name) {
                return file.getName().equalsIgnoreCase(name);
            }
            
        });
        if( files.length>0 ){
            try {
                return files[0].toURI().toURL();
            } catch (MalformedURLException e) {
                ShapefileDataStoreFactory.LOGGER.log(Level.SEVERE, "", e);
            }
        }
        return null;
    }

    /**
     * This verifies that this class has been closed correctly (nothing locking)
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    public void dispose() {
        if (numberOfLocks() != 0) {
            logCurrentLockers(Level.SEVERE);
            lockers.clear(); // so as not to get this log again.
        }
        mapCache.clean();
    }

    /**
     * Writes to the log all the lockers and when they were constructed.
     * 
     * @param logLevel
     *                the level at which to log.
     */
    public void logCurrentLockers(Level logLevel) {
        for (Collection<ShpFilesLocker> lockerList : lockers.values()) {
            for (ShpFilesLocker locker : lockerList) {
                StringBuilder sb = new StringBuilder("The following locker still has a lock: ");
                sb.append(locker);
                ShapefileDataStoreFactory.LOGGER.log(logLevel, sb.toString());
            }
        }
    }

    protected String baseName(Object obj) {
        for (ShpFileType type : ShpFileType.values()) {
            String base = null;
            if (obj instanceof File) {
                File file = (File) obj;
                base = type.toBase(file);
            }
            if (obj instanceof URL) {
                URL file = (URL) obj;
                base = type.toBase(file);
            }
            if (base != null) {
                return base;
            }
        }
        return null;
    }

    /**
     * Returns the string form of the url that identifies the file indicated by
     * the type parameter or null if it is known that the file does not exist.
     * 
     * <p>
     * Note: a URL should NOT be constructed from the string instead the URL
     * should be obtained through calling one of the aquireLock methods.
     * 
     * @param type
     *                indicates the type of file the caller is interested in.
     * 
     * @return the string form of the url that identifies the file indicated by
     *         the type parameter or null if it is known that the file does not
     *         exist.
     */
    public String get(ShpFileType type) {
        return urls.get(type).toExternalForm();
    }

    /**
     * Returns the number of locks on the current set of shapefile files. This
     * is not thread safe so do not count on it to have a completely accurate
     * picture but it can be useful debugging
     * 
     * @return the number of locks on the current set of shapefile files.
     */
    public int numberOfLocks() {
        int count = 0;
        for (Collection<ShpFilesLocker> lockerList : lockers.values()) {
            count += lockerList.size();
        }
        return count;
    }
    
    /**
     * Acquire a URL for read only purposes.  It is recommended that get*Stream or
     * get*Channel methods are used when reading or writing to the file is
     * desired.
     * 
     * 
     * @see #getInputStream(ShpFileType, FileReader)
     * @see #getReadChannel(ShpFileType, FileReader)
     * @see #getWriteChannel(ShpFileType, FileReader)
     * 
     * @param type
     *                the type of the file desired.
     * @param requestor
     *                the object that is requesting the URL. The same object
     *                must release the lock and is also used for debugging.
     * @return the URL to the file of the type requested
     */
    public URL acquireRead(ShpFileType type, FileReader requestor) {
        URL url = urls.get(type);
        if (url == null)
            return null;
        
        readWriteLock.readLock().lock();
        Collection<ShpFilesLocker> threadLockers = getCurrentThreadLockers();
        threadLockers.add(new ShpFilesLocker(url, requestor));
        return url;
    }

    /**
     * Unlocks a read lock. The url and requestor must be the the same as the
     * one of the lockers.
     * 
     * @param url
     *                url that was locked
     * @param requestor
     *                the class that requested the url
     */
    public void unlockRead(URL url, FileReader requestor) {
        if (url == null) {
            throw new NullPointerException("url cannot be null");
        }
        if (requestor == null) {
            throw new NullPointerException("requestor cannot be null");
        }

        Collection threadLockers = getCurrentThreadLockers();
        boolean removed = threadLockers.remove(new ShpFilesLocker(url, requestor));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Expected requestor "
                            + requestor
                            + " to have locked the url but it does not hold the lock for the URL");
        }
        if(threadLockers.size() == 0)
            lockers.remove(Thread.currentThread());
        readWriteLock.readLock().unlock();
    }

    /**
     * Unlocks a read lock. The requestor must be have previously obtained a
     * lock for the url.
     * 
     * 
     * @param url
     *                url that was locked
     * @param requestor
     *                the class that requested the url
     */
    public void unlockWrite(URL url, FileWriter requestor) {
        if (url == null) {
            throw new NullPointerException("url cannot be null");
        }
        if (requestor == null) {
            throw new NullPointerException("requestor cannot be null");
        }
        Collection<ShpFilesLocker> threadLockers = getCurrentThreadLockers();
        boolean removed = threadLockers.remove(new ShpFilesLocker(url, requestor));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Expected requestor "
                            + requestor
                            + " to have locked the url but it does not hold the lock for the URL");
        }
        
        if(threadLockers.size() == 0) {
            lockers.remove(Thread.currentThread());
        } else {
            // get back read locks before giving up the write one
            regainReadLocks(threadLockers);
        }
        readWriteLock.writeLock().unlock();
    }
   
    /**
     * Returns the list of lockers attached to a given thread, or creates it if missing
     * @return
     */
    private Collection<ShpFilesLocker> getCurrentThreadLockers() {
        Collection<ShpFilesLocker> threadLockers = lockers.get(Thread.currentThread());
        if(threadLockers == null) {
            threadLockers = new ArrayList<ShpFilesLocker>();
            lockers.put(Thread.currentThread(), threadLockers);
        }
        return threadLockers;
    }
    
    /**
     * Re-takes the read locks in preparation for lock downgrade
     * @param threadLockers
     */
    private void regainReadLocks(Collection<ShpFilesLocker> threadLockers) {
        for (ShpFilesLocker shpFilesLocker : threadLockers) {
            if(shpFilesLocker.reader != null && shpFilesLocker.upgraded) {
                readWriteLock.readLock().lock();
                shpFilesLocker.upgraded = false;
            }
        }
    }

    /**
     * Determine if the location of this shapefile is local or remote.
     * 
     * @return true if local, false if remote
     */
    public boolean isLocal() {
        return urls.get(ShpFileType.SHP).toExternalForm().toLowerCase()
                .startsWith("file:");
    }

    /**
     * Opens a input stream for the indicated file. A read lock is requested at
     * the method call and released on close.
     * 
     * @param type
     *                the type of file to open the stream to.
     * @param requestor
     *                the object requesting the stream
     * @return an input stream
     * 
     * @throws IOException
     *                 if a problem occurred opening the stream.
     */
    public InputStream getInputStream(ShpFileType type,
            final FileReader requestor) throws IOException {
        final URL url = acquireRead(type, requestor);

        try {
            FilterInputStream input = new FilterInputStream(url.openStream()) {

                private volatile boolean closed = false;

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        if (!closed) {
                            closed = true;
                            unlockRead(url, requestor);
                        }
                    }
                }

            };
            return input;
        }catch(Throwable e){
            unlockRead(url, requestor);
            if( e instanceof IOException ){
                throw (IOException) e;
            } else if( e instanceof RuntimeException){
                throw (RuntimeException) e;
            } else if( e instanceof Error ){
                throw (Error) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Obtain a ReadableByteChannel from the given URL. If the url protocol is
     * file, a FileChannel will be returned. Otherwise a generic channel will be
     * obtained from the urls input stream.
     * <p>
     * A read lock is obtained when this method is called and released when the
     * channel is closed.
     * </p>
     * 
     * @param type
     *                the type of file to open the channel to.
     * @param requestor
     *                the object requesting the channel
     * 
     */
    public ReadableByteChannel getReadChannel(ShpFileType type,
            FileReader requestor) throws IOException {
        URL url = acquireRead(type, requestor);
        ReadableByteChannel channel = null;
        try {
            if (isLocal()) {

                File file = DataUtilities.urlToFile(url);
                
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                channel = new FileChannelDecorator(raf.getChannel(), this, url,
                        requestor);

            } else {
                InputStream in = url.openConnection().getInputStream();
                channel = new ReadableByteChannelDecorator(Channels
                        .newChannel(in), this, url, requestor);
            }
        } catch (Throwable e) {
            unlockRead(url, requestor);
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return channel;
    }

    public String getTypeName() {
        String path = SHP.toBase(urls.get(SHP));
        int slash = Math.max(0, path.lastIndexOf('/') + 1);
        int dot = path.indexOf('.', slash);

        if (dot < 0) {
            dot = path.length();
        }

        return path.substring(slash, dot);
    }
    
    /**
     * Internal method that the file channel decorators will call to allow reuse of the memory mapped buffers
     * @param wrapped
     * @param url
     * @param mode
     * @param position
     * @param size
     * @return
     * @throws IOException
     */
	MappedByteBuffer map(FileChannel wrapped, URL url, MapMode mode, long position, long size) throws IOException {
		if(memoryMapCacheEnabled) {
			return mapCache.map(wrapped, url, mode, position, size);
		} else {
			return wrapped.map(mode, position, size);
		}
	}
	
	/**
	 * Enables the memory map cache. When enabled the memory mapped portions of the files are cached and shared
	 * (giving each thread a clone of it)
	 * @param memoryMapCacheEnabled
	 */
	public void setMemoryMapCacheEnabled(boolean memoryMapCacheEnabled) {
		this.memoryMapCacheEnabled = memoryMapCacheEnabled;
		if(!memoryMapCacheEnabled) {
			mapCache.clean();
		}
	}

    /**
     * Returns true if the file exists. Throws an exception if the file is not
     * local.
     * 
     * @param fileType
     *                the type of file to check existance for.
     * 
     * @return true if the file exists.
     * 
     * @throws IllegalArgumentException
     *                 if the files are not local.
     */
    public boolean exists(ShpFileType fileType) throws IllegalArgumentException {
        if (!isLocal()) {
            throw new IllegalArgumentException(
                    "This method only makes sense if the files are local");
        }
        URL url = urls.get(fileType);
        if (url == null) {
            return false;
        }

        File file = DataUtilities.urlToFile(url);
        return file.exists();
    }
    
}
