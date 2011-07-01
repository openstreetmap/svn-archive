package org.openstreetmap.gui.jmapviewer.tilesources;


public class TMSTileSource extends AbstractOsmTileSource {
    protected int maxZoom;
    protected int minZoom = 0;

    public TMSTileSource(String name, String url, int maxZoom) {
        super(name, url);
        this.maxZoom = maxZoom;
    }

    public TMSTileSource(String name, String url, int minZoom, int maxZoom) {
        super(name, url);
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
    }

    @Override
    public int getMinZoom() {
        return (minZoom == 0) ? super.getMinZoom() : minZoom;
    }

    @Override
    public int getMaxZoom() {
        return (maxZoom == 0) ? super.getMaxZoom() : maxZoom;
    }

    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }
}
