package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class OsmTileSource {

    public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
    public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles";

    public static abstract class AbstractOsmTileSource implements TileSource {
        protected String NAME;
        protected String BASE_URL;
        public AbstractOsmTileSource(String name, String base_url)
        {
            NAME = name;
            BASE_URL = base_url;
        }

        public String getName() {
            return NAME;
        }

        public int getMaxZoom() {
            return 18;
        }

        public int getMinZoom() {
            return 0;
        }

        public String getTilePath(int zoom, int tilex, int tiley) {
            return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
        }

        public String getBaseUrl() {
            return this.BASE_URL;
        }

        public String getTileUrl(int zoom, int tilex, int tiley) {
            return this.getBaseUrl() + getTilePath(zoom, tilex, tiley);
        }

        @Override
        public String toString() {
            return getName();
        }

        public String getTileType() {
            return "png";
        }

    }

    public static class Mapnik extends AbstractOsmTileSource {
        public Mapnik() {
            super("Mapnik", MAP_MAPNIK);
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }

    }

    public static class CycleMap extends AbstractOsmTileSource {

        private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle";

        private static final String[] SERVER = { "a", "b", "c" };

        private int SERVER_NUM = 0;

        public CycleMap() {
            super("OSM Cycle Map", PATTERN);
        }

        @Override
        public String getBaseUrl() {
            String url = String.format(this.BASE_URL, new Object[] { SERVER[SERVER_NUM] });
            SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
            return url;
        }

        public int getMaxZoom() {
            return 17;
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.LastModified;
        }

    }

    public static abstract class OsmaSource extends AbstractOsmTileSource {
        String osmaSuffix;
        public OsmaSource(String name, String osmaSuffix) {
            super(name, MAP_OSMA);
            this.osmaSuffix = osmaSuffix;
        }

        public int getMaxZoom() {
            return 17;
        }

        @Override
        public String getBaseUrl() {
            return MAP_OSMA + "/" + osmaSuffix;
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.IfModifiedSince;
        }
    }
    public static class TilesAtHome extends OsmaSource {
        public TilesAtHome() {
            super("TilesAtHome", "tile");
        }
    }
    public static class Maplint extends OsmaSource {
        public Maplint() {
            super("Maplint", "maplint");
        }
    }
}
