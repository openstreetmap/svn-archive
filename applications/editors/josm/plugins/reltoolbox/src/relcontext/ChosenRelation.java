package relcontext;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.*;
import org.openstreetmap.josm.data.osm.event.*;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Chosen relation; is used for all actions and is highlighted on the map.
 *
 * @author Zverik
 */
public class ChosenRelation implements EditLayerChangeListener, MapViewPaintable, DataSetListener {
    protected Relation chosenRelation = null;
    private Set<ChosenRelationListener> chosenRelationListeners = new HashSet<ChosenRelationListener>();

    public void set( Relation rel ) {
        if( rel == chosenRelation || (rel != null && chosenRelation != null && rel.equals(chosenRelation)) ) {
            return; // new is the same as old
        }
        Relation oldRel = chosenRelation;
        chosenRelation = rel;
        analyse();
        Main.map.mapView.repaint();
        fireRelationChanged(oldRel);
    }

    protected void fireRelationChanged( Relation oldRel ) {
        for( ChosenRelationListener listener : chosenRelationListeners )
            listener.chosenRelationChanged(oldRel, chosenRelation);
    }

    public Relation get() {
        return chosenRelation;
    }

    public void clear() {
        set(null);
    }

    public boolean isSame( Object r ) {
        if( r == null )
            return chosenRelation == null;
        else if( !(r instanceof Relation) )
            return false;
        else
            return chosenRelation != null && r.equals(chosenRelation);
    }
    
    private final static String[] MULTIPOLYGON_TYPES = new String[] {
        "multipolygon", "boundary"
    };

    /**
     * Check if the relation type assumes all ways inside it form a multipolygon.
     */
    public boolean isMultipolygon() {
        return isMultipolygon(chosenRelation);
    }

    public static boolean isMultipolygon( Relation r ) {
        if( r == null )
            return false;
        String type = r.get("type");
        if( type == null )
            return false;
        for( String t : MULTIPOLYGON_TYPES )
            if( t.equals(type) )
                return true;
        return false;
    }

    public int getSegmentsCount() {
        return 0;
    }

    public int getCirclesCount() {
        return 0;
    }

    protected void analyse() {
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
        System.out.println("editLayerChanged() oldLayer=" + oldLayer + ", newLayer=" + newLayer);
        clear();
        if( newLayer != null && oldLayer == null ) {
            Main.map.mapView.addTemporaryLayer(this);
        } else if( oldLayer != null ) {
            Main.map.mapView.removeTemporaryLayer(this);
        }
    }

    public void paint( Graphics2D g, MapView mv, Bounds bbox ) {
        if( chosenRelation == null ) {
            return;
        }

        OsmDataLayer dataLayer = Main.map.mapView.getEditLayer();
        float opacity = dataLayer == null ? 0.0f : !dataLayer.isVisible() ? 0.0f : (float)dataLayer.getOpacity();
        if( opacity < 0.01 )
            return;

        Stroke oldStroke = g.getStroke();
        Composite oldComposite = g.getComposite();
        g.setColor(Color.yellow);
        g.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f * opacity));
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
                        p = mv.getPoint(way.getNode(i));
                        b.lineTo(p.x, p.y);
                    }
                    g.draw(b);
                }
            } else if( element.getType() == OsmPrimitiveType.RELATION ) {
                // todo: draw all relation members (recursion?)
            }
            // todo: closedway, multipolygon - ?
        }
        g.setStroke(oldStroke);
        g.setComposite(oldComposite);
    }

    public void relationMembersChanged( RelationMembersChangedEvent event ) {
        if( chosenRelation != null && event.getRelation().equals(chosenRelation) )
            fireRelationChanged(chosenRelation);
    }
    
    public void tagsChanged( TagsChangedEvent event ) {
        if( chosenRelation != null && event.getPrimitive().equals(chosenRelation) )
            fireRelationChanged(chosenRelation);
    }

    public void dataChanged( DataChangedEvent event ) {
        if( chosenRelation != null )
            fireRelationChanged(chosenRelation);
    }

    public void primtivesRemoved( PrimitivesRemovedEvent event ) {
        if( chosenRelation != null && event.getPrimitives().contains(chosenRelation) )
            clear();
    }

    public void wayNodesChanged( WayNodesChangedEvent event ) {
        if( chosenRelation != null )
            fireRelationChanged(chosenRelation); // download incomplete primitives doesn't cause dataChanged event
    }

    public void primtivesAdded( PrimitivesAddedEvent event ) {}
    public void nodeMoved( NodeMovedEvent event ) {}
    public void otherDatasetChange( AbstractDatasetChangedEvent event ) {}
}
