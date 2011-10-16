//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileExporter#exportData(java.io.File, org.openstreetmap.josm.gui.layer.Layer)
	 */
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
