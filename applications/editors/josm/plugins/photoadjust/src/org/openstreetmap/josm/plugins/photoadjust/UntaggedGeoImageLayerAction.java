// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Create a new GeoImage layer and move the untagged photos to it.
 */
public class UntaggedGeoImageLayerAction
    extends AbstractAction implements LayerAction {

    /** Define menu entry (text and image). */
    public UntaggedGeoImageLayerAction() {
        super(tr("New layer for untagged images"), ImageProvider.get("untaglayer"));
    }

    /**
     * Get the layer this menu entry belongs to.
     * @return the layer this menu entry belongs to
     */
    private static GeoImageLayer getSelectedLayer() {
        return (GeoImageLayer)LayerListDialog.getInstance().getModel()
            .getSelectedLayers().get(0);
    }

    /** This is called after the menu entry was selected. */
    @Override
    public void actionPerformed(ActionEvent evt) {
        GeoImageLayer layer = getSelectedLayer();
        if (layer != null) {
            List<ImageEntry> untagged = new ArrayList<>();
            List<ImageEntry> images = layer.getImages();
            ImageEntry img;
            for (int idx = images.size() - 1; idx >= 0; idx--) {
                img = images.get(idx);
                if (img.getPos() == null) {
                    // Move this image to the new layer and delete it
                    // from the original layer.
                    untagged.add(img);
                    layer.getImageData().removeImage(img);
                }
            }
            MainApplication.getLayerManager()
                .addLayer(new GeoImageLayer(untagged, layer.getGpxLayer(),
                                            tr("Untagged Images"),
                                            layer.isUseThumbs()));
        }
    }

    /**
     * Check if there is any image without coordinates.
     * @param layer geo image layer
     * @return {@code true} if there is any image without coordinates
     */
    private static boolean enabled(GeoImageLayer layer) {
        if (layer != null) {
            for (ImageEntry img: layer.getImages()) {
                if (img.getPos() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Create actual menu entry and define if it is enabled or not. */
    @Override
    public Component createMenuComponent() {
        JMenuItem toggleItem = new JMenuItem(this);
        toggleItem.setEnabled(enabled(getSelectedLayer()));
        return toggleItem;
    }

    /** Check if the current layer is supported. */
    @Override
    public boolean supportLayers(List<Layer> layers) {
        return layers.size() == 1 && layers.get(0) instanceof GeoImageLayer;
    }
}
