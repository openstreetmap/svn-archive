package relcontext;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Chosen relation; is used for all actions and is highlighted on the map.
 *
 * @author Zverik
 */
public class ChosenRelation implements EditLayerChangeListener, MapViewPaintable {
    private Relation chosenRelation = null;
    private List<ChosenRelationListener> chosenRelationListeners = new ArrayList<ChosenRelationListener>();

    public void set( Relation rel ) {
        if( rel == chosenRelation || (rel != null && chosenRelation != null && rel.equals(chosenRelation)) ) {
            return; // new is the same as old
        }
        Relation oldRel = chosenRelation;
        chosenRelation = rel;
        analyse();
        for( ChosenRelationListener listener : chosenRelationListeners ) {
            listener.chosenRelationChanged(oldRel, chosenRelation);
        }
        return;
    }

    public Relation get() {
        return chosenRelation;
    }

    public void clear() {
        set(null);
    }

    public int getSegmentsCount() {
        return 0;
    }

    public int getCirclesCount() {
        return 0;
    }

    private void analyse() {
        // todo
    }

    public void addChosenRelationListener( ChosenRelationListener listener ) {
        chosenRelationListeners.add(listener);
    }

    public void removeChosenRelationListener( ChosenRelationListener listener ) {
        chosenRelationListeners.remove(listener);
    }

    public void editLayerChanged( OsmDataLayer oldLayer, OsmDataLayer newLayer ) {
        // todo: dim chosen relation when changing layer
        // todo: check this WTF!
        if( newLayer != null ) {
            Main.map.mapView.addTemporaryLayer(this);
        } else {
            Main.map.mapView.removeTemporaryLayer(this);
        }
    }

    public void paint( Graphics2D g, MapView mv, Bounds bbox ) {
        if( chosenRelation == null ) {
            return;
        }

        g.setColor(Color.yellow);
        g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        for( OsmPrimitive element : chosenRelation.getMemberPrimitives() ) {
            if( element.getType() == OsmPrimitiveType.NODE ) {
                Node node = (Node)element;
                Point center = mv.getPoint(node);
                g.drawOval(center.x - 4, center.y - 4, 9, 9);
            } else if( element.getType() == OsmPrimitiveType.WAY ) {
                Way way = (Way)element;
                if( way.getNodesCount() >= 2 ) {
                    GeneralPath b = new GeneralPath();
                    Point p = mv.getPoint(way.getNode(0));
                    b.moveTo(p.x, p.y);
                    for( int i = 1; i < way.getNodesCount(); i++ ) {
                        p = mv.getPoint(way.getNode(1));
                        b.lineTo(p.x, p.y);
                    }
                    g.draw(b);
                }
            } else if( element.getType() == OsmPrimitiveType.RELATION ) {
                // todo: draw all relation members (recursion?)
            }
            // todo: closedway, multipolygon - ?
        }
        g.setStroke(new BasicStroke(1)); // from building_tools; is it really needed?
    }
}
