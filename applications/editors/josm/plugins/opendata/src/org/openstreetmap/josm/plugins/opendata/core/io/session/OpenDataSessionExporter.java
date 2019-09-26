package org.openstreetmap.josm.plugins.opendata.core.io.session;

import java.io.IOException;
import java.io.OutputStream;

import org.openstreetmap.josm.io.session.GenericSessionExporter;
import org.openstreetmap.josm.io.session.OsmDataSessionExporter;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;

public class OpenDataSessionExporter extends GenericSessionExporter<OdDataLayer> {

    public OpenDataSessionExporter(OdDataLayer layer) { // NO_UCD (test only)
        super(layer, "open-data", "0.1", "osm");
    }

    @Override
    protected void addDataFile(OutputStream out) throws IOException {
        OsmDataSessionExporter.export(layer.data, out);
    }
}
