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

import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTMSTileSource;

/**
 * @author Olli
 *
 */
public class ElevationGridTileSource extends AbstractTMSTileSource {
    public ElevationGridTileSource(String name) {
	super(name, "");
	// TODO Auto-generated constructor stub
    }

    @Override
    public TileUpdate getTileUpdate() {
	return TileUpdate.None;
    }

    @Override
    public String getName() {
	return "eg";
    }

    public String getExtension() {
        return "";
    }

    /**
     * @throws IOException when subclass cannot return the tile URL
     */
    public String getTilePath(int zoom, int tilex, int tiley) throws IOException {
        return "/" + zoom + "/" + tilex + "/" + tiley + "." + getExtension();
    }

    public String getBaseUrl() {
        return "";
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
        return this.getBaseUrl() + getTilePath(zoom, tilex, tiley);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getTileType() {
        return "";
    }

    @Override
    public int getTileSize() {
	// TODO
        return 256;
    }

}
