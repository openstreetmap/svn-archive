package org.openstreetmap.josm.plugins.imagery.tms;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;

public class TMSTileSource extends OsmTileSource.AbstractOsmTileSource {
    private int maxZoom;

    public TMSTileSource(String name, String url, int maxZoom) {
        super(name, url);
        this.maxZoom = maxZoom;
    }

    @Override
    public int getMaxZoom() {
        return (maxZoom == 0) ? super.getMaxZoom() : maxZoom;
    }

    @Override
    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }
}
