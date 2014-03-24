// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.FileExporter;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ComputeBoundsAction;

/**
 * @author Don-vip
 *
 */
public class XmlBoundsExporter extends FileExporter implements XmlBoundsConstants {

	public XmlBoundsExporter() {
		super(FILE_FILTER);
	}

	@Override
	public void exportData(File file, Layer layer) throws IOException {
		if (layer instanceof OsmDataLayer) {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING));
			try {
				writer.write(new ComputeBoundsAction((OsmDataLayer) layer).getXml());
			} finally {
				writer.close();
			}
		}
	}
}
