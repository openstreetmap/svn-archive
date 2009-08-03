/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.DateUtils;
import org.openstreetmap.josm.tools.ImageProvider;


public class EditGpxLayer extends Layer {

    private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(EditGpxPlugin.class.getResource("/images/editgpx_layer.png")));
    private DataSet dataSet;
    private GPXLayerImportAction layerImport;

    public EditGpxLayer(String str, DataSet ds) {
        super(str);
        dataSet = ds;
        layerImport = new GPXLayerImportAction(dataSet);
    }

    /**
     * check if dataSet is empty
     * if so show import dialog to user
     */
    public void initializeImport() {
        try {
            if(dataSet.nodes.isEmpty() ) {
                layerImport.activateImport();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Component[] getMenuEntries() {
        return new Component[] {
            new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
            new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
            new JSeparator(),
            new JMenuItem(layerImport),
            new JMenuItem(new ConvertToGpxLayerAction()),
            new JMenuItem(new ConvertToAnonTimeGpxLayerAction()),
            new JSeparator(),
            new JMenuItem(new LayerListPopup.InfoAction(this))};
    }

    @Override
    public String getToolTipText() {
        return tr("Layer for editing GPX tracks");
    }

    @Override
    public boolean isMergable(Layer other) {
        // TODO
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
        // TODO
    }

    @Override
    public void paint(Graphics g, MapView mv) {
        g.setColor(Color.yellow);

        //don't iterate through dataSet whiling making changes
        synchronized(layerImport.importing) {
            for(Node n: dataSet.nodes) {
                if (!n.deleted) {
                    Point pnt = Main.map.mapView.getPoint(n.getEastNorth());
                    g.drawOval(pnt.x - 2, pnt.y - 2, 4, 4);
                }
            }
        }
    }


    public void reset(){
        //TODO implement a reset
    }


    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // TODO Auto-generated method stub
    }


    /**
     * convert a DataSet to GPX
     *
     * @param boolean anonTime If true set all time and date in GPX to 01/01/1970 00:00 ?
     * @return GPXData
     */
    private GpxData toGpxData(boolean anonTime) {
        GpxData gpxData = new GpxData();
        HashSet<Node> doneNodes = new HashSet<Node>();
        //add all ways
        for (Way w : dataSet.ways) {
            if (w.incomplete || w.deleted) continue;
            GpxTrack trk = new GpxTrack();
            gpxData.tracks.add(trk);

            if (w.get("name") != null)
                trk.attr.put("name", w.get("name"));

            ArrayList<WayPoint> trkseg = null;
            for (Node n : w.nodes) {
                if (n.incomplete || n.deleted) {
                    trkseg = null;
                    continue;
                }

                Date tstamp = n.getTimestamp();

                if (trkseg == null) {
                    trkseg = new ArrayList<WayPoint>();
                    trk.trackSegs.add(trkseg);
                }
                doneNodes.add(n);

                WayPoint wpt = new WayPoint(n.getCoor());
                if (anonTime) {
                    wpt.attr.put("time", "1970-01-01T00:00:00Z");
                } else {
                    wpt.attr.put("time", DateUtils.fromDate(tstamp));
                }
                wpt.setTime();

                trkseg.add(wpt);
            }
        }

        // add nodes as waypoints
        for (Node n : dataSet.nodes) {
            if (n.incomplete || n.deleted || doneNodes.contains(n)) continue;

            Date tstamp = n.getTimestamp();

            WayPoint wpt = new WayPoint(n.getCoor());
            if (anonTime) {
                wpt.attr.put("time", "1970-01-01T00:00:00Z");
            } else {
                wpt.attr.put("time", DateUtils.fromDate(tstamp));
            }
            wpt.setTime();

            if (n.keys != null && n.keys.containsKey("name")) {
                wpt.attr.put("name", n.keys.get("name"));
            }
            gpxData.waypoints.add(wpt);
        }
        return gpxData;
    }

    //context item "Convert to GPX layer"
    public class ConvertToGpxLayerAction extends AbstractAction {
        public ConvertToGpxLayerAction() {
            super(tr("Convert to GPX layer"), ImageProvider.get("converttogpx"));
        }
        public void actionPerformed(ActionEvent e) {
            Main.main.addLayer(new GpxLayer(toGpxData(false), tr("Converted from: {0}", getName())));
            Main.main.removeLayer(EditGpxLayer.this);
        }
    }

    //context item "Convert to GPX layer with anonymised time"
    public class ConvertToAnonTimeGpxLayerAction extends AbstractAction {
        public ConvertToAnonTimeGpxLayerAction() {
            super(tr("Convert to GPX layer with anonymised time"), ImageProvider.get("converttogpx"));
        }
        public void actionPerformed(ActionEvent e) {
            Main.main.addLayer(new GpxLayer(toGpxData(true), tr("Converted from: {0}", getName())));
            Main.main.removeLayer(EditGpxLayer.this);
        }
    }
}
