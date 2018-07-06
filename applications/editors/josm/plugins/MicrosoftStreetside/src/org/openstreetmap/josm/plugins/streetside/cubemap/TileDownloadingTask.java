// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.cubemap;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.plugins.streetside.cache.StreetsideCache;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL;

import us.monoid.web.Resty;

public class TileDownloadingTask implements Callable<String> {

  final static Logger logger = Logger.getLogger(TileDownloadingTask.class);

	private String tileId;
	private final long startTime = System.currentTimeMillis();
	private StreetsideCache cache;
	protected CubemapBuilder cb;

	/**
	   * Listeners of the class.
	   */
    private final List<ITileDownloadingTaskListener> listeners = new CopyOnWriteArrayList<>();

	boolean cancelled = false;

	public TileDownloadingTask(String id) {
		tileId = id;
		cb = CubemapBuilder.getInstance();
		addListener(CubemapBuilder.getInstance());
	}

	/**
	   * Adds a new listener.
	   *
	   * @param lis Listener to be added.
	   */
	public final void addListener(final ITileDownloadingTaskListener lis) {
	    listeners.add(lis);
	}

	/**
	 * @return the tileId
	 */
	public String getId() {
		return tileId;
	}

	/**
	 * @param id the tileId to set
	 */
	public void setId(String id) {
		tileId = id;
	}

	/**
	 * @return the cache
	 */
	public StreetsideCache getCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(StreetsideCache cache) {
		this.cache = cache;
	}

	/**
	 * @return the cb
	 */
	public CubemapBuilder getCb() {
		return cb;
	}

	/**
	 * @param cb the cb to set
	 */
	public void setCb(CubemapBuilder cb) {
		this.cb = cb;
	}

	/**
	 * @param cancelled the cancelled to set
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public String call() throws Exception {

		BufferedImage img = ImageIO.read(new Resty().bytes(
				StreetsideURL.VirtualEarth.streetsideTile(tileId, false).toExternalForm())
				.stream());

		if (img == null) {
			logger.error("Download of BufferedImage " + tileId + " is null!");
		}

		CubemapBuilder.getInstance().getTileImages().put(tileId, img);

		fireTileAdded(tileId);

		if (StreetsideProperties.DEBUGING_ENABLED.get()) {
		  long endTime = System.currentTimeMillis();
	    long runTime = (endTime-startTime)/1000;
	    logger.debug(MessageFormat.format("Loaded image for {0} in {1} seconds.", tileId, runTime));
		}

		return tileId;
	}

	private void fireTileAdded(String id) {
	    listeners.stream().filter(Objects::nonNull).forEach(lis -> lis.tileAdded(id));
	}
}