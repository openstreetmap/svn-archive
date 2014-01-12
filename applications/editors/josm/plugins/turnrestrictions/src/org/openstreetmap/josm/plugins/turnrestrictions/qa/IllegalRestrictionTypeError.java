package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler;

/**
 * Issue when the restriction type isn't a standard value. Can't be fixed
 * automatically, user is directed to the Basic editor.
 * 
 */
public class IllegalRestrictionTypeError extends Issue{
    private String value;
    
    public IllegalRestrictionTypeError(IssuesModel parent, String value) {
        super(parent, Severity.ERROR);
        actions.add(new FixInEditorAction());
        this.value = value;
    }

    @Override
    public String getText() {       
        return tr("This turn restriction uses a non-standard restriction type <tt>{0}</tt> for the tag key <tt>restriction</tt>. "
                + "It is recommended to use standard values only. Please select one in the Basic editor.",
                value
                );              
    }
    
    class FixInEditorAction extends AbstractAction {
        public FixInEditorAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to Basic Editor and manually choose a turn restriction type"));
        }
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoBasicEditor(NavigationControler.BasicEditorFokusTargets.RESTRICION_TYPE);         
        }       
    }
}
