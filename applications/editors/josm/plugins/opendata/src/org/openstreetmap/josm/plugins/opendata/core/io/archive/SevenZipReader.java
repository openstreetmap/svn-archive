//    JOSM opendata plugin.
//    Copyright (C) 2011-2013 Don-vip
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
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.j7zip.SevenZip.ArchiveExtractCallback;
import org.j7zip.SevenZip.HRESULT;
import org.j7zip.SevenZip.IInStream;
import org.j7zip.SevenZip.MyRandomAccessFile;
import org.j7zip.SevenZip.Archive.IInArchive;
import org.j7zip.SevenZip.Archive.SevenZip.Handler;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.Utils;

public class SevenZipReader extends ArchiveReader {

    private final IInArchive archive = new Handler();
    
    public SevenZipReader(InputStream in, AbstractDataSetHandler handler, boolean promptUser) throws IOException {
        super(handler, handler != null ? handler.getArchiveHandler() : null, promptUser);
        OutputStream out = null;
        try {
            // Write entire 7z file as a temp file on disk as we need random access later, and "in" can be a network stream
            File tmpFile = File.createTempFile("7z_", ".7z", OdUtils.createTempDir());
            out = new FileOutputStream(tmpFile);
            Utils.copyStream(in, out);
            Utils.close(out);
            out = null;
            IInStream random = new MyRandomAccessFile(tmpFile.getPath(), "r");
            if (archive.Open(random) != 0) {
                String message = "Unable to open 7z archive: "+tmpFile.getPath();
                Main.warn(message);
                random.close();
                if (!tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                throw new IOException(message);
            }
        } finally {
            Utils.close(out);
        }
    }
    
    public static DataSet parseDataSet(InputStream in, AbstractDataSetHandler handler, ProgressMonitor instance, boolean promptUser) 
            throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException {
        return new SevenZipReader(in, handler, promptUser).parseDoc(instance);
    }

    @Override protected String getTaskMessage() {
        return tr("Reading 7Zip file...");
    }

    @Override protected void extractArchive(File temp, List<File> candidates) throws IOException, FileNotFoundException {
        archive.Extract(null, -1, IInArchive.NExtract_NAskMode_kExtract, new ExtractCallback(archive, temp, candidates));
    }
    
    private class ExtractCallback extends ArchiveExtractCallback {
        private final List<File> candidates;
        
        public ExtractCallback(IInArchive archive, File tempDir, List<File> candidates) {
            Init(archive);
            super.outputDir = tempDir.getPath();
            this.candidates = candidates;
        }

        @Override 
        public int GetStream(int index, OutputStream[] outStream, int askExtractMode) throws IOException {
            int res = super.GetStream(index, outStream, askExtractMode);
            if (res == HRESULT.S_OK) {
                lookForCandidate(_filePath, candidates, file);
            }
            return res;
        }
    }
}
