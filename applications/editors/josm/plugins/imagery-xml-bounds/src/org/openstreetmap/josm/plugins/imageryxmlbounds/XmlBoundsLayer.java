// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.BoundsLayerSaveAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.BoundsLayerSaveAsAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * An "OSM data" layer that cannot be uploaded, merged, and in which real OSM data cannot be imported.
 * Its sole purpose is to allow "classic" OSM edition tools to edit Imagery bounds (as XML files)
 * without compromising OSM database integrity.
 *
 * @author Don-vip
 */
public class XmlBoundsLayer extends OsmDataLayer implements LayerChangeListener, ActiveLayerChangeListener, XmlBoundsConstants {

    @Override
    public Action[] getMenuEntries() {
        List<Action> result = new ArrayList<>();
        for (Action action : super.getMenuEntries()) {
            if (action instanceof LayerSaveAction) {
                result.add(new BoundsLayerSaveAction(this));

            } else if (action instanceof LayerSaveAsAction) {
                result.add(new BoundsLayerSaveAsAction(this));

            } else if (!(action instanceof LayerGpxExportAction) && !(action instanceof ConvertToGpxLayerAction)) {
                // Add everything else, expect GPX-related action
                result.add(action);
            }
        }
        result.add(new ShowBoundsAction(this));
        return result.toArray(new Action[result.size()]);
    }

    private static final JosmAction[] ACTIONS_TO_DISABLE = new JosmAction[] {
        MainApplication.getMenu().download,
        MainApplication.getMenu().downloadPrimitive,
        MainApplication.getMenu().downloadReferrers,
        MainApplication.getMenu().upload,
        MainApplication.getMenu().uploadSelection,
        MainApplication.getMenu().update,
        MainApplication.getMenu().updateModified,
        MainApplication.getMenu().updateSelection,
        MainApplication.getMenu().openLocation
    };

    private static final Map<JosmAction, Boolean> ACTIONS_STATES = new HashMap<>();

    /**
     * Constructs a new {@code XmlBoundsLayer}.
     * @param data data set
     */
    public XmlBoundsLayer(DataSet data) {
        this(data, OsmDataLayer.createNewName(), null);
    }

    /**
     * Constructs a new {@code XmlBoundsLayer}.
     * @param data data set
     * @param name Layer name
     * @param associatedFile Associated file (can be null)
     */
    public XmlBoundsLayer(DataSet data, String name, File associatedFile) {
        super(data, name, associatedFile);
        MainApplication.getLayerManager().addLayerChangeListener(this);
        MainApplication.getLayerManager().addActiveLayerChangeListener(this);
    }

    @Override
    public boolean isMergable(Layer other) {
        return other instanceof XmlBoundsLayer;
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("xml_24.png");
    }

    @Override
    public boolean requiresUploadToServer() {
        return false; // Never !
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        Layer newLayer = MainApplication.getLayerManager().getActiveLayer();
        Layer oldLayer = e.getPreviousActiveLayer();
        if (newLayer == this && !(oldLayer instanceof XmlBoundsLayer)) {
            for (JosmAction action : ACTIONS_TO_DISABLE) {
                ACTIONS_STATES.put(action, action.isEnabled());
                action.setEnabled(false);
            }
        } else if (oldLayer == this && !(newLayer instanceof XmlBoundsLayer)) {
            for (JosmAction action : ACTIONS_TO_DISABLE) {
                action.setEnabled(ACTIONS_STATES.get(action));
            }
        }
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        // Do nothing
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (MainApplication.getLayerManager().getEditLayer() instanceof XmlBoundsLayer) {
            for (JosmAction action : ACTIONS_TO_DISABLE) {
                action.setEnabled(false);
            }
        }
    }
}
