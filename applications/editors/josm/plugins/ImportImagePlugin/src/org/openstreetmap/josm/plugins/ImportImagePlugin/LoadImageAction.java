package org.openstreetmap.josm.plugins.ImportImagePlugin;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.plugins.ImportImagePlugin.ImageLayer.LayerCreationCancledException;


/**
 * Class extends JosmAction and creates a new image layer.
 *
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class LoadImageAction extends JosmAction {

    private Logger logger = Logger.getLogger(LoadImageAction.class);

    /**
     * Constructor...
     */
    public LoadImageAction() {
        super(tr("Import image"), null, tr("Import georeferenced image"), null, false);
    }

    public void actionPerformed(ActionEvent arg0) {

        // Choose a file
        JFileChooser fc = new JFileChooser(Main.pref.get("plugins.importimage.importpath", null));
        fc.setAcceptAllFileFilterUsed(false);
        int result = fc.showOpenDialog(Main.parent);

        ImageLayer layer = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            Main.pref.put("plugins.importimage.importpath", fc.getCurrentDirectory().getAbsolutePath());
            logger.info("File choosen:" + fc.getSelectedFile());
            try {
                layer = new ImageLayer(fc.getSelectedFile());
            } catch (LayerCreationCancledException e) {
                // if user decides that layer should not be created just return.
                return;
            } catch (Exception e) {
                logger.error("Error while creating image layer: \n" + e.getMessage());
                JOptionPane.showMessageDialog(null, marktr("Error while creating image layer: " + e.getCause()));
                return;

            }

            // Add layer:
            Main.main.addLayer(layer);
            BoundingXYVisitor boundingXYVisitor = new BoundingXYVisitor();
            layer.visitBoundingBox(boundingXYVisitor);
            Main.map.mapView.recalculateCenterScale(boundingXYVisitor);
        }
    }
}
