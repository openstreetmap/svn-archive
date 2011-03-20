package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class OsmTileSource {

    public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
    public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles/tile";

    protected static abstract class AbstractOsmTileSource implements TileSource {

        public int getMaxZoom() {
            return 18;
        }

        public int getMinZoom() {
            return 0;
        }

        public String getTileUrl(int zoom, int tilex, int tiley) {
            return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
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

        public static String NAME = "Mapnik";

        public String getName() {
            return NAME;
        }

        @Override
        public String getTileUrl(int zoom, int tilex, int tiley) {
            return MAP_MAPNIK + super.getTileUrl(zoom, tilex, tiley);
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.IfNoneMatch;
        }

    }

    public static class CycleMap extends AbstractOsmTileSource {

        private static final String PATTERN = "http://%s.andy.sandbox.cloudmade.com/tiles/cycle/%d/%d/%d.png";
        public static String NAME = "OSM Cycle Map";

        private static final String[] SERVER = { "a", "b", "c" };

        private int SERVER_NUM = 0;

        @Override
        public String getTileUrl(int zoom, int tilex, int tiley) {
            String url = String.format(PATTERN, new Object[] { SERVER[SERVER_NUM], zoom, tilex, tiley });
            SERVER_NUM = (SERVER_NUM + 1) % SERVER.length;
            return url;
        }

        public int getMaxZoom() {
            return 17;
        }

        public String getName() {
            return NAME;
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.LastModified;
        }

    }

    public static class TilesAtHome extends AbstractOsmTileSource {

        public static String NAME = "TilesAtHome";

        public int getMaxZoom() {
            return 17;
        }

        public String getName() {
            return NAME;
        }

        @Override
        public String getTileUrl(int zoom, int tilex, int tiley) {
            return MAP_OSMA + super.getTileUrl(zoom, tilex, tiley);
        }

        public TileUpdate getTileUpdate() {
            return TileUpdate.IfModifiedSince;
        }
    }
}
