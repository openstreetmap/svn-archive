package org.openstreetmap.josm.plugins.czechaddress;

import java.util.Collection;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;

/**
 * Collection of utilities for manipulating the JOSM map.
 *
 * <p>This set of state-less utilities, which can be handy in all parts of
 * the plugin. Therefore all methods are {@code static} and the class is
 * {@code abstract}.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public abstract class MapUtils {

    static final double NODE_ZOOM_LEVEL = 0.00000007;


    /**
     * Selects and zooms the JOSM viewport to given primitives.
     *
     * <p>It does so by calculating the center of given primitives and
     * then it zooms to it.</p>
     *
     * <p><b>WARNING and TODO:</b> The method {@code zoomTo()} currently
     * checks for damaged {@link Node}s, whose {@code eastNorth} is set to
     * null. This property is not accessed in this method and therefore
     * no checking is done. However the "mad GUI" problem may still arise.
     * Therefore please be careful.</p>
     *
     * @see BoundingXYVisitor
     * @see MapView
     */
    public static void zoomToMany(Collection<OsmPrimitive> primitives) {
        BoundingXYVisitor visitor = new BoundingXYVisitor();
        for (OsmPrimitive op : primitives) {
            if (op instanceof Node)
                ((Node) op).visit(visitor);

            else if (op instanceof Way)
                ((Way) op).visit(visitor);
        }
        Main.map.mapView.zoomTo(
                visitor.min.interpolate(visitor.max, 0.5),
                NODE_ZOOM_LEVEL);
        Main.ds.setSelected(primitives);
    }

    /**
     * Selects and zooms the JOSM viewport to given primitive.
     *
     * <p><b>TODO:</b> There is an error in JOSM, which makes the whole
     * GUI totally mad if we zoom to a {@link Node}, whose {@code eastNorth}
     * is set null. Currently zooming to such a node is ignored, but the
     * question is where so such damaged nodes come from?</p>
     *
     * @see BoundingXYVisitor
     * @see MapView
     */
    public static void zoomTo(OsmPrimitive primitive) {
        BoundingXYVisitor visitor = new BoundingXYVisitor();

        if (primitive instanceof Node && ((Node) primitive).eastNorth != null)
            Main.map.mapView.zoomTo(((Node) primitive).eastNorth, NODE_ZOOM_LEVEL);

        else if (primitive instanceof Way) {
            ((Way) primitive).visit(visitor);
            Main.map.mapView.zoomTo(
                    visitor.min.interpolate(visitor.max, 0.5),
                    NODE_ZOOM_LEVEL);
        }

        Main.ds.setSelected(primitive);
    }
}
