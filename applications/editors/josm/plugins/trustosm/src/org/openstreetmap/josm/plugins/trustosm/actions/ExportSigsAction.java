package org.openstreetmap.josm.plugins.trustosm.actions;

import static org.openstreetmap.josm.actions.SaveActionBase.createAndOpenSaveFileChooser;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.plugins.trustosm.io.SigExporter;
import org.openstreetmap.josm.tools.Shortcut;

public class ExportSigsAction extends DiskAccessAction {

	public ExportSigsAction() {
		super(tr("Export sigs..."), "exportsigs", tr("Export all signatures to XML file."),
				Shortcut.registerShortcut("file:exportsigs", tr("Export sigs to XML..."), KeyEvent.VK_X, Shortcut.GROUPS_ALT2+Shortcut.GROUP_MENU));
	}

	public ExportSigsAction(String name, String iconName, String tooltip, Shortcut shortcut) {
		super(name, iconName, tooltip, shortcut);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isEnabled())
			return;
		doSave();
	}

	public boolean doSave() {
		File f = createAndOpenSaveFileChooser(tr("Save Signatures file"), "tosm");
		if (f == null)
			return false;
		SigExporter exporter = new SigExporter();
		try {
			exporter.exportData(f, null);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
