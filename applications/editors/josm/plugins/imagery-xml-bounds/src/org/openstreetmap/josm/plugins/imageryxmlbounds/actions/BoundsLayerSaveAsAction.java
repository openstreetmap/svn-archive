// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;

import org.openstreetmap.josm.actions.SaveAsAction;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerSaveAsAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 * Save Imagery XML file.
 * @author Don-vip
 */
public class BoundsLayerSaveAsAction extends LayerSaveAsAction {

    /**
     * Save Imagery XML file
     */
    public static class SaveBoundsAsAction extends SaveAsAction {

        @Override
        public File getFile(Layer layer) {
            return openFileDialog(layer);
        }

        /**
         * Opens a "File/Save as" dialog if the given layer is an XML bounds layer
         * @param layer layer
         * @return chosen destination file, or {@code null}
         */
        public static File openFileDialog(Layer layer) {
            if (layer instanceof XmlBoundsLayer) {
                return createAndOpenSaveFileChooser(tr("Save Imagery XML file"), XmlBoundsConstants.EXTENSION);
            } else {
                return null;
            }
        }
    }

    protected final XmlBoundsLayer layer;

    /**
     * Constructs a new {@code BoundsLayerSaveAsAction}.
     * @param layer XML Bounds layer
     */
    public BoundsLayerSaveAsAction(XmlBoundsLayer layer) {
        super(layer);
        this.layer = layer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SaveBoundsAsAction().doSave(layer);
    }
}
