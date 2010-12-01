package org.openstreetmap.gui.jmapviewer;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.coor.LatLon;

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

        public AbstractOsmTileSource(String name, String base_url, String attr_img_url)
        {
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

        public String getTilePath(int zoom, int tilex, int tiley) {
            return "/" + zoom + "/" + tilex + "/" + tiley + "." + getExtension();
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

        public int getTileSize() {
            return 256;
        }

        public Image getAttributionImage() {
            if(ATTR_IMG_URL != null)
                return new ImageIcon(ATTR_IMG_URL).getImage();
            else
                return null;
        }

        public boolean requiresAttribution() {
            return REQUIRES_ATTRIBUTION;
        }

        public String getAttributionText(int zoom, LatLon topLeft, LatLon botRight) {
            return "CC-BY-SA OpenStreetMap and Contributors";
        }

        public String getAttributionLinkURL() {
            return "http://openstreetmap.org/";
        }

        public String getTermsOfUseURL() {
            return "http://openstreetmap.org/";
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
