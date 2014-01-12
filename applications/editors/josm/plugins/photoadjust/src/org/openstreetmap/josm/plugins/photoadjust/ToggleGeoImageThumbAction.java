package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Toggle the image display between thumbnails and symbols.
 */
class ToggleGeoImageThumbAction extends AbstractAction implements LayerAction {

    /** Define menu entry (text and image). */
    public ToggleGeoImageThumbAction() {
        super(tr("Toggle the image thumbnail preview"),
              ImageProvider.get("togglegit"));
    }

    /** Get the layer this menu entry belongs to. */
    private GeoImageLayer getSelectedLayer() {
        return (GeoImageLayer)LayerListDialog.getInstance().getModel()
            .getSelectedLayers().get(0);
    }

    /** This is called after the menu entry was selected. */
    @Override
    public void actionPerformed(ActionEvent arg0) {
	GeoImageLayer layer = getSelectedLayer();
	layer.setUseThumbs(!layer.isUseThumbs());
	Main.map.mapView.repaint();
    }

    /** Check if there is any suitable image to be toggled. */
    private boolean enabled(GeoImageLayer layer) {
        return layer.getImages().size() > 0;
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
