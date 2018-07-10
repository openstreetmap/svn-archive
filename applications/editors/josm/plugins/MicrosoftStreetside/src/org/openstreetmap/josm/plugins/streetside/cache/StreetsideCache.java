// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cache;

import java.net.URL;
import java.util.HashMap;

import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.data.cache.JCSCachedTileLoaderJob;
import org.openstreetmap.josm.data.imagery.TileJobOptions;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL.VirtualEarth;

/**
 * Stores the downloaded pictures locally.
 *
 * @author nokutu
 *
 */
public class StreetsideCache extends JCSCachedTileLoaderJob<String, BufferedImageCacheEntry> {

	private final URL url;
	private final String id;

	/**
	 * Types of images.
	 *
	 * @author nokutu
	 */
	public enum Type {
		/** Full quality image */
		FULL_IMAGE,
		/** Low quality image */
		THUMBNAIL
	}

	/**
	 * Main constructor.
	 *
	 * @param id
	 *          The id of the image.
	 * @param type
	 *          The type of image that must be downloaded (THUMBNAIL or
	 *          FULL_IMAGE).
	 */
	public StreetsideCache(final String id, final Type type) {
		super(Caches.ImageCache.getInstance().getCache(),new TileJobOptions(50000, 50000, new HashMap<String,String>(),50000l));

		if (id == null || type == null) {
			this.id = null;
			url = null;
		} else {
			this.id = id;
			url = VirtualEarth.streetsideTile(id, type == Type.THUMBNAIL);
		}
	}

	@Override
	public String getCacheKey() {
		return id;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	protected BufferedImageCacheEntry createCacheEntry(byte[] content) {
		return new BufferedImageCacheEntry(content);
	}

	@Override
	protected boolean isObjectLoadable() {
		if (cacheData == null) {
			return false;
		}
		final byte[] content = cacheData.getContent();
		return content != null && content.length > 0;
	}
}
