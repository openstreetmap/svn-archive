package org.openstreetmap.josm.plugins.turnrestrictions.list;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.DefaultListSelectionModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * <p>This is the list model for the list of turn restrictions in the current data set.</p>
 * 
 * <p>The model is a {@link EditLayerChangeListener}. It initializes itself from the data set of
 * the current edit layer.</p>
 * 
 * <p>The model is a {@link DataSetListener}. It updates itself to reflect the list of turn
 * restrictions in the current data set.</p> 
 *
 */
public class TurnRestrictionsInDatasetListModel extends TurnRestrictionsListModel implements EditLayerChangeListener, DataSetListener {
    private static final Logger logger = Logger.getLogger(TurnRestrictionsInDatasetListModel.class.getName());
    
    public TurnRestrictionsInDatasetListModel(
            DefaultListSelectionModel selectionModel) {
        super(selectionModel);
    }
    
    /**
     * Filters the list of turn restrictions from a collection of OSM primitives.
     * 
     * @param primitives the primitives 
     * @return the list of turn restrictions 
     */
    protected List<Relation> filterTurnRestrictions(Collection<? extends OsmPrimitive> primitives) {
        List<Relation> ret = new LinkedList<Relation>();
        if (primitives == null) return ret;
        for(OsmPrimitive p: primitives){
            if (!isTurnRestriction(p)) continue;
            ret.add((Relation)p);
        }
        return ret;
    }
    
    /* --------------------------------------------------------------------------- */
    /* interface EditLayerChangeListener                                           */
    /* --------------------------------------------------------------------------- */
    public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
        if (newLayer == null) {
            setTurnRestrictions(null);
            return;
        }
        List<Relation> turnRestrictions = new LinkedList<Relation>();
        for (Relation r: newLayer.data.getRelations()) {
            if (isValid(r) && isTurnRestriction(r)) {
                turnRestrictions.add(r);
            }
        }
        setTurnRestrictions(turnRestrictions);
    }
    
    /* --------------------------------------------------------------------------- */
    /* interface DataSetListener                                                   */
    /* --------------------------------------------------------------------------- */   
    public void dataChanged(DataChangedEvent event) {       
        OsmDataLayer layer = Main.map.mapView.getEditLayer();
        if (layer == null) {
            setTurnRestrictions(null);
        } else {
            List<Relation> turnRestrictions = filterTurnRestrictions(layer.data.getRelations());
            setTurnRestrictions(turnRestrictions);
        }
    }

    public void primitivesAdded(PrimitivesAddedEvent event) {
        List<Relation> turnRestrictions = filterTurnRestrictions(event.getPrimitives());
        if (!turnRestrictions.isEmpty()) {
            addTurnRestrictions(turnRestrictions);
        }
    }

    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        List<Relation> turnRestrictions = filterTurnRestrictions(event.getPrimitives());
        if (!turnRestrictions.isEmpty()) {
            removeTurnRestrictions(turnRestrictions);
        }
    }

    public void relationMembersChanged(RelationMembersChangedEvent event) {
        List<Relation> turnRestrictions = filterTurnRestrictions(event.getPrimitives());
        if (!turnRestrictions.isEmpty()) {
            List<Relation> sel = getSelectedTurnRestrictions();
            for(Relation tr: turnRestrictions) {    
                // enforce a repaint of the respective turn restriction
                int idx = getTurnRestrictionIndex(tr);
                fireContentsChanged(this, idx,idx);
            }
            setSelectedTurnRestrictions(sel);
        }
    }

    public void tagsChanged(TagsChangedEvent event) {
        List<Relation> turnRestrictions = filterTurnRestrictions(event.getPrimitives());
        if (!turnRestrictions.isEmpty()) {
            List<Relation> sel = getSelectedTurnRestrictions();
            for(Relation tr: turnRestrictions) {    
                // enforce a repaint of the respective turn restriction
                int idx = getTurnRestrictionIndex(tr);
                fireContentsChanged(this, idx,idx);
            }
            setSelectedTurnRestrictions(sel);
        }       
    }

    public void wayNodesChanged(WayNodesChangedEvent event) {/* ignore */}
    public void nodeMoved(NodeMovedEvent event) {/* ignore */}
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {/* ignore */}
}
