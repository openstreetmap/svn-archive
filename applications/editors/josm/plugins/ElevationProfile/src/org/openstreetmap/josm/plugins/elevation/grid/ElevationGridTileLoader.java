// License: GPL. For details, see LICENSE file.
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

    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        CheckParameterUtil.ensureParameterNotNull(tile);

        return new TileJob() {

            @Override
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

            @Override
            public Tile getTile() {
                return tile;
            }

            @Override
            public void submit() {
                run();
            }

            @Override
            public void submit(boolean force) {
                submit();

            }
        };
    }

    @Override
    public void cancelOutstandingTasks() {
        // intentionally left blank
    }

}
