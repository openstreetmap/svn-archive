package org.openstreetmap.josm.plugins.openLayers;

import java.net.URL;

import net.sf.ehcache.*;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Currently, this storage uses a ehcache to store objects, but as ehcache is
 * too big, with many jars, it should be replaced for a hand-made storage to
 * disk.
 * 
 * @author frsantos
 * 
 */
public class StorageManager {

    private Cache cache;
    
    private static StorageManager storage;
    
    public static void initStorage(String basedir)
    {
	if( storage != null ) storage.dispose();
	
	storage = new StorageManager(basedir);
    }
    
    protected StorageManager(String basedir)
    {
	System.setProperty("net.sf.ehcache.enableShutdownHook", "true"); 
	cache = new Cache("OpenLayers", 500, MemoryStoreEvictionPolicy.LRU, true, basedir + "cache", false, 300*24*7, 300, true, 3600*24*7, null);
	CacheManager.getInstance().addCache(cache);
    }
    
    protected void dispose()
    {
	if( cache != null )
	    cache.dispose();
    }
    
    public static StorageManager getInstance()
    {
	return storage;
    }
    
    public HttpResponse get(URL key)
    {
	Element element = cache.get(key);
	if( element != null )
	    return (HttpResponse)element.getObjectValue();
	
	return null;
    }
    
    public void put(URL key, HttpResponse value)
    {
	Element element = new Element(key, value);
	cache.put(element);
    }

    /**
     * Flushes old data to disk
     */
    public static void flush() {
	if( storage != null )
	    storage.cache.flush();
    }
}
