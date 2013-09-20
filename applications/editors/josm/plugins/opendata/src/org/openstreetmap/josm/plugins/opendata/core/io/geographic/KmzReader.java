//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class KmzReader extends AbstractReader implements OdConstants {

	private ZipInputStream zis;
    
    public KmzReader(ZipInputStream zis) {
        this.zis = zis;
    }

	public static DataSet parseDataSet(InputStream in, ProgressMonitor instance) throws IOException, XMLStreamException, FactoryConfigurationError {
		return new KmzReader(new ZipInputStream(in)).parseDoc(instance);
	}

	private DataSet parseDoc(ProgressMonitor instance) throws IOException, XMLStreamException, FactoryConfigurationError  {
	    ZipEntry entry;
	    do {
	        entry = zis.getNextEntry();
	        if (entry == null) {
	            Main.warn("No KML file found");
	            return null;
	        }
	    } while (!entry.getName().toLowerCase().endsWith(".kml"));
		long size = entry.getSize();
		byte[] buffer;
		if (size > 0) {
            buffer = new byte[(int) size];
            int off = 0;
            int count = 0;
            while ((count = zis.read(buffer, off, (int) size)) > 0) {
                off += count;
                size -= count;
            }
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            do {
                b = zis.read();
                if (b != -1) {
                    out.write(b);
                }
            } while (b != -1);
            buffer = out.toByteArray();
        }
	    
		return KmlReader.parseDataSet(new ByteArrayInputStream(buffer), instance);
	}
}
