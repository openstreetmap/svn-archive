package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
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
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * JosmSelectionListModel is the model for a list which displays the currently selected 
 * objects in the current edit layer.
 * 
 */
public class JosmSelectionListModel extends AbstractListModel implements EditLayerChangeListener, SelectionChangedListener, DataSetListener, PrimitiveIdListProvider{
    static private final Logger logger = Logger.getLogger(JosmSelectionListModel.class.getName());
    
    private final List<OsmPrimitive> selection = new ArrayList<OsmPrimitive>();
    private final DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
    private OsmDataLayer layer;

    /**
     * Constructor
     * 
     * @param selectionModel the selection model used in the list. Must not be null.
     * @param layer the layer this model is displaying the selection from. Must not be null.
     * @throws IllegalArgumentException thrown if {@code layer} is null
     */
    public JosmSelectionListModel(OsmDataLayer layer) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.layer = layer;
        setJOSMSelection(layer.data.getSelected());
    }

    public Object getElementAt(int index) {
        return selection.get(index);
    }

    public int getSize() {
        return selection.size();
    }

    /**
     * Replies the collection of OSM primitives currently selected in the view
     * of this model
     * 
     * @return the selected primitives
     */
    public Collection<OsmPrimitive> getSelected() {
        Set<OsmPrimitive> sel = new HashSet<OsmPrimitive>();
        for(int i=0; i< getSize();i++) {
            if (selectionModel.isSelectedIndex(i)) {
                sel.add(selection.get(i));
            }
        }
        return sel;
    }

    /**
     * Sets the OSM primitives to be selected in the view of this model
     * 
     * @param sel the collection of primitives to select
     */
    public void setSelected(Collection<OsmPrimitive> sel) {
        selectionModel.clearSelection();
        if (sel == null) return;
        for (OsmPrimitive p: sel){
            int i = selection.indexOf(p);
            if (i >= 0){
                selectionModel.addSelectionInterval(i, i);
            }
        }
    }

    @Override
    protected void fireContentsChanged(Object source, int index0, int index1) {
        Collection<OsmPrimitive> sel = getSelected();
        super.fireContentsChanged(source, index0, index1);
        setSelected(sel);
    }

    /**
     * Sets the collection of currently selected OSM objects
     * 
     * @param selection the collection of currently selected OSM objects
     */
    public void setJOSMSelection(Collection<? extends OsmPrimitive> selection) {
        Collection<OsmPrimitive> sel = getSelected();
        this.selection.clear();
        if (selection == null) {
            fireContentsChanged(this, 0, getSize());
            return;
        }
        this.selection.addAll(selection);
        fireContentsChanged(this, 0, getSize());       
        setSelected(sel);       
        // if the user selects exactly one primitive (i.e. a way), we automatically
        // select it in the list of selected JOSM objects too. 
        if (getSelected().isEmpty() && this.selection.size() == 1) {
        	setSelected(this.selection);
        }
    }

    /**
     * Triggers a refresh of the view for all primitives in {@code toUpdate}
     * which are currently displayed in the view
     * 
     * @param toUpdate the collection of primitives to update
     */
    public void update(Collection<? extends OsmPrimitive> toUpdate) {
        if (toUpdate == null) return;
        if (toUpdate.isEmpty()) return;
        Collection<OsmPrimitive> sel = getSelected();
        for (OsmPrimitive p: toUpdate){
            int i = selection.indexOf(p);
            if (i >= 0) {
                super.fireContentsChanged(this, i,i);
            }
        }
        setSelected(sel);
    }
        
    public ListSelectionModel getListSelectionModel() {
    	return selectionModel;
    }

    /* ------------------------------------------------------------------------ */
    /* interface EditLayerChangeListener                                        */
    /* ------------------------------------------------------------------------ */
    public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
        if (newLayer == null) {
            // don't show a JOSM selection if we don't have a data layer 
            setJOSMSelection(null);
        } else if (newLayer != layer){
            // don't show a JOSM selection if this turn restriction editor doesn't
            // manipulate data in the current data layer
            setJOSMSelection(null);
        } else {
            setJOSMSelection(newLayer.data.getSelected());
        }
    }

    /* ------------------------------------------------------------------------ */
    /* interface SelectionChangeListener                                        */
    /* ------------------------------------------------------------------------ */
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        // only update the JOSM selection if it is changed in the same data layer
        // this turn restriction editor is working on
        OsmDataLayer layer = Main.main.getEditLayer();
        if(layer == null) return;
        if (layer != this.layer) return;
        setJOSMSelection(newSelection);
    }

    /* ------------------------------------------------------------------------ */
    /* interface DataSetListener                                                */
    /* ------------------------------------------------------------------------ */
    public void dataChanged(DataChangedEvent event) {
        if (event.getDataset() != layer.data) return;
        fireContentsChanged(this, 0, getSize());
    }

    public void nodeMoved(NodeMovedEvent event) {
        if (event.getDataset() != layer.data) return;
        // may influence the display name of primitives, update the data
        update(event.getPrimitives());
    }

    public void otherDatasetChange(AbstractDatasetChangedEvent event) {
        if (event.getDataset() != layer.data) return;
        // may influence the display name of primitives, update the data
        update(event.getPrimitives());
    }

    public void relationMembersChanged(RelationMembersChangedEvent event) {
        if (event.getDataset() != layer.data) return;
        // may influence the display name of primitives, update the data
        update(event.getPrimitives());
    }

    public void tagsChanged(TagsChangedEvent event) {
        if (event.getDataset() != layer.data) return;
        // may influence the display name of primitives, update the data
        update(event.getPrimitives());
    }

    public void wayNodesChanged(WayNodesChangedEvent event) {
        if (event.getDataset() != layer.data) return;
        // may influence the display name of primitives, update the data
        update(event.getPrimitives());
    }

    public void primitivesAdded(PrimitivesAddedEvent event) {/* ignored - handled by SelectionChangeListener */}
    public void primitivesRemoved(PrimitivesRemovedEvent event) {/* ignored - handled by SelectionChangeListener*/}
  
    /* ------------------------------------------------------------------------ */
    /* interface PrimitiveIdListProvider                                        */
    /* ------------------------------------------------------------------------ */
    public List<PrimitiveId> getSelectedPrimitiveIds() {
        List<PrimitiveId> ret = new ArrayList<PrimitiveId>(getSelected().size());
        for(int i=0; i< selection.size(); i++) {
            if (selectionModel.isSelectedIndex(i)) {
                ret.add(selection.get(i).getPrimitiveId());
            }
        }
        return ret;
    }
}
