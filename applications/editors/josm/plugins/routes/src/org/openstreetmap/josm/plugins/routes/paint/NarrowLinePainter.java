package org.openstreetmap.josm.plugins.routes.paint;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.util.BitSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.routes.ConvertedWay;
import org.openstreetmap.josm.plugins.routes.RouteDefinition;
import org.openstreetmap.josm.plugins.routes.RouteLayer;

public class NarrowLinePainter extends AbstractLinePainter {

    private static final float LINE_WIDTH = 5;
    private final RouteLayer layer;

    public NarrowLinePainter(RouteLayer layer) {
        this.layer = layer;
    }

    public void drawWay(ConvertedWay way, MapView mapView, Graphics2D g) {
        List<Node> nodes = way.getNodes();
        BitSet routes = way.getRoutes();

        if (nodes.size() < 2) {
            return;
        }

        //double totalWidth = LINE_WIDTH + (colors.size() - 1) * 4;
        //double width = totalWidth / colors.size();
        //double shift = -totalWidth / 2 + width / 2;
        double width = LINE_WIDTH;
        double shift = - (LINE_WIDTH * routes.cardinality()) / 2 + width / 2;

        for (int k=0; k<routes.length(); k++) {

            if (!routes.get(k)) {
                continue;
            }

            RouteDefinition route = layer.getRoutes().get(k);

            g.setColor(route.getColor());
            g.setStroke(new BasicStroke((float) width));

            g.draw(getPath(g, mapView, nodes, shift));

            shift += width + 2;
        }
    }

}
