// License: GPL. For details, see LICENSE file.
package relcontext;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * Chosen relation; is used for all actions and is highlighted on the map.
 *
 * @author Zverik
 */
public class ChosenRelation implements ActiveLayerChangeListener, MapViewPaintable, DataSetListener {
    protected Relation chosenRelation = null;
    private Set<ChosenRelationListener> chosenRelationListeners = new HashSet<>();

    public void set(Relation rel) {
        if (rel == chosenRelation || (rel != null && chosenRelation != null && rel.equals(chosenRelation)))
            return; // new is the same as old
        Relation oldRel = chosenRelation;
        chosenRelation = rel;
        analyse();
        MainApplication.getMap().mapView.repaint();
        fireRelationChanged(oldRel);
    }

    protected void fireRelationChanged(Relation oldRel) {
        for (ChosenRelationListener listener : chosenRelationListeners) {
            listener.chosenRelationChanged(oldRel, chosenRelation);
        }
    }

    public Relation get() {
        return chosenRelation;
    }

    public void clear() {
        set(null);
    }

    public boolean isSame(Object r) {
        if (r == null)
            return chosenRelation == null;
        else if (!(r instanceof Relation))
            return false;
        else
            return chosenRelation != null && r.equals(chosenRelation);
    }

    private static final String[] MULTIPOLYGON_TYPES = new String[] {
            "multipolygon", "boundary"
    };

    /**
     * Check if the relation type assumes all ways inside it form a multipolygon.
     * @return true if the relation type assumes all ways inside it form a multipolygon
     */
    public boolean isMultipolygon() {
        return isMultipolygon(chosenRelation);
    }

    public static boolean isMultipolygon(Relation r) {
        if (r == null)
            return false;
        String type = r.get("type");
        if (type == null)
            return false;
        for (String t : MULTIPOLYGON_TYPES) {
            if (t.equals(type))
                return true;
        }
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

    public void addChosenRelationListener(ChosenRelationListener listener) {
        chosenRelationListeners.add(listener);
    }

    public void removeChosenRelationListener(ChosenRelationListener listener) {
        chosenRelationListeners.remove(listener);
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        // todo: dim chosen relation when changing layer
        // todo: check this WTF!
        OsmDataLayer newLayer = MainApplication.getLayerManager().getEditLayer();
        clear();
        if (newLayer != null && e.getPreviousDataLayer() == null) {
            MainApplication.getMap().mapView.addTemporaryLayer(this);
        } else if (newLayer == null) {
            MainApplication.getMap().mapView.removeTemporaryLayer(this);
        }
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bbox) {
        if (chosenRelation == null)
            return;

        OsmDataLayer dataLayer = mv.getLayerManager().getEditLayer();
        float opacity = dataLayer == null ? 0.0f : !dataLayer.isVisible() ? 0.0f : (float) dataLayer.getOpacity();
        if (opacity < 0.01)
            return;

        Composite oldComposite = g.getComposite();
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(Color.yellow);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f * opacity));

        drawRelations(g, mv, bbox, chosenRelation, new HashSet<Relation>());

        g.setComposite(oldComposite);
        g.setStroke(oldStroke);
    }

    private void drawRelations(Graphics2D g, MapView mv, Bounds bbox, Relation rel, Set<Relation> visitedRelations) {
        if (!visitedRelations.contains(rel)) {
            visitedRelations.add(rel);
            for (OsmPrimitive element : rel.getMemberPrimitives()) {
                if (null != element.getType()) {
                    switch(element.getType()) {
                    case NODE:
                        Node node = (Node) element;
                        Point center = mv.getPoint(node);
                        g.drawOval(center.x - 4, center.y - 4, 9, 9);
                        break;
                    case WAY:
                        Way way = (Way) element;
                        if (way.getNodesCount() >= 2) {
                            GeneralPath b = new GeneralPath();
                            Point p = mv.getPoint(way.getNode(0));
                            b.moveTo(p.x, p.y);
                            for (int i = 1; i < way.getNodesCount(); i++) {
                                p = mv.getPoint(way.getNode(i));
                                b.lineTo(p.x, p.y);
                            }
                            g.draw(b);
                        }   break;
                    case RELATION:
                        Color oldColor = g.getColor();
                        g.setColor(Color.magenta);
                        drawRelations(g, mv, bbox, (Relation) element, visitedRelations);
                        g.setColor(oldColor);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void relationMembersChanged(RelationMembersChangedEvent event) {
        if (chosenRelation != null && event.getRelation().equals(chosenRelation)) {
            fireRelationChanged(chosenRelation);
        }
    }

    @Override
    public void tagsChanged(TagsChangedEvent event) {
        if (chosenRelation != null && event.getPrimitive().equals(chosenRelation)) {
            fireRelationChanged(chosenRelation);
        }
    }

    @Override
    public void dataChanged(DataChangedEvent event) {
        if (chosenRelation != null) {
            fireRelationChanged(chosenRelation);
        }
    }

    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        if (chosenRelation != null && event.getPrimitives().contains(chosenRelation)) {
            clear();
        }
    }

    @Override
    public void wayNodesChanged(WayNodesChangedEvent event) {
        if (chosenRelation != null) {
            fireRelationChanged(chosenRelation); // download incomplete primitives doesn't cause dataChanged event
        }
    }

    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {}

    @Override
    public void nodeMoved(NodeMovedEvent event) {}

    @Override
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {}
}
