package org.openstreetmap.josm.plugins.turnrestrictions.list;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * This is the list model for the list of turn restrictions related to OSM
 * objects in the current selection.
 */
public class TurnRestrictionsInSelectionListModel extends TurnRestrictionsListModel implements EditLayerChangeListener, SelectionChangedListener {
    //private static final Logger logger = Logger.getLogger(TurnRestrictionsInSelectionListModel.class.getName());
    
    public TurnRestrictionsInSelectionListModel(
            DefaultListSelectionModel selectionModel) {
        super(selectionModel);
    }
    
    /**
     * Initializes the model with the turn restrictions the primitives in 
     * {@code selection} participate.
     * 
     * @param selection the collection of selected primitives
     */
    public void initFromSelection(Collection<? extends OsmPrimitive> selection) {
        Set<Relation> turnRestrictions = new HashSet<Relation>();
        if (selection == null) return;
        for (OsmPrimitive p: selection) {
            for (OsmPrimitive parent: p.getReferrers()) {
                if (isTurnRestriction(parent))
                    turnRestrictions.add((Relation)parent);
            }
        }
        setTurnRestrictions(turnRestrictions);
    }

    /* --------------------------------------------------------------------------- */
    /* interface EditLayerChangeListener                                           */
    /* --------------------------------------------------------------------------- */
    public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
        if (newLayer == null) {
            setTurnRestrictions(null);
            return;
        }
        initFromSelection(newLayer.data.getSelected());
    }
    
    /* --------------------------------------------------------------------------- */
    /* interface SelectionChangedListener                                          */
    /* --------------------------------------------------------------------------- */   
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        initFromSelection(newSelection);
    }
}
