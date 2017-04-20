package org.openstreetmap.josm.plugins.rasterfilters.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.plugins.rasterfilters.gui.FiltersDialog;
import org.openstreetmap.josm.tools.ImageProvider;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * The action that is called when user click on 'Choose filters' button
 *
 * @author Nipel-Crumple
 */
public final class ShowFiltersDialogAction extends AbstractAction implements LayerAction {

    private List<FiltersDialog> dialogs = new ArrayList<>();

    /**
     * Constructs a new {@code ShowFiltersDialogAction}.
     */
    public ShowFiltersDialogAction() {
        putValue(NAME, tr("Filters"));
        putValue(SHORT_DESCRIPTION, tr("Choose Filter"));
        putValue("ImageResource", new ImageProvider("josm_filters_48.png").getResource());
    }

    public void addFiltersDialog(FiltersDialog dialog) {
        dialogs.add(dialog);
    }

    public void removeFiltersDialog(FiltersDialog dialog) {
        dialogs.remove(dialog);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Layer layer = Main.getLayerManager().getActiveLayer();

        if (layer instanceof ImageryLayer) {
            for (FiltersDialog temp : dialogs) {

                if (temp.getLayer().equals(layer)) {
                    try {
                        temp.createAndShowGui();
                    } catch (MalformedURLException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }

            }
        } else {
            Main.debug("The layer is not the instance of " + ImageryLayer.class.getCanonicalName());
        }
    }

    public FiltersDialog getDialogByLayer(Layer layer) {
        for (FiltersDialog dialog : dialogs) {

            if (dialog.getLayer().equals(layer)) {
                return dialog;
            }

        }

        return null;
    }

    @Override
    public boolean supportLayers(List<Layer> layers) {
        return true;
    }

    @Override
    public Component createMenuComponent() {
        return new JMenuItem(this);
    }
}
