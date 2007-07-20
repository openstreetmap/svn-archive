package org.openstreetmap.josm.plugins.taggingpresettester;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Fires up the tagging preset tester
 * @author Immanuel.Scholz
 */
public class TaggingPresetTesterAction extends JosmAction {

	public TaggingPresetTesterAction() {
		super(tr("Tagging Preset Tester"), "tagging-preset-tester", tr("Open the tagging preset test tool for previewing tagging preset dialogs."), KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, true);
		Main.main.menu.helpMenu.addSeparator();
		Main.main.menu.helpMenu.add(this);
	}

	public void actionPerformed(ActionEvent e) {
		String taggingPresetSources = Main.pref.get("taggingpreset.sources");
		if (taggingPresetSources.equals("")) {
			JOptionPane.showMessageDialog(Main.parent, tr("You have to specify tagging preset sources in the preferences first."));
			return;
		}
		String[] args = taggingPresetSources.split(";");
		new TaggingPresetTester(args);
	}
}
