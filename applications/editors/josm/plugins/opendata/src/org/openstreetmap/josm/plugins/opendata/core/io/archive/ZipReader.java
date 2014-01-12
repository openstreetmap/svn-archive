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
package org.openstreetmap.josm.plugins.opendata.core.io.archive;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;

public class ZipReader extends ArchiveReader {

	private final ZipInputStream zis;
	
	private ZipEntry entry;
	
    public ZipReader(InputStream in, AbstractDataSetHandler handler, boolean promptUser) {
        super(handler, handler != null ? handler.getArchiveHandler() : null, promptUser);
        this.zis = in instanceof ZipInputStream ? (ZipInputStream) in : new ZipInputStream(in);
    }

	public static DataSet parseDataSet(InputStream in, AbstractDataSetHandler handler, ProgressMonitor instance, boolean promptUser) 
	        throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException {
		return new ZipReader(in, handler, promptUser).parseDoc(instance);
	}

    protected void extractArchive(final File temp, final List<File> candidates) throws IOException, FileNotFoundException {
        while ((entry = zis.getNextEntry()) != null) {
            File file = new File(temp + File.separator + entry.getName());
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (file.exists() && !file.delete()) {
                throw new IOException("Could not delete temp file/dir: " + file.getAbsolutePath());
            }
            if (!entry.isDirectory()) {
                if (!file.createNewFile()) { 
                    throw new IOException("Could not create temp file: " + file.getAbsolutePath());
                }
                // Write temp file
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = zis.read(buffer, 0, buffer.length)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                // Allow handler to perform specific treatments (for example, fix invalid .prj files)
                if (archiveHandler != null) {
                    archiveHandler.notifyTempFileWritten(file);
                }
                // Set last modification date
                long time = entry.getTime();
                if (time > -1) {
                    file.setLastModified(time);
                }
                lookForCandidate(entry.getName(), candidates, file);
            } else if (!file.mkdir()) {
                throw new IOException("Could not create temp dir: " + file.getAbsolutePath());
            }
        }
    }

    @Override protected String getTaskMessage() {
        return tr("Reading Zip file...");
    }
}
