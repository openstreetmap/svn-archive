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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.FileImporter;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.imagery.ImageryReader;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.data.XmlBoundsConverter;
import org.xml.sax.SAXException;

/**
 * @author Don-vip
 *
 */
public class XmlBoundsImporter extends FileImporter implements XmlBoundsConstants {

	public XmlBoundsImporter() {
		super(FILE_FILTER);
	}
	
	public DataSet parseDataSet(final String source) throws IOException, SAXException {
	    return parseDataSet(source, null);
	}

	public DataSet parseDataSet(final File file) throws IOException, SAXException {
        return parseDataSet(null, file);
    }
	
	protected DataSet parseDataSet(final String source, final File file) throws IOException, SAXException {
        ImageryReader reader = null;
        
        try {
            reader = new ValidatingImageryReader(source != null ? source : file.getAbsolutePath());
        } catch (SAXException e)  {
            if (JOptionPane.showConfirmDialog(
                    Main.parent,
                    tr("Validating error in file {0}:\n{1}\nDo you want to continue without validating the file ?", 
                            source != null ? source : file.getPath(), e.getLocalizedMessage()),
                    tr("Open Imagery XML file"),
                    JOptionPane.YES_NO_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
                return null;
            }

            reader = new ImageryReader(source != null ? source : file.getAbsolutePath());
        }
        
        return XmlBoundsConverter.convertImageryEntries(reader.parse());
	}
	
    protected void importData(final String source, final String layerName, final File file, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        try {
            final DataSet dataSet = parseDataSet(source, file);
            final XmlBoundsLayer layer = new XmlBoundsLayer(dataSet, source != null ? layerName : file.getName(), file);
            Runnable uiStuff = new Runnable() {
                @Override
                public void run() {
                    if (dataSet.allPrimitives().isEmpty()) {
                        JOptionPane.showMessageDialog(
                                Main.parent, tr("No data found in file {0}.", source != null ? source : file.getPath()),
                                tr("Open Imagery XML file"), JOptionPane.INFORMATION_MESSAGE);
                    }
                    Main.main.addLayer(layer);
                    layer.onPostLoadFromFile();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                uiStuff.run();
            } else {
                SwingUtilities.invokeLater(uiStuff);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileImporter#importData(java.io.File, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	public void importData(final File file, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		importData(null, null, file, progressMonitor);
	}
	
	public void importData(final String source, final String layerName, ProgressMonitor progressMonitor)
	        throws IOException, IllegalDataException {
	    importData(source, layerName, null, progressMonitor);
    }

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.io.FileImporter#importData(java.util.List, org.openstreetmap.josm.gui.progress.ProgressMonitor)
	 */
	@Override
	public void importData(List<File> files, ProgressMonitor progressMonitor)
			throws IOException, IllegalDataException {
		for (File file : files) {
			importData(file, progressMonitor);
		}
	}
}
