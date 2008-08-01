package org.openstreetmap.josm.plugins.remotecontrol;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.*;

import javax.swing.*;

import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.remotecontrol.Util.Version;
import org.openstreetmap.josm.tools.I18n;

/**
 * Preference settings for the Remote Control plugin
 * 
 * @author Frederik Ramm
 */
public class RemoteControlPreferences implements PreferenceSetting
{

    
    public void addGui( final PreferenceDialog gui ) 
    {
 
		Version ver = Util.getVersion();
		String description = tr("A plugin that allows JOSM to be controlled from other applications.");
		if( ver != null )
			description += "<br><br>" + tr("Version: {0}<br>Last change at {1}", ver.revision, ver.time) + "<br><br>";
    	JPanel ywms = gui.createPreferenceTab("remotecontrol.gif", I18n.tr("Remote Control"), description + I18n.tr("Settings for the Remote Control plugin."));
    	ywms.add(new JLabel("no prefs yet."));
    }
    
    public void ok() {
    }

    /**
     * ActionListener for the configuration of WMS plugin  
     * @author frsantos
     */
    private final class RemoteControlConfigurationActionListener implements ActionListener, FocusListener
    {
    	/** If the action is already handled */
        boolean alreadyHandled = false;
        public void actionPerformed(ActionEvent e) {
            if(!alreadyHandled)
                configureRemoteControlPluginPreferences();
            alreadyHandled = true;
        }

        public void focusGained(FocusEvent e) {
            alreadyHandled = false;
        }

        public void focusLost(FocusEvent e) {
            if(!alreadyHandled)
                configureRemoteControlPluginPreferences();
            alreadyHandled = true;
        }
    }
    
    
    /**
     * Configures Remote Control 
     */
    private void configureRemoteControlPluginPreferences()
    {
    }
}
