package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.plugins.turnrestrictions.dnd.PrimitiveIdListProvider;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * ViaListModel is a model for the list of 'via' objects of a turn restriction.
 * 
 */
public class ViaListModel extends AbstractListModel implements PrimitiveIdListProvider, Observer{
    //static private final Logger logger = Logger.getLogger(ViaListModel.class.getName());
    
    private DefaultListSelectionModel selectionModel;
    private final ArrayList<OsmPrimitive> vias = new ArrayList<OsmPrimitive>();
    private TurnRestrictionEditorModel model;
    
    /**
     * Constructor 
     * 
     * @param model the turn restriction editor model. Must not be null.
     * @param selectionModel the selection model. Must not be null.
     * @throws IllegalArgumentException thrown if model is null
     * @throws IllegalArgumentException thrown if selectionModel is null
     */
    public ViaListModel(TurnRestrictionEditorModel model, DefaultListSelectionModel selectionModel) {
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        CheckParameterUtil.ensureParameterNotNull(selectionModel, "selectionModel");
        this.model = model;
        this.selectionModel = selectionModel;
        model.addObserver(this);
        refresh();
    }

    /**
     * Replies the list of currently selected vias
     * 
     * @return the list of currently selected vias
     */
    public List<OsmPrimitive> getSelectedVias() {
        ArrayList<OsmPrimitive> ret = new ArrayList<OsmPrimitive>();
        for (int i=0; i < getSize(); i++) {
            if (selectionModel.isSelectedIndex(i)) {
                ret.add(vias.get(i));
            }
        }
        return ret;
    }
    
    /**
     * Sets the collection of currently selected vias
     * 
     *  @param vias a collection of vias 
     */
    public void setSelectedVias(Collection<OsmPrimitive> vias) {
        selectionModel.clearSelection();
        if (vias == null) return;
        for(OsmPrimitive via: vias) {
            int idx = this.vias.indexOf(via);
            if (idx < 0) continue;
            selectionModel.addSelectionInterval(idx, idx);
        }
    }
    
    /**
     * Replies the list of selected rows 
     * 
     * @return the list of selected rows
     */
    public List<Integer> getSelectedRows() {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (int i=0; i < getSize(); i++) {
            if (selectionModel.isSelectedIndex(i)) {
                ret.add(i);
            }
        }
        return ret;
    }
    
    protected List<Integer> moveUp(List<Integer> rows, int targetRow) {
        List<Integer> ret = new ArrayList<Integer>(rows.size());
        int delta = rows.get(0) - targetRow;
        for(int row: rows) {
            OsmPrimitive via = vias.remove(row);
            vias.add(row - delta, via);
            ret.add(row - delta);
        }
        return ret;
    }
    
    protected List<Integer>  moveDown(List<Integer> rows, int targetRow) {
        List<Integer> ret = new ArrayList<Integer>(rows.size());
        int delta = targetRow - rows.get(0);
        for(int i = rows.size()-1; i >=0; i--) {
            int row = rows.get(i);
            OsmPrimitive via = vias.remove(row);
            vias.add(row + delta, via);
            ret.add(row + delta);
        }
        return ret;
    }
    
    public void moveVias(List<Integer> selectedRows, int targetRow){
        if (selectedRows == null) return;
        if (selectedRows.size() == 1){
            int sourceRow = selectedRows.get(0);
            if (sourceRow == targetRow) return;
            OsmPrimitive via = vias.remove(sourceRow);
            vias.add(targetRow, via);
            fireContentsChanged(this, 0, getSize());
            selectionModel.setSelectionInterval(targetRow, targetRow);
            return;
        } 
        int min = selectedRows.get(0);
        int max = selectedRows.get(selectedRows.size()-1);
        if (targetRow < min) {
            selectedRows = moveUp(selectedRows, targetRow);
        } else if (targetRow == min){
            // do nothing
        } else if (targetRow - min < getSize() - max){
            int delta = Math.min(targetRow - min, getSize()-1 - max);
            targetRow = min + delta;
            if (targetRow > min) {
                selectedRows = moveDown(selectedRows, targetRow);
            }
        } 
        fireContentsChanged(this, 0, getSize());
        selectionModel.clearSelection();
        for(int row: selectedRows) {
            selectionModel.addSelectionInterval(row, row);
        }       
    }
    
    /**
     * Move the currently selected vias up by one position
     */
    public void moveUp() {
        List<Integer> sel = getSelectedRows();
        if (sel.isEmpty() || sel.get(0) == 0) return;
        moveVias(sel, sel.get(0)-1);
    }

    /**
     * Move the currently selected vias down by one position
     */
    public void moveDown() {
        List<Integer> sel = getSelectedRows();
        if (sel.isEmpty() || sel.get(sel.size()-1) == getSize()-1) return;
        moveVias(sel, sel.get(sel.size()-1)+1);
    }
    
    /**
     * Inserts a list of OSM objects given by OSM primitive ids. 
     * 
     * @param idsToInsert the ids of the objects to insert
     */
    public void insertVias(List<PrimitiveId> idsToInsert) {
        if (idsToInsert == null) return;
        List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>(idsToInsert.size());
        DataSet ds = model.getLayer().data;
        for(PrimitiveId id: idsToInsert){
            OsmPrimitive p = ds.getPrimitiveById(id);
            if (p == null){
                System.out.println(tr("Failed to retrieve OSM object with id {0} from dataset {1}. Cannot add it as ''via''.", id, ds));
                continue;
            }
            primitives.add(p);
        }
        int targetRow = Math.max(selectionModel.getMinSelectionIndex(),0);
        List<OsmPrimitive> newVias = new ArrayList<OsmPrimitive>(vias);
        newVias.addAll(targetRow, primitives);
        model.setVias(newVias);
        fireContentsChanged(this, 0, getSize());
        selectionModel.clearSelection();
        for(int i=targetRow; i< targetRow + primitives.size();i++) {
            selectionModel.addSelectionInterval(i, i);
        }           
    }

    /**
     * Removes the currently selected vias
     */
    public void removeSelectedVias() {
        ArrayList<OsmPrimitive> newVias = new ArrayList<OsmPrimitive>(vias);
        int j = 0;
        for(int i=0; i< getSize();i++){
            if (!selectionModel.isSelectedIndex(i)) continue;
            newVias.remove(i-j);
            j++;
        }
        if (j == 0) return; // nothing selected, nothing deleted
        model.setVias(newVias);
    }
    
    /**
     * Refreshes the list of 'vias' in this model with the current list of
     * vias from the turn restriction model. 
     */
    protected void refresh() {
        List<OsmPrimitive> sel = getSelectedVias();
        vias.clear();
        vias.addAll(model.getVias());       
        fireContentsChanged(this, 0, getSize());
        setSelectedVias(sel);
    }

    public Object getElementAt(int index) {
        return vias.get(index);
    }

    public int getSize() {
        return vias.size();
    }
    
    /* ----------------------------------------------------------------------- */
    /* interface PrimitiveIdListProvider                                       */
    /* ----------------------------------------------------------------------- */
    public List<PrimitiveId> getSelectedPrimitiveIds() {
        ArrayList<PrimitiveId> ids = new ArrayList<PrimitiveId>();
        for (OsmPrimitive p: getSelectedVias()) {
            ids.add(p.getPrimitiveId());
        }
        return ids;
    }

    /* ----------------------------------------------------------------------- */
    /* interface Observer                                                      */
    /* ----------------------------------------------------------------------- */
    public void update(Observable o, Object arg) {
        refresh();
    }   
}
