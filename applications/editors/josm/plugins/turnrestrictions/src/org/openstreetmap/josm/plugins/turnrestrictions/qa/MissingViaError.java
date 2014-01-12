package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Issue if the legs of a turn restriction aren't connected and if there 
 * are no via objects configured. 
 * 
 */
public class MissingViaError extends Issue {

	public MissingViaError(IssuesModel parent) throws IllegalArgumentException {
		super(parent, Severity.WARNING);
		actions.add(new FixAction());
	}

	@Override
	public String getText() {
		 String msg = 
			 tr("The two ways participating in the turn restriction <strong>aren''t connected.</strong>")
			+ "<p>"
			+ tr("Make sure you add one or more via objects (nodes or ways) to the turn restriction.");
		 return msg;
	}

	class FixAction extends AbstractAction {
        public FixAction() {
            putValue(NAME, tr("Fix in editor"));
            putValue(SHORT_DESCRIPTION, tr("Go to the Advanced Editor and add via objects"));
        }
        public void actionPerformed(ActionEvent e) {
            getIssuesModel().getNavigationControler().gotoAdvancedEditor();
        }       
    }
}
