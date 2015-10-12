package org.openstreetmap.josm.plugins.tofix;

import java.util.List;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.tofix.bean.items.ItemUnconnectedBean;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author ruben
 */

public class TofixDraw {

    ItemUnconnectedBean itemBean = null;

    public static void draw_Node(final TofixLayer tofixLayer, LatLon latLon) {
        MapView mv = Main.map.mapView;
        Bounds bounds = null;
        if (latLon.isOutSideWorld()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Can not find outside of the world."));
            return;
        }
        BoundingXYVisitor v = new BoundingXYVisitor();

        v.visit(new Bounds(latLon.toBBox(0.0007).toRectangle()));
        Main.map.mapView.zoomTo(v);
        if (!Main.map.mapView.hasLayer(tofixLayer)) {
            mv.addLayer(tofixLayer);
            tofixLayer.add_Node(latLon);
        } else {
            tofixLayer.add_Node(latLon);
        }
    }

    public static void draw_line(final TofixLayer tofixLayer, LatLon latLon, List<List<Node>> list_nodes) {
        MapView mv = Main.map.mapView;
        Bounds bounds = null;
        if (latLon.isOutSideWorld()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Can not find outside of the world."));
            return;
        }
        BoundingXYVisitor v = new BoundingXYVisitor();
        v.visit(new Bounds(latLon.toBBox(0.0007).toRectangle()));
        Main.map.mapView.zoomTo(v);
        if (!Main.map.mapView.hasLayer(tofixLayer)) {
            mv.addLayer(tofixLayer);
            tofixLayer.add_Line(list_nodes);
        } else {
            tofixLayer.add_Line(list_nodes);
        }
    }

    public static void draw_nodes(final TofixLayer tofixLayer, LatLon latLon, List<Node> list_nodes) {
        MapView mv = Main.map.mapView;
        Bounds bounds = null;
        if (latLon.isOutSideWorld()) {
            JOptionPane.showMessageDialog(Main.parent, tr("Can not find outside of the world."));
            return;
        }
        BoundingXYVisitor v = new BoundingXYVisitor();
        v.visit(new Bounds(latLon.toBBox(0.0007).toRectangle()));
        Main.map.mapView.zoomTo(v);
        if (!Main.map.mapView.hasLayer(tofixLayer)) {
            mv.addLayer(tofixLayer);
            tofixLayer.add_Nodes(list_nodes);
        } else {
            tofixLayer.add_Nodes(list_nodes);
        }
    }
}
