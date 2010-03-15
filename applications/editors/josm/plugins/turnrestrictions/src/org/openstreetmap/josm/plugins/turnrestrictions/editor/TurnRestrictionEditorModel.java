package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
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
	
	
	/** 
	 * holds the relation member of turn restriction relation from which the turn
	 * restriction leg with role 'from' was initialized. This is needed if OSM
	 * data contains turn restrictions with multiple 'from' members. The
	 * field is null if a turn restriction didn't have a member with role
	 * 'from'.
	 */
	private RelationMember fromRelationMember;
	
	/** 
	 * holds the relation member of turn restriction relation from which the turn
	 * restriction leg with role 'to' was initialized. This is needed if OSM
	 * data contains turn restrictions with multiple 'to' members. The
	 * field is null if a turn restriction didn't have a member with role
	 * 'to'.
	 */
	private RelationMember toRelationMember;
	private Way from;
	private Way to;
	private TagCollection tags = new TagCollection();
	private DataSet dataSet;
	private final List<OsmPrimitive> vias = new ArrayList<OsmPrimitive>();
	
	public TurnRestrictionEditorModel(){
	}
	
	/**
	 * Sets the way participating in the turn restriction in a given role.
	 * 
	 * @param role the role. Must not be null.  
	 * @param way the way which participates in the turn restriction in the respective role.
	 * null, to remove the way with the given role.
	 * @exception IllegalArgumentException thrown if role is null
	 */
	public void setTurnRestrictionLeg(TurnRestrictionLegRole role, Way way) {
		CheckParameterUtil.ensureParameterNotNull(role, "role");
		Way oldValue = null;
		switch(role){
		case FROM: 
			oldValue = this.from;
			this.from = way; 
			break;
		case TO:
			oldValue = this.to;
			this.to = way; 
			break;
		}
		
		if (oldValue != way) {
			setChanged();
			notifyObservers();		
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
		
		if (dataSet == null) {			
			throw new IllegalStateException("data set not initialized");			
		}
		OsmPrimitive p = dataSet.getPrimitiveById(wayId);
		if (p == null) {
			throw new IllegalStateException(MessageFormat.format("didn't find way with id {0} in dataset {1}", wayId, dataSet));			
		}
		setTurnRestrictionLeg(role, (Way)p);
	}	
	
	/**
	 * Replies the turn restrictioin leg with role {@code role}
	 * 
	 * @param role the role. Must not be null.
	 * @return the turn restrictioin leg with role {@code role}. null, if
	 * no such turn restriction leg exists
	 * @throws IllegalArgumentException thrown if role is null
	 */
	public Way getTurnRestrictionLeg(TurnRestrictionLegRole role){
		CheckParameterUtil.ensureParameterNotNull(role, "role");
		switch(role){
		case FROM: return from;
		case TO: return to;
		}
		return null;
	}
	
	/**
	 * Initializes the model from a relation representing a turn
	 * restriction
	 * 
	 * @param turnRestriction the turn restriction
	 */
	protected void initFromTurnRestriction(Relation turnRestriction) {
		this.from = null;
		this.to = null;
		this.fromRelationMember = null;
		this.toRelationMember = null;
		this.tags = new TagCollection();
		this.vias.clear();
		if (turnRestriction == null) return;
		for (RelationMember rm: turnRestriction.getMembers()) {
			if (rm.getRole().equals("from") && rm.isWay()) {
				this.fromRelationMember = rm;
				this.from = rm.getWay();
				break;
			}
		}
		for (RelationMember rm: turnRestriction.getMembers()) {
			if (rm.getRole().equals("to") && rm.isWay()) {
				this.toRelationMember = rm;
				this.to = rm.getWay();
				break;
			}
		}
		
		for (RelationMember rm: turnRestriction.getMembers()) {
			if (rm.getRole().equals("via")) {
				this.vias.add(rm.getMember());
			}
		}
		
		// make sure we have a restriction tag
		tags = TagCollection.from(turnRestriction);
		tags.setUniqueForKey("type", "restriction");
				
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
		if (turnRestriction.getDataSet() == null) {			 
			throw new IllegalArgumentException(
				// don't translate - it's a technical message
				MessageFormat.format("turnRestriction {0} must belong to a dataset", turnRestriction.getId())
			);
		}
		this.dataSet = turnRestriction.getDataSet();
		initFromTurnRestriction(turnRestriction);
	}
	
	/**
	 * Populates the turn restriction editor model with a new turn restriction,
	 * which isn't added to a data set yet. {@code ds} is the data set the turn
	 * restriction is eventually being added to. Relation members of this
	 * turn restriction must refer to objects in {@code ds}. 
	 *  
	 * {@code turnRestriction} is an arbitrary relation. A tag type=restriction
	 * isn't required. If it is missing, it is added here.  {@code turnRestriction}
	 * is required to be a new turn restriction with a negative id. It must not be
	 * part of a dataset yet
	 * 
	 * @param turnRestriction the turn restriction. Must not be null. New turn restriction
	 * required
	 * @param ds the dataset. Must not be null.
	 * @throws IllegalArgumentException thrown if turnRestriction is null
	 * @throws IllegalArgumentException thrown if turnRestriction is part of a dataset
	 * @throws IllegalArgumentException thrown if turnRestriction isn't a new turn restriction  
	 */
	public void populate(Relation turnRestriction, DataSet ds) {
		CheckParameterUtil.ensureParameterNotNull(turnRestriction, "turnRestriction");
		CheckParameterUtil.ensureParameterNotNull(ds, "ds");
		if (!turnRestriction.isNew()){			
			throw new IllegalArgumentException(
					// don't translate - it's a technical message
					MessageFormat.format("new turn restriction expected, got turn restriction with id {0}", turnRestriction.getId())
			);
		}
		if (turnRestriction.getDataSet() != null) {
			throw new IllegalArgumentException(
                    // don't translate - it's a technical message
					MessageFormat.format("expected turn restriction not assigned to a  dataset, got turn restriction with id {0} assigned to {1}", turnRestriction.getId(), turnRestriction.getDataSet())
			);
		}
		for(RelationMember rm: turnRestriction.getMembers()) {
			if (rm.getMember().getDataSet() != ds) {
				throw new IllegalArgumentException(
	                    // don't translate - it's a technical message
						MessageFormat.format("expected all members assigned to dataset {0}, got member {1} assigned to {2}", ds, rm, rm.getMember().getDataSet())
				);
			}
		}
		this.dataSet = ds;
		initFromTurnRestriction(turnRestriction);
	}
	
	/**
	 * Applies the current state in the model to a turn restriction
	 * 
	 * @param turnRestriction the turn restriction. Must not be null.
	 */
	public void apply(Relation turnRestriction) {
		CheckParameterUtil.ensureParameterNotNull(turnRestriction, "turnRestriction");
		// apply the tags
		tags.applyTo(turnRestriction);
		
		List<RelationMember> members = new ArrayList<RelationMember>();
		if (from != null){
			members.add(new RelationMember("from", from));
		}
		if (to != null) {
			members.add(new RelationMember("to", to));			
		}
		for(OsmPrimitive via: vias) {
			members.add(new RelationMember("via", via));
		}
		turnRestriction.setMembers(members);
	}
	
	/**
	 * Replies the current tag value for the tag <tt>restriction</tt>.
	 * null, if there isn't a tag <tt>restriction</tt>.  
	 * 
	 * @return the tag value
	 */
	public String getRestrictionTagValue() {
		if (!tags.hasTagsFor("restriction")) return null;
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
			tags.removeByKey("restriction");
		} else {
			tags.setUniqueForKey("restriction", value.trim());
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
		return Collections.unmodifiableList(vias);
	}
	
	/**
	 * Sets the list of vias for the edited turn restriction.
	 * 
	 * If {@code vias} is null, all vias are removed. All primitives
	 * in {@code vias} must be assigned to a dataset and the dataset
	 * must be equal to the dataset of this editor model, see {@see #getDataSet()}
	 * 
	 * @param vias the list of vias 
	 * @throws IllegalArgumentException thrown if one of the via objects is null or
	 * if it belongs to the wrong dataset 
	 */
	public void setVias(List<OsmPrimitive> vias) {
		if (vias == null) {
			this.vias.clear();
			setChanged();
			notifyObservers();
			return;
		}
		for (OsmPrimitive p: vias) {
			if (p == null)
				throw new IllegalArgumentException("a via object must not be null");
			if (p.getDataSet() != dataSet)
				throw new IllegalArgumentException(MessageFormat.format("a via object must belong to dataset {1}, object {2} belongs to {3}", dataSet, p.getPrimitiveId(), p.getDataSet()));
		}
		this.vias.clear();
		this.vias.addAll(vias);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Replies the dataset this turn restriction editor model is 
	 * working with
	 * 
	 * @return the dataset 
	 */
	public DataSet getDataSet() {
		return this.dataSet;
	}

	/**
	 * Registers this model with global event sources like {@see DatasetEventManager}
	 */
	public void registerAsEventListener(){
		DatasetEventManager.getInstance().addDatasetListener(this, FireMode.IN_EDT);
	}
	
	/**
	 * Removes this model as listener from global event sources like  {@see DatasetEventManager}
	 */
	public void unregisterAsEventListener() {
		DatasetEventManager.getInstance().removeDatasetListener(this);
	}

	/* ----------------------------------------------------------------------------------------- */
	/* interface DataSetListener                                                                 */
	/* ----------------------------------------------------------------------------------------- */
	
	protected boolean isAffectedByDataSetUpdate(DataSet ds, List<? extends OsmPrimitive> updatedPrimitives) {
		if (ds != dataSet) return false;
		if (updatedPrimitives == null || updatedPrimitives.isEmpty()) return false;
		if (from != null && updatedPrimitives.contains(from)) return true;
		if (to != null && updatedPrimitives.contains(to)) return true;
		for (OsmPrimitive via: vias){
			if (updatedPrimitives.contains(via)) return true;
		}
		return false;
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

	public void primtivesAdded(PrimitivesAddedEvent event) {/* irrelevant in this context */}
	public void primtivesRemoved(PrimitivesRemovedEvent event) {
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
}
