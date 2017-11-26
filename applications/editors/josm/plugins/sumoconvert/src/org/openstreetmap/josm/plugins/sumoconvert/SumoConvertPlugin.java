package org.openstreetmap.josm.plugins.sumoconvert;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * This is the main class for the sumoconvert plugin.
 * 
 */
public class SumoConvertPlugin extends Plugin{
    
	private SumoExportAction exportAction;
	
    public SumoConvertPlugin(PluginInformation info) {
        super(info);
        exportAction = new SumoExportAction();
        MainApplication.getMenu().toolsMenu.add(exportAction);
    }
}
