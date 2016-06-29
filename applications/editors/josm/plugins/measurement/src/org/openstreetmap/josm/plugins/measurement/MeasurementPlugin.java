// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.measurement;
/// @author Raphael Mack <osm@raphael-mack.de>
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
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

public class MeasurementPlugin extends Plugin {

    private IconToggleButton btn;
    private MeasurementMode mode;
    protected static MeasurementDialog measurementDialog;
    protected static MeasurementLayer currentLayer;

    public MeasurementPlugin(PluginInformation info) {
        super(info);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            newFrame.addToggleDialog(measurementDialog = new MeasurementDialog());
            mode = new MeasurementMode(newFrame, "measurement", tr("measurement mode"));
            btn = new IconToggleButton(mode);
            btn.setVisible(true);
            newFrame.addMapMode(btn);
        } else {
            btn = null;
            mode = null;
            measurementDialog = null;
        }
    }

    public static MeasurementLayer getCurrentLayer() {
        if (currentLayer == null) {
            currentLayer = new MeasurementLayer(tr("Measurements"));
            Main.getLayerManager().addLayer(currentLayer);
            final ActiveLayerChangeListener activeListener = new ActiveLayerChangeListener() {
                @Override
                public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
                    Layer newLayer = Main.getLayerManager().getActiveLayer();
                    if (newLayer instanceof MeasurementLayer)
                        MeasurementPlugin.currentLayer = (MeasurementLayer)newLayer;
                }
            };
            Main.getLayerManager().addActiveLayerChangeListener(activeListener);
            Main.getLayerManager().addLayerChangeListener(new LayerChangeListener(){
                @Override
                public void layerAdded(LayerAddEvent e) {
                    // Do nothing
                }

                @Override
                public void layerRemoving(LayerRemoveEvent e) {
                    Layer oldLayer = e.getRemovedLayer();
                    if (oldLayer != null && oldLayer == currentLayer) {
                        Main.getLayerManager().removeActiveLayerChangeListener(activeListener);
                        Main.getLayerManager().removeLayerChangeListener(this);
                    }
                }

                @Override
                public void layerOrderChanged(LayerOrderChangeEvent e) {
                    // Do nothing
                }
            });
        }
        return currentLayer;
    }
}
