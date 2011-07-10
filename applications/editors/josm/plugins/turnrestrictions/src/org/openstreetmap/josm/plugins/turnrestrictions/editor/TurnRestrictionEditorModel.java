package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.text.MessageFormat;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.event.DatasetEventManager.FireMode;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.tagging.TagEditorModel;
import org.openstreetmap.josm.gui.tagging.TagModel;
import org.openstreetmap.josm.plugins.turnrestrictions.qa.IssuesModel;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * This is the model for the turn restriction editor. It keeps the editing state
 * for a single turn restriction. 
 * 
 */
public class TurnRestrictionEditorModel extends Observable implements DataSetListener{
    static private final Logger logger = Logger.getLogger(TurnRestrictionEditorModel.class.getName());
    
    /**
     * Replies true if {@code tp1} and {@code tp2} have the same tags and
     * the same members 
     * 
     * @param tp1 a turn restriction. Must not be null. 
     * @param tp2 a turn restriction . Must not be null.
     * @return true if {@code tp1} and {@code tp2} have the same tags and
     * the same members
     * @throws IllegalArgumentException thrown if {@code tp1} is null
     * @throws IllegalArgumentException thrown if {@code tp2} is null
     */
    static public boolean hasSameMembersAndTags(Relation tp1, Relation tp2) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(tp1, "tp1");
        CheckParameterUtil.ensureParameterNotNull(tp2, "tp2");
        if (!TagCollection.from(tp1).asSet().equals(TagCollection.from(tp2).asSet())) return false;
        if (tp1.getMembersCount() != tp2.getMembersCount()) return false;
        for(int i=0; i < tp1.getMembersCount();i++){
            if (!tp1.getMember(i).equals(tp2.getMember(i))) return false;
        }
        return true;
    }
    
    private OsmDataLayer layer;
    private final TagEditorModel tagEditorModel = new TagEditorModel();
    private  RelationMemberEditorModel memberModel;
    private  IssuesModel issuesModel;
    private NavigationControler navigationControler;
    private JosmSelectionListModel selectionModel;
    
    /**
     * Creates a model in the context of a {@link OsmDataLayer}
     * 
     * @param layer the layer. Must not be null.
     * @param navigationControler control to direct the user to specific UI components. Must not be null 
     * @throws IllegalArgumentException thrown if {@code layer} is null
     */
    public TurnRestrictionEditorModel(OsmDataLayer layer, NavigationControler navigationControler) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        CheckParameterUtil.ensureParameterNotNull(navigationControler, "navigationControler");
        this.layer = layer;
        this.navigationControler = navigationControler;
        memberModel = new RelationMemberEditorModel(layer);
        memberModel.addTableModelListener(new RelationMemberModelListener());
        issuesModel = new IssuesModel(this);
        addObserver(issuesModel);
        tagEditorModel.addTableModelListener(new TagEditorModelObserver());
        selectionModel = new JosmSelectionListModel(layer);
    }
    
    /**
     * Replies the model for the currently selected JOSM primitives
     */
    public JosmSelectionListModel getJosmSelectionListModel() {
    	return selectionModel;
    }
    
    /**
     * Sets the way participating in the turn restriction in a given role.
     * 
     * @param role the role. Must not be null.  
     * @param way the way which participates in the turn restriction in the respective role.
     * null, to remove the way with the given role.
     * @exception IllegalArgumentException thrown if role is null
     */
    public void setTurnRestrictionLeg(TurnRestrictionLegRole role, Way way) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(role, "role");
        switch(role){
        case FROM:
            memberModel.setFromPrimitive(way);
            break;
        case TO:
            memberModel.setToPrimitive(way);
            break;
        }
    }   
        
    /**
     * Sets the way participating in the turn restriction in a given role.
     * 
     * @param role the role. Must not be null.  
     * @param wayId the id of the way to set
     * @exception IllegalArgumentException thrown if role is null
     * @exception IllegalArgumentException thrown if wayId != null isn't the id of a way
     * @exception IllegalStateException thrown the no way with this id was found in the dataset 
     */
    public void setTurnRestrictionLeg(TurnRestrictionLegRole role, PrimitiveId wayId) {
        CheckParameterUtil.ensureParameterNotNull(role, "role");
        if (wayId == null) {
            setTurnRestrictionLeg(role, (Way)null);
            return;
        }
        if (!wayId.getType().equals(OsmPrimitiveType.WAY)) {
            throw new IllegalArgumentException(MessageFormat.format("parameter ''wayId'' of type {0} expected, got {1}", OsmPrimitiveType.WAY, wayId.getType()));
        }

        OsmPrimitive p = layer.data.getPrimitiveById(wayId);
        if (p == null) {
            throw new IllegalStateException(MessageFormat.format("didn''t find way with id {0} in layer ''{1}''", wayId, layer.getName()));         
        }
        setTurnRestrictionLeg(role, (Way)p);
    }   
    
    /**
     * <p>"Officially" a turn restriction should have exactly one member with 
     * role {@link TurnRestrictionLegRole#FROM FROM} and one member with role {@link TurnRestrictionLegRole#TO TO},
     * both referring to an OSM {@link Way}. In order to deals with turn restrictions where these
     * integrity constraints are violated, this model also supports relation with multiple or no
     * 'from' or 'to' members.</p>
     * 
     * <p>Replies the turn restriction legs with role {@code role}. If no leg with this
     * role exists, an empty set is returned. If multiple legs exists, the set of referred
     * primitives is returned.</p>  
     * 
     * @param role the role. Must not be null.
     * @return the set of turn restriction legs with role {@code role}. The empty set, if
     * no such turn restriction leg exists
     * @throws IllegalArgumentException thrown if role is null
     */
    public Set<OsmPrimitive>getTurnRestrictionLeg(TurnRestrictionLegRole role){
        CheckParameterUtil.ensureParameterNotNull(role, "role");
        switch(role){
        case FROM: return memberModel.getFromPrimitives();
        case TO: return memberModel.getToPrimitives();
        }
        // should not happen
        return null;
    }
    
    /**
     * Initializes the model from a relation representing a turn
     * restriction
     * 
     * @param turnRestriction the turn restriction
     */
    protected void initFromTurnRestriction(Relation turnRestriction) {
        
        // populate the member model
        memberModel.populate(turnRestriction);
        
        // make sure we have a restriction tag
        TagCollection tags = TagCollection.from(turnRestriction);
        tags.setUniqueForKey("type", "restriction");
        tagEditorModel.initFromTags(tags);
                
        setChanged();
        notifyObservers();
    }
    
    /**
     * Populates the turn restriction editor model with a turn restriction. 
     * {@code turnRestriction} is an arbitrary relation. A tag type=restriction
     * isn't required. If it is missing, it is added here. {@code turnRestriction}
     * must not be null and it must belong to a dataset. 
     * 
     * @param turnRestriction the turn restriction
     * @throws IllegalArgumentException thrown if turnRestriction is null
     * @throws IllegalArgumentException thrown if turnRestriction doesn't belong to a dataset  
     */
    public void populate(Relation turnRestriction) {
        CheckParameterUtil.ensureParameterNotNull(turnRestriction, "turnRestriction");
        if (turnRestriction.getDataSet() != null && turnRestriction.getDataSet() != layer.data) {           
            throw new IllegalArgumentException(
                // don't translate - it's a technical message
                MessageFormat.format("turnRestriction {0} must not belong to a different dataset than the dataset of layer ''{1}''", turnRestriction.getId(), layer.getName())
            );
        }
        initFromTurnRestriction(turnRestriction);
    }
    
    
    /**
     * Applies the current state in the model to a turn restriction
     * 
     * @param turnRestriction the turn restriction. Must not be null.
     */
    public void apply(Relation turnRestriction) {
        CheckParameterUtil.ensureParameterNotNull(turnRestriction, "turnRestriction");      
        TagCollection tags = tagEditorModel.getTagCollection();
        turnRestriction.removeAll();
        tags.applyTo(turnRestriction);
        memberModel.applyTo(turnRestriction);       
    }
    
    /**
     * Replies the current tag value for the tag <tt>restriction</tt>.
     * The empty tag, if there isn't a tag <tt>restriction</tt>.  
     * 
     * @return the tag value
     */
    public String getRestrictionTagValue() {
        TagCollection tags = tagEditorModel.getTagCollection();
        if (!tags.hasTagsFor("restriction")) return "";
        return tags.getJoinedValues("restriction");
    }
    
    /**
     * Sets the current value for the restriction tag. If {@code value} is
     * null or an empty string, the restriction tag is removed. 
     * 
     * @param value the value of the restriction tag 
     */
    public void setRestrictionTagValue(String value){
        if (value == null || value.trim().equals("")) {
            tagEditorModel.delete("restriction");           
        } else {
            TagModel  tm = tagEditorModel.get("restriction");
            if (tm != null){
                tm.setValue(value);
            } else {
                tagEditorModel.prepend(new TagModel("restriction", value.trim().toLowerCase()));
            }
        }
        setChanged();
        notifyObservers();
    }
    
    /**
     * Replies the list of 'via' objects. The return value is an
     * unmodifiable list.
     *  
     * @return the list of 'via' objects
     */
    public List<OsmPrimitive> getVias() {
        return memberModel.getVias();
    }
    
    /**
     * <p>Sets the list of vias for the edited turn restriction.</p>
     * 
     * <p>If {@code vias} is null, all vias are removed. All primitives
     * in {@code vias} must be assigned to a dataset and the dataset
     * must be equal to the dataset of this editor model, see {@link #getDataSet()}</p>
     * 
     * <p>null values in {@link vias} are skipped.</p>
     * 
     * @param vias the list of vias 
     * @throws IllegalArgumentException thrown if one of the via objects belongs to the wrong dataset 
     */
    public void setVias(List<OsmPrimitive> vias) throws IllegalArgumentException{
        memberModel.setVias(vias);
    }
    
    /**
     * Replies the layer in whose context this editor is working
     * 
     * @return the layer in whose context this editor is working
     */
    public OsmDataLayer getLayer() {
        return layer;
    }
    
    /**
     * Registers this model with global event sources like {@link DatasetEventManager}
     */
    public void registerAsEventListener(){
        DatasetEventManager.getInstance().addDatasetListener(this, FireMode.IN_EDT);
    }
    
    /**
     * Removes this model as listener from global event sources like  {@link DatasetEventManager}
     */
    public void unregisterAsEventListener() {
        DatasetEventManager.getInstance().removeDatasetListener(this);
    }
    
    /**
     * Replies the tag  editor model 
     * 
     * @return the tag  editor model
     */
    public TagEditorModel getTagEditorModel() {
        return tagEditorModel;
    }
    
    /**
     * Replies the editor model for the relation members
     * 
     * @return the editor model for the relation members
     */
    public RelationMemberEditorModel getRelationMemberEditorModel() {
        return memberModel;
    }
    
    /**
     * Replies the model for the open issues in this turn restriction
     * editor.
     * 
     * @return the model for the open issues in this turn restriction
     * editor
     */
    public IssuesModel getIssuesModel() {
        return issuesModel;
    }
    
    public NavigationControler getNavigationControler() {
        return navigationControler;
    }
    
    /**
     * Replies the current value of the tag "except", or the empty string
     * if the tag doesn't exist.
     * 
     * @return
     */
    public ExceptValueModel getExcept() {
        TagModel tag = tagEditorModel.get("except");
        if (tag == null) return new ExceptValueModel("");
        return new ExceptValueModel(tag.getValue());
    }
    
    /**
     * Sets the current value of the tag "except". Removes the
     * tag is {@code value} is null or consists of white
     * space only. 
     * 
     * @param value the new value for 'except'
     */
    public void setExcept(ExceptValueModel value){
        if (value == null || value.getValue().equals("")) {
            if (tagEditorModel.get("except") != null){
                tagEditorModel.delete("except");
                setChanged();
                notifyObservers();              
            }
            return;         
        }
        TagModel tag = tagEditorModel.get("except");
        if (tag == null) {
            tagEditorModel.prepend(new TagModel("except", value.getValue()));
            setChanged();
            notifyObservers();
        } else {
            if (!tag.getValue().equals(value.getValue())) {
                tag.setValue(value.getValue().trim());
                setChanged();
                notifyObservers();
            }
        }       
    }

    /* ----------------------------------------------------------------------------------------- */
    /* interface DataSetListener                                                                 */
    /* ----------------------------------------------------------------------------------------- */ 
    protected boolean isAffectedByDataSetUpdate(DataSet ds, List<? extends OsmPrimitive> updatedPrimitives) {
        if (ds != layer.data) return false;
        if (updatedPrimitives == null || updatedPrimitives.isEmpty()) return false;
        Set<OsmPrimitive> myPrimitives = memberModel.getMemberPrimitives();
        int size1 = myPrimitives.size();
        myPrimitives.retainAll(updatedPrimitives);
        return size1 != myPrimitives.size();
    }
    
    public void dataChanged(DataChangedEvent event) {
        // refresh the views
        setChanged();
        notifyObservers();      
    }

    public void nodeMoved(NodeMovedEvent event) {
        // may affect the display name of node in the list of vias
        if (isAffectedByDataSetUpdate(event.getDataset(), event.getPrimitives())) {
            setChanged();
            notifyObservers();
        }
    }

    public void otherDatasetChange(AbstractDatasetChangedEvent event) {/* irrelevant in this context */}

    public void primitivesAdded(PrimitivesAddedEvent event) {/* irrelevant in this context */}
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        // relevant for the state of this model but not handled here. When the 
        // state of this model is applied to the dataset we check whether the 
        // the turn restriction refers to deleted or invisible primitives 
    }

    public void relationMembersChanged(RelationMembersChangedEvent event) {/* irrelevant in this context */}
    public void tagsChanged(TagsChangedEvent event) {
        // may affect the display name of 'from', 'to' or 'via' elements
        if (isAffectedByDataSetUpdate(event.getDataset(), event.getPrimitives())) {
            setChanged();
            notifyObservers();
        }
    }

    public void wayNodesChanged(WayNodesChangedEvent event) {
        // may affect the display name of 'from', 'to' or 'via' elements
        if (isAffectedByDataSetUpdate(event.getDataset(), event.getPrimitives())) {
            setChanged();
            notifyObservers();
        }       
    }   
    
    class RelationMemberModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            setChanged();
            notifyObservers();
        }       
    }

    /* ----------------------------------------------------------------------------------------- */
    /* inner classes                                                                             */
    /* ----------------------------------------------------------------------------------------- */ 
    class TagEditorModelObserver implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            setChanged();
            notifyObservers();
        }       
    }
}
