package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionType;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Represents an error given by the fact that either 'from' or 'to' should
 * be split because they intersect.
 *
 */
public class TurnRestrictionLegSplitRequiredError extends Issue{
	static private final Logger logger = Logger.getLogger(TurnRestrictionLegSplitRequiredError.class.getName());
	
    private TurnRestrictionLegRole role;
    private Way from;
    private Way to;
    private Node intersect;
    
    /**
     * <p>Creates the issue for a pair of ways {@code from} and {@code to} which intersect
     * at node {@code intersect}.</p>
     * 
     * @param parent the parent model 
     * @param from the way with role "from"
     * @param to the way with role "to"
     * @param intersect the intersection node 
     */
    public TurnRestrictionLegSplitRequiredError(IssuesModel parent, Way from, Way to){
    	super(parent, Severity.ERROR);
    	CheckParameterUtil.ensureParameterNotNull(from, "from");
    	CheckParameterUtil.ensureParameterNotNull(to, "to"); 
    	
    	intersect= TurnRestrictionBuilder.getUniqueCommonNode(from, to);
    	if (intersect == null)
    		throw new IllegalArgumentException("exactly one intersecting node required");
    	
    	this.from = from;
    	this.to = to;
    	this.role = null;
    	actions.add(new SplitAction());
    }

    /**
     * Create the issue
     *
     * @param parent the parent model
     * @param role the role of the way which should be splitted
     * @param from the way with role 'from'
     * @param to the way with role 'to'
     * @param intersect the node at the intersection
     */
    public TurnRestrictionLegSplitRequiredError(IssuesModel parent, TurnRestrictionLegRole role, Way from, Way to, Node intersect) {
        super(parent, Severity.ERROR);
        this.role = role;
        this.from = from;
        this.to = to;
        this.intersect = intersect;
        actions.add(new SplitAction());
    }

    @Override
    public String getText() {
        String msg = null;
        if (role == null){
        	/*
        	 * from and to intersect at a common node. Both have to be split.
        	 */
        	return tr("The way <span class=\"object-name\">{0}</span> with role <tt>from</tt> and the "
        			+ "way <span class=\"object-name\">{1}</span> with role <tt>to</tt> intersect "        			
        			+ "at node <span class=\"object-name\">{2}</span>. "
        			+ "<p> "
        			+ "Both ways should be split at the intersecting node.",
                    from.getDisplayName(DefaultNameFormatter.getInstance()),
                    to.getDisplayName(DefaultNameFormatter.getInstance()),
                    intersect.getDisplayName(DefaultNameFormatter.getInstance())
                );
        }
        switch(role){
        case FROM:
        	/*
        	 * "to" joins "from" at a common node. Only from has to be split
        	 */
            msg = tr("The way <span class=\"object-name\">{0}</span> with role <tt>{1}</tt> should be split "
                + "at node <span class=\"object-name\">{2}</span> where it connects to way <span class=\"object-name\">{3}</span>.",
                from.getDisplayName(DefaultNameFormatter.getInstance()),
                role.getOsmRole(),
                intersect.getDisplayName(DefaultNameFormatter.getInstance()),
                to.getDisplayName(DefaultNameFormatter.getInstance())
            );
            break;
        case TO:
        	/*
        	 * "from" joins "to" at a common node. Only to has to be split
        	 */
            msg = tr("The way <span class=\"object-name\">{0}</span> with role <tt>{1}</tt> should be split "
                    + "at node <span class=\"object-name\">{2}</span> where it connects to way <span class=\"object-name\">{3}</span>.",
                    to.getDisplayName(DefaultNameFormatter.getInstance()),
                    role.getOsmRole(),
                    intersect.getDisplayName(DefaultNameFormatter.getInstance()),
                    from.getDisplayName(DefaultNameFormatter.getInstance())
                );
            break;
        }
        return msg;
    }

    class SplitAction extends AbstractAction {
        public SplitAction() {
            putValue(NAME, tr("Split now"));
            putValue(SHORT_DESCRIPTION, tr("Split the ways"));
        }
        
        public void actionPerformed(ActionEvent e) {

        	SplitWayResult result = null;
            if (role == null || role.equals(TurnRestrictionLegRole.FROM)){
            	  result = SplitWayAction.split(
                          parent.getEditorModel().getLayer(),
                          from,
                          Collections.singletonList(intersect),
                          Collections.<OsmPrimitive>emptyList()
                  );
            	  if (result != null){
                      Main.main.undoRedo.add(result.getCommand());
                  }
            }
            
            if (role == null || role.equals(TurnRestrictionLegRole.TO)) {
            	result = SplitWayAction.split(
                        parent.getEditorModel().getLayer(),
                        to,
                        Collections.singletonList(intersect),
                        Collections.<OsmPrimitive>emptyList()
                );
            	if (result != null){
                    Main.main.undoRedo.add(result.getCommand());
                }
            	if (result == null) return;
	        	TurnRestrictionType restrictionType = TurnRestrictionType.fromTagValue(getIssuesModel().getEditorModel().getRestrictionTagValue());
	            if (restrictionType == null) return;
	            Way adjustedTo = TurnRestrictionBuilder.selectToWayAfterSplit(
	            		 from,
	            		 result.getOriginalWay(),
	            		 result.getNewWays().get(0),
	            		 restrictionType
	            );
	
	            if (adjustedTo == null) return;
	            getIssuesModel().getEditorModel().setTurnRestrictionLeg(
	            		 TurnRestrictionLegRole.TO,
	            		 adjustedTo
	            );	     
	            getIssuesModel().getEditorModel().getLayer().data.setSelected(
	              		 Arrays.asList(from, adjustedTo)
	             );	
            } else {
	            getIssuesModel().getEditorModel().getLayer().data.setSelected(
	              		 Arrays.asList(from, to)
	             );	            	
            }           
        }
    }
}
