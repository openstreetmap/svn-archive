package org.openstreetmap.josm.plugins.remotecontrol;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.remotecontrol.Util.Version;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;

/**
 * Preference settings for the Remote Control plugin
 * 
 * @author Frederik Ramm
 */
public class RemoteControlPreferences implements PreferenceSetting
{
	private JCheckBox permissionLoadData = new JCheckBox(tr("load data from API"));
	private JCheckBox permissionChangeSelection = new JCheckBox(tr("change the selection"));
	private JCheckBox permissionChangeViewport = new JCheckBox(tr("change the viewport"));
	private JCheckBox alwaysAskUserConfirm = new JCheckBox(tr("confirm all Remote Control actions manually"));
	
    public void addGui(final PreferenceDialog gui) 
    {
		Version ver = Util.getVersion();
		String description = tr("A plugin that allows JOSM to be controlled from other applications.");
		if (ver != null)
			description += "<br><br>" + tr("Version: {0}<br>Last change at {1}", ver.revision, ver.time) + "<br><br>";
    	JPanel remote = gui.createPreferenceTab("remotecontrol.gif", tr("Remote Control"), tr("Settings for the Remote Control plugin."));    
    	remote.add(new JLabel("<html>"+tr("The Remote Control plugin will always listen on port 8111 on localhost." + 
    			"The port is not variable because it is referenced by external applications talking to the plugin.") + "</html>"), GBC.eol().insets(0,5,0,10).fill(GBC.HORIZONTAL));

    	JPanel perms = new JPanel();
    	perms.setLayout(new GridBagLayout());
    	perms.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), tr("Permitted actions")));
        perms.add(permissionLoadData, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionChangeSelection, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        perms.add(permissionChangeViewport, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));       
        remote.add(perms, GBC.eol().fill(GBC.HORIZONTAL));
        
        remote.add(alwaysAskUserConfirm, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        remote.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

        
        permissionLoadData.setSelected(Main.pref.getBoolean("remotecontrol.permission.load-data", true));
        permissionChangeSelection.setSelected(Main.pref.getBoolean("remotecontrol.permission.change-selection", true));
        permissionChangeViewport.setSelected(Main.pref.getBoolean("remotecontrol.permission.change-viewport", true));
        alwaysAskUserConfirm.setSelected(Main.pref.getBoolean("remotecontrol.always-confirm", false));
        
    }
    
    public void ok() {
    	Main.pref.put("remotecontrol.permission.load-data", permissionLoadData.isSelected());
    	Main.pref.put("remotecontrol.permission.change-selection", permissionChangeSelection.isSelected());
    	Main.pref.put("remotecontrol.permission.change-viewport", permissionChangeViewport.isSelected());
    	Main.pref.put("remotecontrol.always-confirm", alwaysAskUserConfirm.isSelected());
    		
    }
}
