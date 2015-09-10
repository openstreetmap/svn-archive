// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.grid;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * Subclass to apply some patches to TileController (see FIXME tags).
 * @author Olli
 *
 */
public class ElevationGridTileController extends TileController {

	/**
	 * @param source
	 * @param tileCache
	 * @param listener
	 */
	public ElevationGridTileController(TileSource source, TileCache tileCache,
			TileLoaderListener listener, TileLoader loader) {
		super(source, tileCache, listener);

		tileSource = source; // FIXME: hard-coded in base class (although parameter is given)!!
		tileLoader = loader; // FIXME: hard-coded in base class!
	}

	@Override
	public Tile getTile(int tilex, int tiley, int zoom) {
		int max = (1 << zoom);
		if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
			return null;
		Tile tile = tileCache.getTile(tileSource, tilex, tiley, zoom);
		if (tile == null) {
			// FIXME: Provide/use a factory method here
			tile = new ElevationGridTile(tileSource, tilex, tiley, zoom);
			tileCache.addTile(tile);
			tile.loadPlaceholderFromCache(tileCache);
		}
		if (tile.hasError()) {
			tile.loadPlaceholderFromCache(tileCache);
		}
		if (!tile.isLoaded()) {
			tileLoader.createTileLoaderJob(tile).submit();
		}
		return tile;
	}

	/**
	 *
	 */
}
