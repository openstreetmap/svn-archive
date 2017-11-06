package org.openstreetmap.josm.plugins.osminspector.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JTextPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer.BugInfo;
import org.openstreetmap.josm.tools.Shortcut;

public class OsmInspectorBugInfoDialog extends ToggleDialog {

	private JTextPane bugTextArea;

	/**
	 * Builds the content panel for this dialog
	 */
	protected void buildContentPanel() {
		MainApplication.getMap().addToggleDialog(this, true);
		
		bugTextArea = new JTextPane();
		createLayout(bugTextArea, true, Arrays.asList(new SideButton[] {}));
		bugTextArea.setText("This is a demo");
		this.add(bugTextArea);
	}

	public OsmInspectorBugInfoDialog(OsmInspectorLayer layer) {

		super(tr("OsmBugInfo"), "select",
				tr("Open a OSM Inspector selection list window."), Shortcut.registerShortcut("subwindow:select",
								tr("Toggle: {0}", tr("Current Selected Bug Info")),
								KeyEvent.VK_D, Shortcut.ALT_SHIFT), 150, // default
																			// height
				true // default is "show dialog"
		);
		buildContentPanel();
	}

	public void updateDialog(OsmInspectorLayer l) {
	}
	
	public void setBugDescription(BugInfo i){
	    bugTextArea.setText(i.getContentString());
	}
	
	@Override
	public void hideNotify() {
		if (dialogsPanel != null) {
			super.hideNotify();
		}
	}
}
