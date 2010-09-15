package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.NavigationControler;

/**
 * Issue when the restriction type is missing. Can't be fixed automatically, user
 * is redirected to the Basic Editor.
 * 
 */
public class MissingRestrictionTypeError extends Issue{
    
    public MissingRestrictionTypeError(IssuesModel parent) {
        super(parent, Severity.ERROR);
        actions.add(new FixInEditorAction());
    }

    @Override
    public String getText() {
        return tr("A turn restriction must declare the type of restriction. Please select a type in the Basic Editor.");                
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
