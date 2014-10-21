// License: GPL. Copyright (C) 2012 Russell Edwards
package org.openstreetmap.josm.plugins.gpsblam;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;

class GPSBlamLayer extends Layer {

    private final Collection<GPSBlamMarker> blamMarkers;

    GPSBlamLayer(String name) {
        super(name);
        blamMarkers = new LinkedList<>();
    }

    private static final Icon ICON = new ImageIcon(Toolkit.getDefaultToolkit().createImage(
            GPSBlamPlugin.class.getResource("/images/gpsblam_layer.png")));

    @Override
    public Icon getIcon() {
        return ICON;
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
                new LayerListPopup.InfoAction(this)};
    }

    @Override
    public String getToolTipText() {
        return tr("GPS Blams");
    }

    @Override
    public boolean isMergable(Layer arg0) {
        return false;
    }

    @Override
    public void mergeFrom(Layer arg0) {
        // Do nothing
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        for (GPSBlamMarker blamMarker : blamMarkers){
            blamMarker.paint(g, mv);
        }
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        // Do nothing
    }

    void addBlamMarker(GPSBlamMarker blamMarker) {
        blamMarkers.add(blamMarker);
    }
}
