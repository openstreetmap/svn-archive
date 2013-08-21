package org.openstreetmap.josm.plugins.turnrestrictions.editor;


import static org.openstreetmap.josm.tools.I18n.tr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class RelationMemberEditorModel extends AbstractTableModel{  
    //static private final Logger logger = Logger.getLogger(RelationMemberEditorModel.class.getName());
    private final ArrayList<RelationMemberModel> members = new ArrayList<RelationMemberModel>();
    private OsmDataLayer layer;
    private DefaultListSelectionModel rowSelectionModel;
    private DefaultListSelectionModel colSelectionModel;
    
    /**
     * Creates a new model in the context of an {@link OsmDataLayer}. Internally allocates
     * a row and a column selection model, see {@link #getRowSelectionModel()} and 
     * {@link #getColSelectionModel()}.
     * 
     * @param layer the data layer. Must not be null.
     * @exception IllegalArgumentException thrown if layer is null
     */
    public RelationMemberEditorModel(OsmDataLayer layer) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.layer = layer;
        rowSelectionModel = new DefaultListSelectionModel();
        colSelectionModel = new DefaultListSelectionModel();
    }

    /**
     *  Creates a new model in the context of an {@link OsmDataLayer}
     *  
     * @param layer layer the data layer. Must not be null.
     * @param rowSelectionModel the row selection model. Must not be null.
     * @param colSelectionModel the column selection model. Must not be null.
     * @throws IllegalArgumentException thrown if layer is null
     * @throws IllegalArgumentException thrown if rowSelectionModel is null
     * @throws IllegalArgumentException thrown if colSelectionModel is null
     */
    public RelationMemberEditorModel(OsmDataLayer layer, DefaultListSelectionModel rowSelectionModel, DefaultListSelectionModel colSelectionModel) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        CheckParameterUtil.ensureParameterNotNull(rowSelectionModel, "rowSelectionModel");
        CheckParameterUtil.ensureParameterNotNull(colSelectionModel, "colSelectionModel");
        this.layer = layer;
        this.rowSelectionModel = rowSelectionModel;
        this.colSelectionModel = colSelectionModel;
    }

    /**
     * Replies the row selection model used in this table model.
     * 
     * @return the row selection model 
     */
    public DefaultListSelectionModel getRowSelectionModel() {
        return rowSelectionModel;
    }
    
    /**
     * Replies the column selection model used in this table model.
     * 
     * @return the col selection model
     */
    public DefaultListSelectionModel getColSelectionModel() {
        return colSelectionModel;
    }
    
    /**
     * Replies the set of {@link OsmPrimitive}s with the role {@code role}. If no
     * such primitives exists, the empty set is returned.
     * 
     * @return the set of {@link OsmPrimitive}s with the role {@code role}
     */
    protected Set<OsmPrimitive> getPrimitivesWithRole(String role) {
        HashSet<OsmPrimitive> ret = new HashSet<OsmPrimitive>();
        for (RelationMemberModel rm: members){
            if (rm.getRole().equals(role)){
                OsmPrimitive p = layer.data.getPrimitiveById(rm.getTarget());
                if (p != null){
                    ret.add(p);
                }
            }
        }
        return ret;
    }
    
    /**
     * Replies the list of {@link RelationMemberModel}s with the role {@code role}. If no
     * such primitives exists, the empty set is returned.
     * 
     * @return the set of {@link RelationMemberModel}s with the role {@code role}
     */
    protected List<RelationMemberModel> getRelationMembersWithRole(String role) {
        ArrayList<RelationMemberModel> ret = new ArrayList<RelationMemberModel>();
        for (RelationMemberModel rm: members){
            if (rm.getRole().equals(role)){
                ret.add(rm);
            }
        }
        return ret;
    }
    
    /**
     * Removes all members with role {@code role}.
     * 
     * @param role the role. Ignored if null.
     * @return true if the list of members was modified; false, otherwise
     */
    protected boolean removeMembersWithRole(String role){
        if (role == null) return false;
        boolean isChanged = false;
        for(Iterator<RelationMemberModel> it = members.iterator(); it.hasNext(); ){
            RelationMemberModel rm = it.next();
            if (rm.getRole().equals(role)) {
                it.remove();
                isChanged = true;
            }
        }
        return isChanged;
    }
        
    /**
     * Replies the set of {@link OsmPrimitive}s with the role 'from'. If no
     * such primitives exists, the empty set is returned.
     * 
     * @return the set of {@link OsmPrimitive}s with the role 'from'
     */
    public Set<OsmPrimitive> getFromPrimitives() {
        return getPrimitivesWithRole("from");       
    }
    
    /**
     * Replies the set of {@link OsmPrimitive}s with the role 'to'. If no
     * such primitives exists, the empty set is returned.
     * 
     * @return the set of {@link OsmPrimitive}s with the role 'from'
     */
    public Set<OsmPrimitive> getToPrimitives() {
        return getPrimitivesWithRole("to");
    }
    
    /**
     * Replies the list of 'via' objects in the order they occur in the
     * member list. Replies an empty list if no vias exist
     * 
     * @return 
     */
    public List<OsmPrimitive> getVias() {
        ArrayList<OsmPrimitive> ret = new ArrayList<OsmPrimitive>();
        for (RelationMemberModel rm: getRelationMembersWithRole("via")){
            ret.add(layer.data.getPrimitiveById(rm.getTarget()));
        }
        return ret;
    }
    
    /**
     * Sets the list of vias. Removes all 'vias' if {@code vias} is null.
     * 
     * null vias are skipped. A via must belong to the dataset of the layer in whose context
     * this editor is working, otherwise an {@link IllegalArgumentException} is thrown.
     * 
     * @param vias the vias.
     * @exception IllegalArgumentException thrown if a via doesn't belong to the dataset of the layer
     * in whose context this editor is working 
     */
    public void setVias(List<OsmPrimitive> vias) throws IllegalArgumentException{
        boolean viasDeleted = removeMembersWithRole("via");
        if (vias == null || vias.isEmpty()){
            if (viasDeleted){
                fireTableDataChanged();
            }
            return;
        }
        // check vias 
        for (OsmPrimitive via: vias) {
            if (via == null) continue;
            if (via.getDataSet() == null || via.getDataSet() != layer.data){
                throw new IllegalArgumentException(MessageFormat.format("via object ''{0}'' must belong to dataset of layer ''{1}''", via.getDisplayName(DefaultNameFormatter.getInstance()), layer.getName()));
            }
        }
        // add vias 
        for (OsmPrimitive via: vias) {
            if (via == null) continue;
            RelationMemberModel model = new RelationMemberModel("via", via);
            members.add(model);
        }
        fireTableDataChanged();
    }
    
    /**
     * Sets the turn restriction member with role {@code role}. Removes all
     * members with role {@code role} if {@code id} is null.
     * 
     * @param id the id 
     * @return true if the model was modified; false, otherwise
     */
    protected boolean setPrimitiveWithRole(PrimitiveId id, String role){
        if (id == null){
            return removeMembersWithRole(role);
        }
        
        List<RelationMemberModel> fromMembers = getRelationMembersWithRole(role);
        if (fromMembers.isEmpty()){
            RelationMemberModel rm = new RelationMemberModel(role, id);
            members.add(rm);
            return true;
        } else if (fromMembers.size() == 1){
            RelationMemberModel rm = fromMembers.get(0);
            if (!rm.getTarget().equals(id)){
                rm.setTarget(id);
                return true;
            }
            return false;
        } else {
            removeMembersWithRole(role);
            RelationMemberModel rm = new RelationMemberModel(role, id);
            members.add(rm);
            return true;
        }
    }
    
    /**
     * Sets the turn restriction member with role 'from'. Removes all
     * members with role 'from' if {@code id} is null.
     * 
     * @param id the id 
     */
    public void setFromPrimitive(PrimitiveId id){
        if (setPrimitiveWithRole(id, "from")) {
            fireTableDataChanged();
        }
    }
    
    /**
     * Sets the turn restriction member with role 'to'. Removes all
     * members with role 'to' if {@code id} is null.
     * 
     * @param id the id 
     */
    public void setToPrimitive(PrimitiveId id){
        if (setPrimitiveWithRole(id, "to")) {
            fireTableDataChanged();
        }
    }
    
    /**
     * Replies the set of {@link OsmPrimitive}s referred to by members in
     * this model.
     * 
     * @return the set of {@link OsmPrimitive}s referred to by members in
     * this model.
     */
    public Set<OsmPrimitive> getMemberPrimitives() {
        Set<OsmPrimitive> ret = new HashSet<OsmPrimitive>();
        for (RelationMemberModel rm: members){
            OsmPrimitive p = layer.data.getPrimitiveById(rm.getTarget());
            if (p != null) ret.add(p);
        }
        return ret;
    }
    
    /**
     * Populates the model with the relation member of a turn restriction. Clears
     * the model if {@code tr} is null. 
     * 
     * @param tr the turn restriction
     */
    public void populate(Relation tr){
        members.clear();
        if (tr == null){
            fireTableDataChanged();
            return;
        }
        for(RelationMember rm: tr.getMembers()){
            members.add(new RelationMemberModel(rm));
        }
        fireTableDataChanged();
    }
    
    /**
     * Replaces the member of turn restriction {@code tr} by the relation members currently
     * edited in this model.
     * 
     * @param tr the turn restriction. Ignored if null.
     */
    public void applyTo(Relation tr){
        if (tr == null) return;
        List<RelationMember> newMembers = new ArrayList<RelationMember>();
        for(RelationMemberModel model: members){
            RelationMember rm = new RelationMember(model.getRole(), layer.data.getPrimitiveById(model.getTarget()));
            newMembers.add(rm);
        }
        tr.setMembers(newMembers);
    }
    
    /**
     * Clears the roles of all relation members currently selected in the 
     * table.
     */
    protected void clearSelectedRoles(){
        for(int i=0; i < getRowCount();i++){
            if (rowSelectionModel.isSelectedIndex(i)) {
                members.get(i).setRole("");
            }
        }       
    }
    
    /**
     * Removes the currently selected rows from the model 
     */
    protected void removedSelectedMembers() {
        for(int i=getRowCount()-1; i >= 0;i--){
            if (rowSelectionModel.isSelectedIndex(i)) {
                members.remove(i);
            }
        }
    }
    
    /**
     * Deletes the current selection.
     * 
     * If only cells in the first column are selected, the roles of the selected
     * members are reset to the empty string. Otherwise the selected members are
     * removed from the model. 
     * 
     */
    public void deleteSelected() {
        if (colSelectionModel.isSelectedIndex(0) && !colSelectionModel.isSelectedIndex(1)) {
            clearSelectedRoles();
        } else if (rowSelectionModel.getMinSelectionIndex() >= 0){
            removedSelectedMembers();
        }
        fireTableDataChanged();
    }
    
    protected List<Integer> getSelectedIndices() {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for(int i =0; i < members.size(); i++){
            if (rowSelectionModel.isSelectedIndex(i)) 
                ret.add(i);
        }
        return ret;
    }
    
    public boolean canMoveUp() {
        List<Integer> sel = getSelectedIndices();
        if (sel.isEmpty()) return false;
        return sel.get(0) > 0;
    }
    
    public boolean canMoveDown() {
        List<Integer> sel = getSelectedIndices();
        if (sel.isEmpty()) return false;
        return sel.get(sel.size()-1) < members.size()-1;
    }
    
    public void moveUpSelected() {
        if (!canMoveUp()) return;
        List<Integer> sel = getSelectedIndices();
        for (int idx: sel){
            RelationMemberModel m = members.remove(idx);
            members.add(idx-1, m);
        }
        fireTableDataChanged();
        rowSelectionModel.clearSelection();
        colSelectionModel.setSelectionInterval(0, 1);
        for (int idx: sel){
            rowSelectionModel.addSelectionInterval(idx-1, idx-1);
        }
    }
    
    public void moveDownSelected() {
        if (!canMoveDown()) return;
        List<Integer> sel = getSelectedIndices();
        for (int i = sel.size()-1; i>=0;i--){
            int idx = sel.get(i);
            RelationMemberModel m = members.remove(idx);
            members.add(idx+1, m);
        }
        fireTableDataChanged();
        rowSelectionModel.clearSelection();
        colSelectionModel.setSelectionInterval(0, 1);
        for (int idx: sel){
            rowSelectionModel.addSelectionInterval(idx+1, idx+1);
        }
    }
    
    /**
     * <p>Inserts a list of new relation members with the empty role for the primitives
     * with id in {@code ids}. Inserts the new primitives at the position of the first
     * selected row. If no row is selected, at the end of the list.</p>
     * 
     * <p> null values are skipped. If there is an id for which there is no primitive in the context 
     *  layer, if the primitive is deleted or invisible, an {@link IllegalArgumentException}
     *  is thrown and nothing is inserted.</p>
     * 
     * @param ids the list of ids. Ignored if null.
     * @throws IllegalArgumentException thrown if one of the ids can't be inserted
     */
    public void insertMembers(Collection<PrimitiveId> ids) throws IllegalArgumentException {
        if (ids == null) return;    
        ArrayList<RelationMemberModel> newMembers = new ArrayList<RelationMemberModel>();
        for (PrimitiveId id: ids){
            OsmPrimitive p = layer.data.getPrimitiveById(id);
            if (p == null){
                throw new IllegalArgumentException(tr("Cannot find object with id ''{0}'' in layer ''{1}''", id.toString(), layer.getName()));
            }
            if (p.isDeleted() || ! p.isVisible()) {
                throw new IllegalArgumentException(tr("Cannot add object ''{0}'' as relation member because it is deleted or invisible in layer ''{1}''", p.getDisplayName(DefaultNameFormatter.getInstance()), layer.getName()));              
            }
            newMembers.add(new RelationMemberModel("",id));
        }
        if (newMembers.isEmpty()) return;
        int insertPos = rowSelectionModel.getMinSelectionIndex();
        if ( insertPos >=0){
            members.addAll(insertPos, newMembers);
        } else {
            members.addAll(newMembers);
        }
        fireTableDataChanged();
        if (insertPos < 0) insertPos = 0;       
        colSelectionModel.setSelectionInterval(0, 1); // select both columns
        rowSelectionModel.setSelectionInterval(insertPos, insertPos + newMembers.size()-1);
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
    	return members.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex){
        case 0: return members.get(rowIndex).getRole();
        case 1: return layer.data.getPrimitiveById(members.get(rowIndex).getTarget());
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // only the column with the member roles is editable
        return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex !=0)return;
        String role = (String)aValue;
        RelationMemberModel model = members.get(rowIndex);
        model.setRole(role);
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}