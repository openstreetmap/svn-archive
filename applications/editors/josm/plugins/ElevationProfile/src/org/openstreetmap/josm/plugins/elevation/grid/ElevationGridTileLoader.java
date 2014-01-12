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

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * @author Olli
 *
 */
public class ElevationGridTileLoader implements TileLoader {
    protected TileLoaderListener listener;

    public ElevationGridTileLoader(TileLoaderListener listener) {
	CheckParameterUtil.ensureParameterNotNull(listener);
        this.listener = listener;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.gui.jmapviewer.interfaces.TileLoader#createTileLoaderJob(org.openstreetmap.gui.jmapviewer.Tile)
     */
    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
	CheckParameterUtil.ensureParameterNotNull(tile);
	
	return new TileJob() {

            public void run() {
                synchronized (tile) {
                    if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                        return;
                    tile.initLoading();
                }
                try {
                    tile.loadImage(null);
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                } catch (Exception e) {
                    tile.setError(e.getMessage());
                    listener.tileLoadingFinished(tile, false);                                        
                } finally {
                    tile.finishLoading();
                }
            }

            public Tile getTile() {
                return tile;
            }
        };
    }
   
}
