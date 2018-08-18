// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions.newlayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromFile;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromKML;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLGroundOverlay;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLReader;
import org.openstreetmap.josm.spi.preferences.Config;

/**
 * Action responsible for creation of new layers based on image files.
 */
public class NewLayerFromFileAction extends JosmAction {

    String m_lastdirprefname = "piclayer.lastdir";

    /**
     * Provides filtering of only image files.
     */
    private static class ImageFileFilter extends FileFilter {

        private String[] supportedExtensions;

        ImageFileFilter() {
            List<String> extensions = new ArrayList<>();
            extensions.add("zip");
            extensions.add("kml");
            for (String ext : ImageIO.getReaderFormatNames()) {
                extensions.add(ext);
            }
            supportedExtensions = extensions.toArray(new String[0]);
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String fileExtension = PicLayerFromFile.getFileExtension(f);

            // Unfortunately, getReaderFormatNames does not always return ALL extensions in
            // both lower and upper case, so we can not do a search in the array
            for (String e: supportedExtensions) {
                if (e.equalsIgnoreCase(fileExtension)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String getDescription() {
            return tr("Supported image files, *.zip, *.kml");
        }
    }

    /**
     * Constructor...
     */
    public NewLayerFromFileAction() {
        super(tr("New picture layer from file..."), "layericon24", null, null, false);
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {

        // Choose a file
        JFileChooser fc = new JFileChooser(Config.getPref().get(m_lastdirprefname));
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new ImageFileFilter());

        fc.setMultiSelectionEnabled(true);
        int result = fc.showOpenDialog(MainApplication.getMainFrame());

        // Create a layer?
        if (result == JFileChooser.APPROVE_OPTION) {
            // The first loaded layer will be placed at the top of any other layer of the same class,
            // or at the bottom of the stack if there is no such layer yet
            // The next layers we load will be placed one after the other after this first layer
            int newLayerPos = MainApplication.getLayerManager().getLayers().size();
            for (Layer l : MainApplication.getLayerManager().getLayersOfType(PicLayerAbstract.class)) {
                int pos = MainApplication.getLayerManager().getLayers().indexOf(l);
                if (pos < newLayerPos) newLayerPos = pos;
            }

            for (File file : fc.getSelectedFiles()) {
                // TODO: we need a progress bar here, it can take quite some time

                Config.getPref().put(m_lastdirprefname, file.getParent());

                // Create layer from file
                if ("kml".equalsIgnoreCase(PicLayerFromFile.getFileExtension(file))) {
                    KMLReader kml = new KMLReader(file);
                    kml.process();
                    JOptionPane.showMessageDialog(null, tr("KML calibration is in beta stage and may produce incorrectly calibrated layers!\n"+
                    "Please use {0} to upload your KMLs that were calibrated incorrectly.",
                    "https://josm.openstreetmap.de/ticket/5451"), tr("Notification"), JOptionPane.INFORMATION_MESSAGE);
                    for (KMLGroundOverlay overlay : kml.getGroundOverlays()) {
                        //TODO: zoom to whole picture, not only the last
                        addNewLayerFromKML(file, overlay, newLayerPos);
                    }
                } else {
                    addNewLayerFromFile(file, newLayerPos, fc.getSelectedFiles().length == 1);
                }
            }
        }
    }

    private void addNewLayerFromFile(File file, int newLayerPos, boolean isZoomToLayer) {
        try {
            PicLayerFromFile layer = new PicLayerFromFile(file);
            layer.initialize();

            placeLayer(layer, newLayerPos, isZoomToLayer);
        } catch (IOException e) {
            // Failed
            System.out.println("NewLayerFromFileAction::actionPerformed - " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void placeLayer(PicLayerAbstract layer, int newLayerPos, boolean isZoomToLayer) throws IOException {
        // Add layer only if successfully initialized

        MainApplication.getLayerManager().addLayer(layer);
        MainApplication.getMap().mapView.moveLayer(layer, newLayerPos++);

        if (isZoomToLayer && Config.getPref().getInt("piclayer.zoom-on-load", 1) != 0) {
            // if we are loading a single picture file, zoom on it, so that the user can see something
            BoundingXYVisitor v = new BoundingXYVisitor();
            layer.visitBoundingBox(v);
            MainApplication.getMap().mapView.zoomTo(v);
        }
    }

    private void addNewLayerFromKML(File root, KMLGroundOverlay overlay, int newLayerPos) {
        try {
            PicLayerFromKML layer = new PicLayerFromKML(root, overlay);
            layer.initialize();

            placeLayer(layer, newLayerPos, true);
        } catch (IOException e) {
            // Failed
            System.out.println("NewLayerFromFileAction::actionPerformed - " + e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
        }
    }
}
