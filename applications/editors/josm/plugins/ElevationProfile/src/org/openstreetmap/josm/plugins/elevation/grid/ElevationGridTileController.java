/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation.grid;

import org.openstreetmap.gui.jmapviewer.JobDispatcher;
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
    private JobDispatcher jobDispatcher; // is private and no getter
    
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
	jobDispatcher = JobDispatcher.getInstance();
    }
    
    /* (non-Javadoc)
     * @see org.openstreetmap.gui.jmapviewer.TileController#getTile(int, int, int)
     */
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
            jobDispatcher.addJob(tileLoader.createTileLoaderJob(tile));
        }
        return tile;
    }
    
    /**
    *
    */
   public void cancelOutstandingJobs() {       
       super.cancelOutstandingJobs(); // should not make a difference but you never know...
       jobDispatcher.cancelOutstandingJobs();
   }
}
