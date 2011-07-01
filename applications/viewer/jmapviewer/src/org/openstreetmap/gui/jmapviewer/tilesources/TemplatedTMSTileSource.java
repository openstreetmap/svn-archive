package org.openstreetmap.gui.jmapviewer.tilesources;


public class TemplatedTMSTileSource extends TMSTileSource {
    public TemplatedTMSTileSource(String name, String url, int maxZoom) {
        super(name, url, maxZoom);
    }

    public TemplatedTMSTileSource(String name, String url, int minZoom, int maxZoom) {
        super(name, url, minZoom, maxZoom);
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) {
        return this.baseUrl
        .replaceAll("\\{zoom\\}", Integer.toString(zoom))
        .replaceAll("\\{x\\}", Integer.toString(tilex))
        .replaceAll("\\{y\\}", Integer.toString(tiley))
        .replaceAll("\\{!y\\}", Integer.toString((int)Math.pow(2, zoom)-1-tiley));

    }
}