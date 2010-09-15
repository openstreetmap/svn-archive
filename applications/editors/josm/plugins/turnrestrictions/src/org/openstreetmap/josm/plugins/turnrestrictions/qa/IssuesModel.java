package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.tagging.TagEditorModel;
import org.openstreetmap.josm.gui.tagging.TagModel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.ExceptValueModel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionEditorModel;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * IssuesModel is a model for an observable list of {@code Issues}
 * related to turn restriction.
 * 
 * It is also an {@see Observer} to an {@see TurnRestrictionEditorModel}
 * and populates itself with issues it derives from the current state
 * in the {@see TurnRestrictionEditorModel}.
 *  
 */
public class IssuesModel extends Observable implements Observer{
    private final ArrayList<Issue> issues = new ArrayList<Issue>();
    private TurnRestrictionEditorModel editorModel;
    
    /**
     * Creates the model 
     * 
     * {@code controler} is used in resolution actions for issues in
     * this model to direct the user to a specific input field in one
     * of the editor tabs in order to fix an issue. 
     * 
     * @param editorModel the editor model. Must not be null.
     * @throws IllegalArgumentException thrown if controler is null
     */
    public IssuesModel(TurnRestrictionEditorModel editorModel) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(editorModel, "editorModel");
        this.editorModel = editorModel;
        this.editorModel.addObserver(this);
    }
    
    /**
     * Populates the model with a list of issues. Just clears the model
     * if {@code issues} is null or empty. 
     * 
     * @param issues the list of issues. 
     */
    public void populate(List<Issue> issues){
        this.issues.clear();
        if (issues != null){
            this.issues.addAll(issues);
        }
        setChanged();
        notifyObservers();
    }
    
    /**
     * Replies the (unmodifiable) list of issues in this model.
     * 
     * @return the (unmodifiable) list of issues in this model.
     */
    public List<Issue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
    
    /**
     * Replies the turn restriction editor model 
     * 
     * @return
     */
    public TurnRestrictionEditorModel getEditorModel() {
        return editorModel;
    }
    
    /**
     * Populates this model with issues derived from the state of the
     * turn restriction editor model. If {@code editorModel} is null, the
     * list of issues is cleared.
     * 
     * @param editorModel the editor model. 
     */
    public void populate() {
        issues.clear();
        if (editorModel != null) {
            checkTags(editorModel);
            checkFromLeg(editorModel);
            checkToLeg(editorModel);
            checkFromAndToEquals(editorModel);
            checkVias(editorModel);
        }
        setChanged();
        notifyObservers();
    }
    
    /**
     * Checks whether there are required tags missing. 
     * 
     * @param editorModel
     */
    protected void checkTags(TurnRestrictionEditorModel editorModel) {
        TagEditorModel tagEditorModel = editorModel.getTagEditorModel();
        TagModel tag = tagEditorModel.get("type");
        
        // missing marker tag for a turn restriction
        if (tag == null || ! tag.getValue().trim().equals("restriction")) {
            issues.add(new RequiredTagMissingError(this, "type", "restriction"));
        }
        
        // missing or illegal restriction type ?
        tag = tagEditorModel.get("restriction");
        if (tag == null) {
            issues.add(new MissingRestrictionTypeError(this));
        } else if (!TurnRestrictionType.isStandardTagValue(tag.getValue())) {
            issues.add(new IllegalRestrictionTypeError(this, tag.getValue()));
        }

        // non-standard value for the 'except' tag? 
        ExceptValueModel except = getEditorModel().getExcept();
        if (!except.isStandard()) {
            issues.add(new NonStandardExceptWarning(this, except));
        }
    }
    
    /**
     * Checks various data integrity restriction for the relation member with
     * role 'from'.
     * 
     */
    protected void checkFromLeg(TurnRestrictionEditorModel editorModel) {
        Set<OsmPrimitive> froms = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM);
        if (froms.isEmpty()){
            issues.add(new MissingTurnRestrictionLegError(this, TurnRestrictionLegRole.FROM));
            return;
        } else if (froms.size() > 1){
            issues.add(new MultipleTurnRestrictionLegError(this, TurnRestrictionLegRole.FROM, froms.size()));
            return;
        } 
        OsmPrimitive p = froms.iterator().next();
        if (! (p instanceof Way)) {
            issues.add(new WrongTurnRestrictionLegTypeError(this, TurnRestrictionLegRole.FROM, p));
        }
    }
    
    /**
     * Checks various data integrity restriction for the relation member with
     * role 'to'.
     * 
     */
    protected void checkToLeg(TurnRestrictionEditorModel editorModel) {
        Set<OsmPrimitive> toLegs = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.TO);
        if (toLegs.isEmpty()){
            issues.add(new MissingTurnRestrictionLegError(this, TurnRestrictionLegRole.TO));
            return;
        } else if (toLegs.size() > 1){
            issues.add(new MultipleTurnRestrictionLegError(this, TurnRestrictionLegRole.TO, toLegs.size()));
            return;
        } 
        OsmPrimitive p = toLegs.iterator().next();
        if (! (p instanceof Way)) {
            issues.add(new WrongTurnRestrictionLegTypeError(this, TurnRestrictionLegRole.TO, p));
        }
    }
    
    /**
     * Creates an issue if this turn restriction has identical 'from' and to'.
     * 
     * @param editorModel
     */
    protected void checkFromAndToEquals(TurnRestrictionEditorModel editorModel){
        Set<OsmPrimitive> toLegs = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.TO);
        Set<OsmPrimitive> fromLegs = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM);
        if (toLegs.size() != 1 || fromLegs.size() != 1) return;
        
        OsmPrimitive from = fromLegs.iterator().next();
        OsmPrimitive to = toLegs.iterator().next();
        
        if (! (from instanceof Way)) return;
        if (! (to instanceof Way)) return;
        if (from.equals(to) && ! "no_u_turn".equals(editorModel.getRestrictionTagValue())){
            // identical from and to allowed for "no_u_turn" only
            //
            issues.add(new IdenticalTurnRestrictionLegsError(this, from));
        }       
    }
    
    protected Node getNodeAtIntersection(Way from, Way to){
        Set<Node> fromNodes = new HashSet<Node>(from.getNodes());
        fromNodes.retainAll(to.getNodes());
        if (fromNodes.size() == 1){
            return fromNodes.iterator().next();
        } else {
            return null;
        }
    }
    
    /**
     * Checks the 'via' members in the turn restriction
     * 
     * @param editorModel the editor model
     */
    protected void checkVias(TurnRestrictionEditorModel editorModel){
        Set<OsmPrimitive> toLegs = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.TO);
        Set<OsmPrimitive> fromLegs = editorModel.getTurnRestrictionLeg(TurnRestrictionLegRole.FROM);
        // we only check vias if 'to' and 'from' are already OK
        if (toLegs.size() != 1 || fromLegs.size() != 1) return;
        if (! (toLegs.iterator().next() instanceof Way)) return;
        if (! (fromLegs.iterator().next() instanceof Way)) return;
        
        Way from = (Way)fromLegs.iterator().next();
        Way to = (Way)toLegs.iterator().next();
        Node intersect = getNodeAtIntersection(from, to);
        if (intersect != null){
            if (!editorModel.getVias().contains(intersect)) {
                issues.add(new IntersectionMissingAsViaError(this, from, to, intersect));
            }
        }
        
        // 'from' intersects with 'to' - should be split  
        if (intersect != null && from.getNode(0) != intersect && from.getNode(from.getNodesCount()-1) != intersect){
            issues.add(new TurnRestrictionLegSplitRequiredError(this, TurnRestrictionLegRole.FROM, from, to, intersect));
        }
        // 'to' intersects with 'from' - should be split
        if (intersect != null && to.getNode(0) != intersect && to.getNode(to.getNodesCount()-1) != intersect){
            issues.add(new TurnRestrictionLegSplitRequiredError(this, TurnRestrictionLegRole.TO, from, to, intersect));
        }       
    }
    
    public NavigationControler getNavigationControler() {
        return editorModel.getNavigationControler();
    }
    
    public int getNumWarnings() {
        int ret = 0;
        for (Issue issue: issues){
            if (issue.getSeverity().equals(Severity.WARNING)) ret++;
        }
        return ret;
    }

    public int getNumErrors() {
        int ret = 0;
        for (Issue issue: issues){
            if (issue.getSeverity().equals(Severity.ERROR)) ret++;
        }
        return ret;
    }

    /* ------------------------------------------------------------------------------------- */
    /* interface Observer                                                                    */
    /* ------------------------------------------------------------------------------------- */
    public void update(Observable o, Object arg) {
        populate();     
    }
}
