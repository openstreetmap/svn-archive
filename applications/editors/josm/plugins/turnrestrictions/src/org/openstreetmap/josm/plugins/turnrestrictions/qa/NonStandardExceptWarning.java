package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.plugins.turnrestrictions.editor.ExceptValueModel;

/**
 * Issue when the 'except' tag consists of non-standard values
 * 
 */
public class NonStandardExceptWarning extends Issue{
    private ExceptValueModel value;
    public NonStandardExceptWarning(IssuesModel parent, ExceptValueModel value) {
        super(parent, Severity.WARNING);
        actions.add(new FixInEditorAction());
        this.value  = value;
    }

    @Override
    public String getText() {       
        return tr("The tag <tt>except</tt> has the non-standard value <tt>{0}</tt>. "
                + "It is recommended to use standard values for <tt>except</tt> only.",
                value.getValue()
                );              
    }
    
    class FixInEditorAction extends AbstractAction {
        public FixInEditorAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to Basic Editor and select standard vehicle type based exceptions"));
        }
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoBasicEditor();        
        }       
    }
}
