package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;

/**
 * Issue when a turn restriction has multiple members with role 'from' or 'to'.
 * 
 */
public class MultipleTurnRestrictionLegError extends Issue {
    private TurnRestrictionLegRole role;
    private int numLegs;
    
    /**
     * Create the issue
     * 
     * @param parent the parent model 
     * @param role the role of the turn restriction leg with multiple entries 
     * @param numLegs the number of legs
     */
    public MultipleTurnRestrictionLegError(IssuesModel parent, TurnRestrictionLegRole role, int numLegs) {
        super(parent, Severity.ERROR);
        this.role = role;
        this.numLegs = numLegs;
        actions.add(new FixAction());
    }

    @Override
    public String getText() {
        switch(role){
        case FROM:  
            return tr("A turn restriction requires exactly one way with role <tt>from</tt>. "
                + "This turn restriction has {0} ways in this role. Please remove "
                + "{1} of them.",
                numLegs,
                numLegs -1
            );
        case TO: 
            return tr("A turn restriction requires exactly one way with role <tt>to</tt>. "
                    + "This turn restriction has {0} ways in this role. Please remove "
                    + "{1} of them.",
                    numLegs,
                    numLegs -1
                );
        }
        return "";
    }

    class FixAction extends AbstractAction {
        public FixAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to the Advanced Editor and remove the members"));
        }
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoAdvancedEditor();
        }       
    }
}
