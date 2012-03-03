/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxData;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrack;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxTrackSegment;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxWayPoint;
import org.openstreetmap.josm.tools.ImageProvider;


public class EditGpxLayer extends Layer {

    private static Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(EditGpxPlugin.class.getResource("/images/editgpx_layer.png")));
    public final EditGpxData data;
    private GPXLayerImportAction layerImport;

    public EditGpxLayer(String str, EditGpxData gpxData) {
        super(str);
        data = gpxData;
        layerImport = new GPXLayerImportAction(data);
    }

    /**
     * check if dataSet is empty
     * if so show import dialog to user
     */
    public void initializeImport() {
        try {
            if(data.isEmpty()) {
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
    public Action[] getMenuEntries() {
        return new Action[] {
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                layerImport,
                new ConvertToGpxLayerAction(),
                new ConvertToAnonTimeGpxLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};
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
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        g.setColor(Color.yellow);

        //don't iterate through dataSet whiling making changes
        synchronized(layerImport.importing) {
            for (EditGpxTrack track: data.getTracks()) {
                for (EditGpxTrackSegment segment: track.getSegments()) {
                    for (EditGpxWayPoint wayPoint: segment.getWayPoints()) {
                        if (!wayPoint.isDeleted()) {
                            Point pnt = Main.map.mapView.getPoint(wayPoint.getCoor().getEastNorth());
                            g.drawOval(pnt.x - 2, pnt.y - 2, 4, 4);
                        }
                    }
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
        return data.createGpxData(anonTime);
    }

    //context item "Convert to GPX layer"
    public class ConvertToGpxLayerAction extends AbstractAction {
        public ConvertToGpxLayerAction() {
            super(tr("Convert to GPX layer"), ImageProvider.get("converttogpx"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (Main.map.mapMode instanceof EditGpxMode) {
                if (!Main.map.selectSelectTool(false)) {
                    Main.map.selectZoomTool(false); // Select tool might not be support of active layer, zoom is always supported
                }
            }
            Main.main.addLayer(new GpxLayer(toGpxData(false), tr("Converted from: {0}", getName())));
            Main.main.removeLayer(EditGpxLayer.this);
        }
    }

    //context item "Convert to GPX layer with anonymised time"
    public class ConvertToAnonTimeGpxLayerAction extends AbstractAction {
        public ConvertToAnonTimeGpxLayerAction() {
            super(tr("Convert to GPX layer with anonymised time"), ImageProvider.get("converttogpx"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (Main.map.mapMode instanceof EditGpxMode) {
                if (!Main.map.selectSelectTool(false)) {
                    Main.map.selectZoomTool(false); // Select tool might not be support of active layer, zoom is always supported
                }
            }
            Main.main.addLayer(new GpxLayer(toGpxData(true), tr("Converted from: {0}", getName())));
            Main.main.removeLayer(EditGpxLayer.this);
        }
    }
}
