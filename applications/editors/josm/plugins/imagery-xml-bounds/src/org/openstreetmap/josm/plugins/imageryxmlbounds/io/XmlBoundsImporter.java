// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.FileImporter;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.imagery.ImageryReader;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.data.XmlBoundsConverter;
import org.openstreetmap.josm.tools.Logging;
import org.xml.sax.SAXException;

/**
 * Imports a JOSM "Maps" file into an OSM data set, to allow editing it.
 * @author Don-vip
 */
public class XmlBoundsImporter extends FileImporter implements XmlBoundsConstants {

	/**
	 * Constructs a new {@code XmlBoundsImporter}.
	 */
    public XmlBoundsImporter() {
        super(FILE_FILTER);
    }

    /**
     * Loads the given input source URL and returns corresponding data set.
     * @param source input source URL
     * @return OSM data set
     * @throws IOException if any I/O error occurs
     * @throws SAXException if any SAX error occurs
     */
    public DataSet parseDataSet(final String source) throws IOException, SAXException {
        return parseDataSet(source, null);
    }

    /**
     * Loads the given input file and returns corresponding data set.
     * @param file input file
     * @return OSM data set
     * @throws IOException if any I/O error occurs
     * @throws SAXException if any SAX error occurs
     */
    public DataSet parseDataSet(final File file) throws IOException, SAXException {
        return parseDataSet(null, file);
    }

    protected DataSet parseDataSet(final String source, final File file) throws IOException, SAXException {
        List<ImageryInfo> entries;
		try (ImageryReader reader = new ValidatingImageryReader(source != null ? source : file.getAbsolutePath())) {
            entries = reader.parse();
        } catch (SAXException e) {
      	    Logging.trace(e);
            if (JOptionPane.showConfirmDialog(
                    MainApplication.getMainFrame(),
                    tr("Validating error in file {0}:\n{1}\nDo you want to continue without validating the file ?",
                            source != null ? source : file.getPath(), e.getLocalizedMessage()),
                    tr("Open Imagery XML file"),
                    JOptionPane.YES_NO_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
                return null;
            }

            try (ImageryReader reader = new ImageryReader(source != null ? source : file.getAbsolutePath())) {
            	entries = reader.parse();
            }
        }

        return XmlBoundsConverter.convertImageryEntries(entries);
    }

    protected void importData(final String source, final String layerName, final File file, ProgressMonitor progressMonitor)
            throws IOException {
        try {
            final DataSet dataSet = parseDataSet(source, file);
            final XmlBoundsLayer layer = new XmlBoundsLayer(dataSet, source != null ? layerName : file.getName(), file);
            GuiHelper.runInEDT(() -> {
			    if (dataSet.allPrimitives().isEmpty()) {
			        JOptionPane.showMessageDialog(
			                MainApplication.getMainFrame(), tr("No data found in file {0}.", source != null ? source : file.getPath()),
			                tr("Open Imagery XML file"), JOptionPane.INFORMATION_MESSAGE);
			    }
			    MainApplication.getLayerManager().addLayer(layer);
			    layer.onPostLoadFromFile();
			});
        } catch (SAXException e) {
            Logging.error(e);
        }
    }

    @Override
    public void importData(final File file, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        importData(null, null, file, progressMonitor);
    }

    @Override
    public void importData(List<File> files, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        for (File file : files) {
            importData(file, progressMonitor);
        }
    }
}
