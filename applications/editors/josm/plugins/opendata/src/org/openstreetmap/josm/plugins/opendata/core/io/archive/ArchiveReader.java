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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.gui.DialogPrompter;
import org.openstreetmap.josm.plugins.opendata.core.io.NeptuneReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmlReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmzReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.TabReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.OdsReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.XlsReader;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public abstract class ArchiveReader extends AbstractReader implements OdConstants {

    protected final AbstractDataSetHandler handler;
    protected final ArchiveHandler archiveHandler;
    protected final boolean promptUser;

    private File file;

    public ArchiveReader(AbstractDataSetHandler handler, ArchiveHandler archiveHandler, boolean promptUser) {
        this.handler = handler;
        this.archiveHandler = archiveHandler;
        this.promptUser = promptUser;
    }
    
    public final File getReadFile() {
        return file;
    }
    
    protected abstract void extractArchive(final File temp, final List<File> candidates) throws IOException, FileNotFoundException;
    
    protected abstract String getTaskMessage();
        
    public DataSet parseDoc(final ProgressMonitor progressMonitor) throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException  {
        
        final File temp = OdUtils.createTempDir();
        final List<File> candidates = new ArrayList<File>();
        
        try {
            if (progressMonitor != null) {
                progressMonitor.beginTask(getTaskMessage());
            }
            extractArchive(temp, candidates);
            
            file = null;
            
            if (promptUser && candidates.size() > 1) {
                DialogPrompter<CandidateChooser> prompt = new DialogPrompter() {
                    @Override
                    protected CandidateChooser buildDialog() {
                        return new CandidateChooser(progressMonitor.getWindowParent(), candidates);
                    }
                };
                if (prompt.promptInEdt().getValue() == 1) {
                    file = prompt.getDialog().getSelectedFile();
                }
            } else if (!candidates.isEmpty()) {
                file = candidates.get(0);
            }
            
            if (file == null) {
                return null;
            } else if (!file.exists()) {
                Main.warn("File does not exist: "+file.getPath());
                return null;
            } else {
                DataSet from = null;
                FileInputStream in = new FileInputStream(file);
                ProgressMonitor instance = null;
                if (progressMonitor != null) {
                    instance = progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false);
                }
                if (file.getName().toLowerCase().endsWith(CSV_EXT)) {
                    from = CsvReader.parseDataSet(in, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(KML_EXT)) {
                    from = KmlReader.parseDataSet(in, instance);
                } else if (file.getName().toLowerCase().endsWith(KMZ_EXT)) {
                    from = KmzReader.parseDataSet(in, instance);
                } else if (file.getName().toLowerCase().endsWith(XLS_EXT)) {
                    from = XlsReader.parseDataSet(in, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(ODS_EXT)) {
                    from = OdsReader.parseDataSet(in, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(SHP_EXT)) {
                    from = ShpReader.parseDataSet(in, file, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(MIF_EXT)) {
                    from = MifReader.parseDataSet(in, file, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(TAB_EXT)) {
                    from = TabReader.parseDataSet(in, file, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(GML_EXT)) {
                    from = GmlReader.parseDataSet(in, handler, instance);
                } else if (file.getName().toLowerCase().endsWith(XML_EXT)) {
                    if (OdPlugin.getInstance().xmlImporter.acceptFile(file)) {
                        from = NeptuneReader.parseDataSet(in, handler, instance);
                    } else {
                        System.err.println("Unsupported XML file: "+file.getName());
                    }
                    
                } else {
                    System.err.println("Unsupported file extension: "+file.getName());
                }
                if (from != null) {
                    ds = from;
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } finally {
            OdUtils.deleteDir(temp);
            if (progressMonitor != null) {
                progressMonitor.finishTask();
            }
        }
        
        return ds;
    }

    protected final void lookForCandidate(String entryName, final List<File> candidates, File file) {
        // Test file name to see if it may contain useful data
        for (String ext : new String[] {
                CSV_EXT, KML_EXT, KMZ_EXT, XLS_EXT, ODS_EXT, SHP_EXT, MIF_EXT, TAB_EXT, GML_EXT
        }) {
            if (entryName.toLowerCase().endsWith("."+ext)) {
                candidates.add(file);
                System.out.println(entryName);
                break;
            }
        }
        // Special treatment for XML files (check supported XSD), unless handler explicitely skip it
        if (XML_FILE_FILTER.accept(file) && ((archiveHandler != null && archiveHandler.skipXsdValidation()) 
                || OdPlugin.getInstance().xmlImporter.acceptFile(file))) {
            candidates.add(file);
            System.out.println(entryName);
        }
    }
}
