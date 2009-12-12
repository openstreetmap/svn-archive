/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Provides an editable GPX layer. Editable layer here means the deletion of points is supported.
 * This plugin can be used to prepare tracks for upload to OSM eg. delete uninteresting parts
 * of the track.
 * Additionally while converting the track back to a normal GPX layer the time can be made
 * anonymous. This feature sets all time stamps to 1970-01-01 00:00.
 *
 * TODO:
 * - BUG: when importing eGpxLayer is shown as RawGpxLayer??
 * - BUG: after deletion of layer not all data is deleted (eg dataset)
 * - implement reset if user made mistake while marking
 *
 *
 */
public class EditGpxPlugin extends Plugin {

    private IconToggleButton btn;
    private EditGpxMode mode;
    protected static EditGpxLayer eGpxLayer;
    protected static DataSet dataSet;
    public static boolean active = false;

    public EditGpxPlugin() {
        dataSet = new DataSet();
        mode = new EditGpxMode(Main.map, "editgpx", tr("edit gpx tracks"), dataSet);

        btn = new IconToggleButton(mode);
        btn.setVisible(true);
    }

    /**
     * initialize button. if button is pressed create new layer.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(oldFrame == null && newFrame != null) {
            mode.setFrame(newFrame);

            if(Main.map != null)
                Main.map.addMapMode(btn);

            active = btn.isSelected();

            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    active = btn.isSelected();
                    if (active) {
                        Main.worker.execute(new Runnable() {
                            public void run() {
                                updateLayer();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * create new layer, add listeners and try importing gpx data.
     */
    private void updateLayer() {
        if(eGpxLayer == null) {
            eGpxLayer = new EditGpxLayer(tr("EditGpx"), dataSet);
            Main.main.addLayer(eGpxLayer);
            MapView.addLayerChangeListener(new LayerChangeListener(){

                public void activeLayerChange(final Layer oldLayer, final Layer newLayer) {
                    if(newLayer instanceof EditGpxLayer)
                        EditGpxPlugin.eGpxLayer = (EditGpxLayer)newLayer;
                }

                public void layerAdded(final Layer newLayer) {
                }

                public void layerRemoved(final Layer oldLayer) {
                    if(oldLayer == eGpxLayer) {
                        eGpxLayer = null;
                        //dataSet = new DataSet();
                        MapView.removeLayerChangeListener(this);
                    }
                }
            });

            eGpxLayer.initializeImport();
        }
        Main.map.mapView.repaint();
    }

    public static ImageIcon loadIcon(String name) {
        URL url = EditGpxPlugin.class.getResource("/images/editgpx.png");
        return new ImageIcon(url);
    }
}
