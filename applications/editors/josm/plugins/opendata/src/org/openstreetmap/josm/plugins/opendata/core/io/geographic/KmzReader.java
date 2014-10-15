// License: GPL. For details, see LICENSE file.
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

public class KmzReader extends AbstractReader {

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
