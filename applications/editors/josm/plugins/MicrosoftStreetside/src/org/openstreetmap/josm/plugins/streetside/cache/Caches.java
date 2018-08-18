// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.ImageIcon;

import org.apache.commons.jcs.access.CacheAccess;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;
import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.data.cache.JCSCacheManager;
import org.openstreetmap.josm.plugins.streetside.model.UserProfile;
import org.openstreetmap.josm.tools.Logging;

public final class Caches {

  final static Logger logger = Logger.getLogger(Caches.class);

	private Caches() {
		// Private constructor to avoid instantiation
	}

	public static File getCacheDirectory() {
		final File f = new File(Preferences.main().getPluginsDirectory().getPath() + "/MicrosoftStreetside/cache");
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	public abstract static class CacheProxy<K, V extends Serializable> {
		private final CacheAccess<K, V> cache;

		public CacheProxy() {
			CacheAccess<K, V> c;
			try {
				c = createNewCache();
			} catch (IOException e) {
				logger.warn("Could not initialize cache for " + getClass().getName(), e);
				c = null;
			}
			cache = c;
		}

		protected abstract CacheAccess<K, V> createNewCache() throws IOException;

		public V get(final K key) {
			return cache == null ? null : cache.get(key);
		}

		public void put(final K key, final V value) {
			if (cache != null) {
				cache.put(key, value);
			}
		}
	}

	public static class ImageCache {
		private static ImageCache instance;
		private final CacheAccess<String, BufferedImageCacheEntry> cache;

		public ImageCache() {
			CacheAccess<String, BufferedImageCacheEntry> c;
			try {
				c = JCSCacheManager.getCache("streetside", 10, 10000, Caches.getCacheDirectory().getPath());
			} catch (Exception e) {
				Logging.log(Logging.LEVEL_WARN, "Could not initialize the Streetside image cache.", e);
				c = null;
			}
			cache = c;
		}

		public CacheAccess<String, BufferedImageCacheEntry> getCache() {
			return cache;
		}

		public static ImageCache getInstance() {
			synchronized (ImageCache.class) {
				if (ImageCache.instance == null) {
					ImageCache.instance = new ImageCache();
				}
				return ImageCache.instance;
			}
		}
	}

	public static class CubemapCache {
		private static CubemapCache instance;
		private final CacheAccess<String, BufferedImageCacheEntry> cache;

		public CubemapCache() {
			CacheAccess<String, BufferedImageCacheEntry> c;
			try {
				c = JCSCacheManager.getCache("streetside", 10, 10000, Caches.getCacheDirectory().getPath());
			} catch (Exception e) {
				logger.warn("Could not initialize the Streetside cubemap cache.", e);
				c = null;
			}
			cache = c;
		}

		public CacheAccess<String, BufferedImageCacheEntry> getCache() {
			return cache;
		}

		public static CubemapCache getInstance() {
			synchronized (CubemapCache.class) {
				if (CubemapCache.instance == null) {
					CubemapCache.instance = new CubemapCache();
				}
				return CubemapCache.instance;
			}
		}
	}

	public static class MapObjectIconCache extends CacheProxy<String, ImageIcon> {
		private static CacheProxy<String, ImageIcon> instance;

		public static CacheProxy<String, ImageIcon> getInstance() {
			synchronized (MapObjectIconCache.class) {
				if (MapObjectIconCache.instance == null) {
					MapObjectIconCache.instance = new MapObjectIconCache();
				}
				return MapObjectIconCache.instance;
			}
		}

		@Override
		protected CacheAccess<String, ImageIcon> createNewCache() throws IOException {
			return JCSCacheManager.getCache("streetsideObjectIcons", 100, 1000, Caches.getCacheDirectory().getPath());
		}
	}

	public static class UserProfileCache extends CacheProxy<String, UserProfile> {
		private static CacheProxy<String, UserProfile> instance;

		public static CacheProxy<String, UserProfile> getInstance() {
			synchronized (UserProfileCache.class) {
				if (UserProfileCache.instance == null) {
					UserProfileCache.instance = new UserProfileCache();
				}
				return UserProfileCache.instance;
			}
		}

		@Override
		protected CacheAccess<String, UserProfile> createNewCache() throws IOException {
			CacheAccess<String, UserProfile> cache =
					JCSCacheManager.getCache("userProfile", 100, 1000, Caches.getCacheDirectory().getPath());
			IElementAttributes atts = cache.getDefaultElementAttributes();
			atts.setMaxLife(604_800_000); // Sets lifetime to 7 days (604800000=1000*60*60*24*7)
			cache.setDefaultElementAttributes(atts);
			return cache;
		}
	}
}
