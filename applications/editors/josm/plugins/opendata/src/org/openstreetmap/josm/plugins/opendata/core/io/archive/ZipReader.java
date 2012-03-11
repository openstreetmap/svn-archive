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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.AbstractReader;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.NeptuneReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmlReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.KmzReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.MifReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpReader;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.TabReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.CsvReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.OdsReader;
import org.openstreetmap.josm.plugins.opendata.core.io.tabular.XlsReader;

public class ZipReader extends AbstractReader implements OdConstants {

	private final ZipInputStream zis;
	private final AbstractDataSetHandler handler;
	
	private File file;
    
    public ZipReader(InputStream in, AbstractDataSetHandler handler) {
        this.zis = in instanceof ZipInputStream ? (ZipInputStream) in : new ZipInputStream(in);
        this.handler = handler;
    }

	public static DataSet parseDataSet(InputStream in, AbstractDataSetHandler handler, ProgressMonitor instance) throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException {
		return new ZipReader(in, handler).parseDoc(instance);
	}
	
	public final File getReadFile() {
		return file;
	}
	
	private static final File createTempDir() throws IOException {
	    final File temp = File.createTempFile("josm_opendata_temp_", Long.toString(System.nanoTime()));

	    if (!temp.delete()) {
	        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	    }

	    if (!temp.mkdir()) {
	        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	    }
	    
	    return temp;
	}
	
	private static final void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            file.delete();
        }
		dir.delete();
	}

	public DataSet parseDoc(ProgressMonitor instance) throws IOException, XMLStreamException, FactoryConfigurationError, JAXBException  {
		
	    final File temp = createTempDir();
	    final List<File> candidates = new ArrayList<File>();
	    
	    try {
			ZipEntry entry;
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
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buffer = new byte[8192];
					int count = 0;
					while ((count = zis.read(buffer, 0, buffer.length)) > 0) {
						fos.write(buffer, 0, count);
					}
					fos.close();
					long time = entry.getTime();
					if (time > -1) {
						file.setLastModified(time);
					}
					for (String ext : new String[] {
							CSV_EXT, KML_EXT, KMZ_EXT, XLS_EXT, ODS_EXT, SHP_EXT, MIF_EXT, TAB_EXT, XML_EXT
					}) {
						if (entry.getName().toLowerCase().endsWith("."+ext)) {
							candidates.add(file);
							System.out.println(entry.getName());
							break;
						}
					}
				} else if (!file.mkdir()) {
					throw new IOException("Could not create temp dir: " + file.getAbsolutePath());
				}
			}
			
			file = null;
			
			if (candidates.size() > 1) {
				CandidateChooser dialog = (CandidateChooser) new CandidateChooser(instance.getWindowParent(), candidates).showDialog();
				if (dialog.getValue() != 1) {
					return null; // User clicked Cancel
				}
				file = dialog.getSelectedFile();
			} else if (candidates.size() == 1) {
				file = candidates.get(0);
			}
			
			if (file != null) {
				DataSet from = null;
				FileInputStream in = new FileInputStream(file);
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
					ds.mergeFrom(from);
				}
			}
	    } catch (IllegalArgumentException e) {
	    	System.err.println(e.getMessage());
	    } finally {
	    	deleteDir(temp);
	    }
		
		return ds;
	}
}
