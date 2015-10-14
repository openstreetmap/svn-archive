package org.openstreetmap.josm.plugins.tofix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;

public class TofixLayer extends Layer implements ActionListener {

    LatLon latLon;
    List<List<Node>> list_list_nodes;
    List<Node> list_nodes;
    String type = "";

    public TofixLayer(String name) {
        super(name);
    }
    // private static final Icon icon = new ImageIcon("icontofix.png");
    final Collection<OsmPrimitive> points = Main.main.getInProgressSelection();

    @Override
    public Icon getIcon() {
        return ImageProvider.get("layer", "marker_small");
    }

    @Override
    public String getToolTipText() {
        return tr("Layer to draw OSM error");
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    public void add_Node(LatLon latLon) {
        type = "draw_node";
        this.latLon = latLon;
        Main.map.mapView.repaint();
    }

    public void add_Line(List<List<Node>> list_nodes) {
        type = "draw_line";
        this.list_list_nodes = list_nodes;
        Main.map.mapView.repaint();
    }

    public void add_Nodes(List<Node> list_nodes) {
        type = "draw_nodes";
        this.list_nodes = list_nodes;
        Main.map.mapView.repaint();
    }

    @Override
    public void paint(Graphics2D g, final MapView mv, Bounds bounds) {
        g.setColor(new Color(254, 30, 123));
        g.setStroke(new BasicStroke((float) 5));
        if (type.equals("draw_node")) {
            Point pnt = mv.getPoint(latLon);
            g.drawOval(pnt.x - 25, pnt.y - 25, 50, 50);
        } else if (type.equals("draw_line")) {
            for (List<Node> l_nodes : list_list_nodes) {
                for (int i = 0; i < l_nodes.size() - 1; i++) {
                    Point pnt1 = mv.getPoint(l_nodes.get(i).getCoor());
                    Point pnt2 = mv.getPoint(l_nodes.get(i + 1).getCoor());
                    g.drawLine(pnt1.x, pnt1.y, pnt2.x, pnt2.y);
                }
            }
        } else if (type.equals("draw_nodes")) {
            for (Node node : list_nodes) {
                Point pnt = mv.getPoint(node.getCoor());
                g.drawOval(pnt.x - 5, pnt.y - 5, 10, 10);
            }
        }

//add for others ways
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        // nothing to do here
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
            LayerListDialog.getInstance().createShowHideLayerAction(),
            SeparatorLayerAction.INSTANCE,
            SeparatorLayerAction.INSTANCE,
            new LayerListPopup.InfoAction(this)};
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showConfirmDialog(null, e.getSource());
    }

    @Override
    public void mergeFrom(Layer layer) {
    }
}
