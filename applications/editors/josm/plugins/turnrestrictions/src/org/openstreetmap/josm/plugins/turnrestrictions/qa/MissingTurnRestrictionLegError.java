package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler.BasicEditorFokusTargets.*;

/**
 * A member with role 'from' or 'to' is missing. Can't be fixed automatically.
 * Redirect the user to the Basic editor panel.
 * 
 */
public class MissingTurnRestrictionLegError extends Issue {
    private TurnRestrictionLegRole role;

    /**
     * Creates the issue. 
     * 
     * @param parent the parent model 
     * @param role the role of the missing way
     */
    public MissingTurnRestrictionLegError(IssuesModel parent, TurnRestrictionLegRole role) {
        super(parent, Severity.ERROR);
        this.role = role;
        actions.add(new FixAction());
    }

    @Override
    public String getText() {
        String msg = "";
        switch(role){
        case FROM: 
            msg = tr("An OSM way with role <tt>from</tt> is required in a turn restriction.");
            break;
        case TO: 
            msg = tr("An OSM way with role <tt>to</tt> is required in a turn restriction.");
            break;
        }
        msg += " " + tr("Please go to the Basic editor and manually choose an OSM way.");
        return msg;
    }

    class FixAction extends AbstractAction {
        public FixAction() {
            putValue(NAME, tr("Add in editor"));
            switch(role){
            case FROM:
                putValue(SHORT_DESCRIPTION, tr("Add an OSM way with role ''from''"));
                break;
            case TO:
                putValue(SHORT_DESCRIPTION, tr("Add an OSM way with role ''to''"));
                break;              
            }           
        }
        public void actionPerformed(ActionEvent e) {
            switch(role){
            case FROM:
                getIssuesModel().getNavigationControler().gotoBasicEditor(FROM);
                break;
            case TO:
                getIssuesModel().getNavigationControler().gotoBasicEditor(TO);
                break;              
            }           
        }       
    }
}
