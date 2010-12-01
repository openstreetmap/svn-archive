package org.openstreetmap.josm.plugins.imagery.tms;

import org.openstreetmap.gui.jmapviewer.OsmTileSource;

public class TMSTileSource extends OsmTileSource.AbstractOsmTileSource {
    public TMSTileSource(String name, String url) {
        super(name, url);
    }
    @Override
    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }
}