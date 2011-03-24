package org.openstreetmap.gui.jmapviewer.tilesources;


public class TemplatedTMSTileSource extends AbstractOsmTileSource {
    private int maxZoom;

    public TemplatedTMSTileSource(String name, String url, int maxZoom) {
        super(name, url);
        this.maxZoom = maxZoom;
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) {
        return this.baseUrl
        .replaceAll("\\{zoom\\}", Integer.toString(zoom))
        .replaceAll("\\{x\\}", Integer.toString(tilex))
        .replaceAll("\\{y\\}", Integer.toString(tiley))
        .replaceAll("\\{!y\\}", Integer.toString((int)Math.pow(2, zoom)-1-tiley));

    }

    @Override
    public int getMaxZoom() {
        return (maxZoom == 0) ? super.getMaxZoom() : maxZoom;
    }

    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }
}