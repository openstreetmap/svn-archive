package org.openstreetmap.josm.plugins.measurement;
/// @author Raphael Mack <osm@raphael-mack.de>
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;

public class MeasurementPlugin extends Plugin {

    private IconToggleButton btn;
    private MeasurementMode mode;
    protected static MeasurementDialog measurementDialog;
    protected static MeasurementLayer currentLayer;

    public MeasurementPlugin() {
        mode = new MeasurementMode(Main.map, "measurement", tr("measurement mode"));
        btn = new IconToggleButton(mode);
        btn.setVisible(true);
        measurementDialog = new MeasurementDialog();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null)
            newFrame.addToggleDialog(measurementDialog);
        if(Main.map != null)
            Main.map.addMapMode(btn);
    }

    public static MeasurementLayer getCurrentLayer(){
        if(currentLayer == null){
            currentLayer = new MeasurementLayer(tr("Measurements"));
            Main.main.addLayer(currentLayer);
            MapView.addLayerChangeListener(new LayerChangeListener(){
                public void activeLayerChange(final Layer oldLayer, final Layer newLayer) {
                    if(newLayer instanceof MeasurementLayer)
                        MeasurementPlugin.currentLayer = (MeasurementLayer)newLayer;
                }
                public void layerAdded(final Layer newLayer) {
                }
                public void layerRemoved(final Layer oldLayer) {
                	if (oldLayer != null && oldLayer == currentLayer)
                		MapView.removeLayerChangeListener(this);
                }
            });
        }
        return currentLayer;
    }
}
