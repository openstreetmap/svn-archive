package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class OsmTileSource {

	public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
	public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";
	public static final String MAP_CYCLE = "http://www.thunderflames.org/tiles/cycle";

	protected static abstract class AbstractOsmTileSource implements TileSource {

		public int getMaxZoom() {
			return 18;
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	public static class Mapnik extends AbstractOsmTileSource {

		public String getName() {
			return "Mapnik";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
		}

	}

	public static class CycleMap extends AbstractOsmTileSource {

		public String getName() {
			return "OSM Cycle Map";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_CYCLE + super.getTileUrl(zoom, tilex, tiley);
		}
	}

	public static class TilesAtHome extends AbstractOsmTileSource {

		public int getMaxZoom() {
			return 17;
		}

		public String getName() {
			return "TilesAtHome";
		}

		@Override
		public String getTileUrl(int zoom, int tilex, int tiley) {
			return MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
		}

	}
}
