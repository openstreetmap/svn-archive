/*
 * GPLv2 or 3, Copyright (c) 2010  Andrzej Zaborowski
 */
package wmsturbochallenge;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This is the main class for the game plugin.
 */
public class WMSRacer extends Plugin implements LayerChangeListener, ActiveLayerChangeListener {
    public WMSRacer(PluginInformation info) {
        super(info);
        driveAction.updateEnabledState();

        JMenu toolsMenu = MainApplication.getMenu().toolsMenu;
        toolsMenu.addSeparator();
        toolsMenu.add(new JMenuItem(driveAction));
    }

    /* Rather than add an action or main menu entry we should add
     * an entry in the new layer's context menus in layerAdded
     * but there doesn't seem to be any way to do that :( */
    protected static class DriveAction extends JosmAction {
        public MapFrame frame = null;
        public Layer currentLayer = null;
        protected Layer groundLayer = null;

        public DriveAction() {
            super(tr("Go driving"), "wmsracer",
                    tr("Drive a race car on this layer"),
                    null, true);
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            if (groundLayer == null ||
                    !groundLayer.isBackgroundLayer())
                return;

            new GameWindow(groundLayer);
        }

        @Override
        public void updateEnabledState() {
            if (frame == null) {
                groundLayer = null;
                setEnabled(false);
                return;
            }

            if (currentLayer != null &&
                    currentLayer.isBackgroundLayer()) {
                groundLayer = currentLayer;
                setEnabled(true);
                return;
            }

            /* TODO: should only iterate through visible layers?
             * or only wms layers? or perhaps we should allow
             * driving on data/gpx layers too, or the full layer
             * stack (by calling mapView.paint() instead of
             * layer.paint()?  Nah.
             * (Note that for GPX or Data layers we could do
             * some clever rendering directly on our perspectivic
             * pseudo-3d surface by defining a strange projection
             * like that or rendering in "stripes" at different
             * horizontal scanlines (lines equidistant from
             * camera eye)) */
            for (Layer l : frame.mapView.getLayerManager().getLayers()) {
                if (l.isBackgroundLayer()) {
                    groundLayer = l;
                    setEnabled(true);
                    return;
                }
            }

            groundLayer = null;
            setEnabled(false);
        }
    }

    protected DriveAction driveAction = new DriveAction();

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null) {
            MainApplication.getLayerManager().removeLayerChangeListener(this);
            MainApplication.getLayerManager().removeActiveLayerChangeListener(this);
        }

        driveAction.frame = newFrame;
        driveAction.updateEnabledState();

        if (newFrame != null) {
            MainApplication.getLayerManager().addLayerChangeListener(this);
            MainApplication.getLayerManager().addActiveLayerChangeListener(this);
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        driveAction.currentLayer = MainApplication.getLayerManager().getActiveLayer();
        driveAction.updateEnabledState();
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        driveAction.updateEnabledState();
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        driveAction.updateEnabledState();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }
}
