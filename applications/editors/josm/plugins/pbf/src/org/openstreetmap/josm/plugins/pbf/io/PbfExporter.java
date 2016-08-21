// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pbf.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmExporter;
import org.openstreetmap.josm.plugins.pbf.PbfConstants;

/**
 * Exports data to a .pbf file.
 * @author Don-vip
 */
public class PbfExporter extends OsmExporter {

    /**
     * Constructs a new {@code PbfExporter}.
     */
    public PbfExporter() {
        super(PbfConstants.FILE_FILTER);
    }

    @Override
    protected void doSave(File file, OsmDataLayer layer) throws IOException {
        try (
            OutputStream out = new FileOutputStream(file);
            PbfWriter w = new PbfWriter(out);
        ) {
            layer.data.getReadLock().lock();
            try {
                w.writeLayer(layer);
            } finally {
                layer.data.getReadLock().unlock();
            }
        }
    }
}
