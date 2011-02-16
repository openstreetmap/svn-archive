package org.openstreetmap.gui.jmapviewer;

import java.awt.Image;
import java.io.IOException;

import javax.swing.ImageIcon;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class OsmTileSource {

    public static final String MAP_MAPNIK = "http://tile.openstreetmap.org";
    public static final String MAP_OSMA = "http://tah.openstreetmap.org/Tiles";

    public static abstract class AbstractOsmTileSource implements TileSource {
        protected String NAME;
        protected String BASE_URL;
        protected String ATTR_IMG_URL;
        protected boolean REQUIRES_ATTRIBUTION = true;

        public AbstractOsmTileSource(String name, String base_url) {
            this(name, base_url, null);
        }

        public AbstractOsmTileSource(String name, String base_url, String attr_img_url) {
            NAME = name;
            BASE_URL = base_url;
            ATTR_IMG_URL = attr_img_url;
            if(ATTR_IMG_URL == null) {
                 REQUIRES_ATTRIBUTION = false;
            }
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

        public String getExtension() {
            return "png";
        }

        /**
         * @throws IOException when subclass cannot return the tile URL
         */
        public String getTilePath(int zoom, int tilex, int tiley) throws IOException {
            return "/" + zoom + "/" + tilex + "/" + tiley + "." + getExtension();
        }

        public String getBaseUrl() {
            return this.BASE_URL;
        }

        public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
            return this.getBaseUrl() + getTilePath(zoom, tilex, tiley);
        }

        @Override
        public String toString() {
            return getName();
        }

        public String getTileType() {
            return "png";
        }

        public int getTileSize() {
            return 256;
        }

        public Image getAttributionImage() {
            if (ATTR_IMG_URL != null)
                return new ImageIcon(ATTR_IMG_URL).getImage();
            else
                return null;
        }

        public boolean requiresAttribution() {
            return REQUIRES_ATTRIBUTION;
        }

        public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
            return "© OpenStreetMap contributors, CC-BY-SA ";
        }

        public String getAttributionLinkURL() {
            return "http://openstreetmap.org/";
        }

        public String getTermsOfUseURL() {
            return "http://www.openstreetmap.org/copyright";
        }

        public double latToTileY(double lat, int zoom) {
            double l = lat / 180 * Math.PI;
            double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
            return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
        }

        public double lonToTileX(double lon, int zoom) {
            return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
        }

        public double tileYToLat(int y, int zoom) {
            return Math.atan(Math.sinh(Math.PI - (Math.PI * y / Math.pow(2.0, zoom - 1)))) * 180 / Math.PI;
        }

        public double tileXToLon(int x, int zoom) {
            return x * 45.0 / Math.pow(2.0, zoom - 3) - 180.0;
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

        private static final String PATTERN = "http://%s.tile.opencyclemap.org/cycle";

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

        @Override
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

        @Override
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
