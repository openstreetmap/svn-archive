// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
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
                    Main.getLayerManager().addLayer(layer);
                    layer.onPostLoadFromFile();
                }
            };
            GuiHelper.runInEDT(uiStuff);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importData(final File file, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        importData(null, null, file, progressMonitor);
    }

    public void importData(final String source, final String layerName, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        importData(source, layerName, null, progressMonitor);
    }

    @Override
    public void importData(List<File> files, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        for (File file : files) {
            importData(file, progressMonitor);
        }
    }
}
