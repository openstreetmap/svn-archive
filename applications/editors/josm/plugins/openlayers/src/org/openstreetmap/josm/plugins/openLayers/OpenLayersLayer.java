package org.openstreetmap.josm.plugins.openLayers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.mozilla.javascript.NativeArray;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Class that displays a OpenLayers layer.
 *
 * @author Francisco R. Santos <frsantos@gmail.com>
 *
 */
public class OpenLayersLayer extends Layer implements PreferenceChangedListener,
    PropertyChangeListener, MyHtmlBlockPanel.ViewUpdateListener {

    private Browser browser;

    /**
     * Creates the layer
     */
    public OpenLayersLayer() {
        super("OpenLayers");

        this.browser = new Browser(OpenLayersPlugin.pluginDir + "yahoo.html", this);

        if( Main.map != null )
        {
            LatLon bottomLeft = Main.map.mapView.getLatLon(0,Main.map.mapView.getHeight());
            LatLon topRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
            browser.executeAsyncScript("zoomMapToExtent(" + bottomLeft.lon() + ","
            + bottomLeft.lat() + "," + topRight.lon() + "," + topRight.lat() + ")");
        }
    }

    /**
     * Draws current map onto the graphics
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        setSize(Main.map.mapView.getSize());
        browser.paint(g);
    }

    /**
     * Sets the size of the layer
     */
    public void setSize(Dimension dim) {
        browser.setSize(dim);
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("OpenLayers");
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Component[] getMenuEntries() {
        return new Component[] {
            new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
            new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
            new JSeparator(),
            // color,
            new JMenuItem(new RenameLayerAction(getAssociatedFile(), this)),
            new JSeparator(),
            new JMenuItem(new LayerListPopup.InfoAction(this)) };
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    @Override
    public void destroy() {
        Main.pref.listener.remove(this);

        if( Main.map != null )
            Main.map.mapView.removePropertyChangeListener(this);

        OpenLayersPlugin.layer = null;
        StorageManager.flush();
    }

    public void preferenceChanged(String key, String newValue) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( !isVisible() )
            return;

            String prop = evt.getPropertyName();
        if ("center".equals(prop) || "scale".equals(prop)) {
            zoomToMapView();
        }
    }

    public void zoomToMapView()
    {
        LatLon bottomLeft = Main.map.mapView.getLatLon(0,Main.map.mapView.getHeight());
        LatLon topRight = Main.map.mapView.getLatLon(Main.map.mapView.getWidth(), 0);
        Object value = browser.executeScript("zoomMapToExtent(" + bottomLeft.lon() + "," + bottomLeft.lat() + "," + topRight.lon() + "," + topRight.lat() + ")");
        if (value != null && false) {
            // TODO: should not be touching Main.map.mapView here as this
            // causes endless recurrency - should do the scalling ourselves

            // Get actual extent from browser
            NativeArray array = (NativeArray)value;
            double left   = ((Double)array.get(0, null)).doubleValue();
            double bottom = ((Double)array.get(1, null)).doubleValue();
            double right  = ((Double)array.get(2, null)).doubleValue();
            double top    = ((Double)array.get(3, null)).doubleValue();
            bottomLeft = new LatLon(bottom, left);
            topRight   = new LatLon(top, right);

            BoundingXYVisitor v = new BoundingXYVisitor();
            v.visit(Main.proj.latlon2eastNorth(bottomLeft));
            v.visit(Main.proj.latlon2eastNorth(topRight));
            System.out.println("Recalculating position (" + left + "," + bottom + "," + right + "," + top + ")");
            Main.map.mapView.recalculateCenterScale(v);
        }
    }

    public void region_update(int x, int y, int w, int h) {
        String status = browser.getStatus();

        org.openstreetmap.josm.Main.map.mapView.repaint(x, y, w, h);

        org.openstreetmap.josm.Main.map.statusLine.setHelpText(status);
    }
}
