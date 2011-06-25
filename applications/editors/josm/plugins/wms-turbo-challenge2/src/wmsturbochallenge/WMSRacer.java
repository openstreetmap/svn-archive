/*
 * GPLv2 or 3, Copyright (c) 2010  Andrzej Zaborowski
 *
 * This is the main class for the game plugin.
 */
package wmsturbochallenge;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.Main;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.ActionEvent;

public class WMSRacer extends Plugin implements LayerChangeListener {
    public WMSRacer(PluginInformation info) {
        super(info);
        driveAction.updateEnabledState();

        JMenu toolsMenu = Main.main.menu.toolsMenu;
        toolsMenu.addSeparator();
        toolsMenu.add(new JMenuItem(driveAction));
    }

    /* Rather than add an action or main menu entry we should add
     * an entry in the new layer's context menus in layerAdded
     * but there doesn't seem to be any way to do that :( */
    protected class DriveAction extends JosmAction {
        public MapFrame frame = null;
        public Layer currentLayer = null;
        protected Layer groundLayer = null;

        public DriveAction() {
            super(tr("Go driving"), "wmsracer",
                    tr("Drive a race car on this layer"),
                    null, true);
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent ev) {
            if (groundLayer == null ||
                    !groundLayer.isBackgroundLayer())
                return;

            new GameWindow(groundLayer);
        }

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
            for (Layer l : frame.mapView.getAllLayers())
                if (l.isBackgroundLayer()) {
                    groundLayer = l;
                    setEnabled(true);
                    return;
                }

            groundLayer = null;
            setEnabled(false);
        }
    }

    protected DriveAction driveAction = new DriveAction();

    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame != null)
            oldFrame.mapView.removeLayerChangeListener(this);

        driveAction.frame = newFrame;
        driveAction.updateEnabledState();

        if (newFrame != null)
            newFrame.mapView.addLayerChangeListener(this);
    }

    /* LayerChangeListener methods */
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        driveAction.currentLayer = newLayer;
        driveAction.updateEnabledState();
    }

    public void layerAdded(Layer newLayer) {
        driveAction.updateEnabledState();
    }

    public void layerRemoved(Layer oldLayer) {
        driveAction.updateEnabledState();
    }
}
