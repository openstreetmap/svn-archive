package org.openstreetmap.josm.plugins.rasterfilters.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerAction;
import org.openstreetmap.josm.plugins.rasterfilters.gui.FiltersDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;

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
        new ImageProvider("josm_filters_48.png").getResource().attachImageIcon(this, true);
    }

    public void addFiltersDialog(FiltersDialog dialog) {
        dialogs.add(dialog);
    }

    public void removeFiltersDialog(FiltersDialog dialog) {
        dialogs.remove(dialog);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Layer layer = MainApplication.getLayerManager().getActiveLayer();

        if (layer instanceof ImageryLayer) {
            for (FiltersDialog temp : dialogs) {

                if (temp.getLayer().equals(layer)) {
                    try {
                        temp.createAndShowGui();
                    } catch (MalformedURLException e1) {
                        Logging.error(e1);
                    }
                    break;
                }

            }
        } else {
            Logging.debug("The layer is not the instance of " + ImageryLayer.class.getCanonicalName());
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
