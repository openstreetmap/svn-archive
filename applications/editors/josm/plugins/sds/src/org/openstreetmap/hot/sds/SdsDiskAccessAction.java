package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public abstract class SdsDiskAccessAction extends DiskAccessAction {
	
    public SdsDiskAccessAction(String name, String iconName, String tooltip,
			Shortcut shortcut) {
		super(name, iconName, tooltip, shortcut);
	}

	public static JFileChooser createAndOpenFileChooser(boolean open, boolean multiple, String title) {
        String curDir = Main.pref.get("lastDirectory");
        if (curDir.equals("")) {
            curDir = ".";
        }
        JFileChooser fc = new JFileChooser(new File(curDir));
        if (title != null) {
            fc.setDialogTitle(title);
        }

        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(multiple);
        fc.setAcceptAllFileFilterUsed(false);
        		
        fc.setFileFilter(new FileFilter() {
        	public boolean accept(File pathname) { return pathname.getName().endsWith(".sds") || pathname.isDirectory(); }
			public String getDescription() { return (tr("SDS data file")); }
        });     

        int answer = open ? fc.showOpenDialog(Main.parent) : fc.showSaveDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir)) {
            Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());
        }

        if (!open) {
            File file = fc.getSelectedFile();
            if (file != null && file.exists()) {
                ExtendedDialog dialog = new ExtendedDialog(
                        Main.parent,
                        tr("Overwrite"),
                        new String[] {tr("Overwrite"), tr("Cancel")}
                );
                dialog.setContent(tr("File exists. Overwrite?"));
                dialog.setButtonIcons(new String[] {"save_as.png", "cancel.png"});
                dialog.showDialog();
                if (dialog.getValue() != 1)
                    return null;
            }
        }

        return fc;
    }

    public static File createAndOpenSaveFileChooser(String title) {
        String curDir = Main.pref.get("lastDirectory");
        if (curDir.equals("")) {
            curDir = ".";
        }
        JFileChooser fc = new JFileChooser(new File(curDir));
        if (title != null) {
            fc.setDialogTitle(title);
        }

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        
        fc.setFileFilter(new FileFilter() {
        	public boolean accept(File pathname) { return pathname.getName().endsWith(".sds") || pathname.isDirectory(); }
			public String getDescription() { return (tr("SDS data file")); }
        }); 
        
        int answer = fc.showSaveDialog(Main.parent);
        if (answer != JFileChooser.APPROVE_OPTION)
            return null;

        if (!fc.getCurrentDirectory().getAbsolutePath().equals(curDir)) {
            Main.pref.put("lastDirectory", fc.getCurrentDirectory().getAbsolutePath());
        }

        File file = fc.getSelectedFile();
        String fn = file.getPath();

        if (!confirmOverwrite(file))
            return null;
        return file;
    }
    
    public static boolean confirmOverwrite(File file) {
        if (file == null || (file.exists())) {
            ExtendedDialog dialog = new ExtendedDialog(
                    Main.parent,
                    tr("Overwrite"),
                    new String[] {tr("Overwrite"), tr("Cancel")}
            );
            dialog.setContent(tr("File exists. Overwrite?"));
            dialog.setButtonIcons(new String[] {"save_as.png", "cancel.png"});
            dialog.showDialog();
            return (dialog.getValue() == 1);
        }
        return true;
    }
}
