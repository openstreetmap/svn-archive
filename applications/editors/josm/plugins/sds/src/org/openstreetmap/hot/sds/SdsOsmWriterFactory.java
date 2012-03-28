// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import java.io.PrintWriter;

import org.openstreetmap.josm.io.OsmWriter;
import org.openstreetmap.josm.io.OsmWriterFactory;

/**
 * Replaces JOSM's original writer factory, so that JOSM uses 
 * our writer instead of its own.
 * 
 * @author Frederik Ramm
 */
public class SdsOsmWriterFactory extends OsmWriterFactory {

	SeparateDataStorePlugin plugin;
	
	public SdsOsmWriterFactory(SeparateDataStorePlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
    protected OsmWriter createOsmWriterImpl(PrintWriter out, boolean osmConform, String version) {
        return new SdsOsmWriter(plugin, out, osmConform, version);
    }
}
