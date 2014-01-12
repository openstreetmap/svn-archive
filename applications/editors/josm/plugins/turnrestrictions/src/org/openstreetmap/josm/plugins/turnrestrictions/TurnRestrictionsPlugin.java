package org.openstreetmap.josm.plugins.turnrestrictions;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.turnrestrictions.list.TurnRestrictionsListDialog;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceEditor;

/**
 * This is the main class for the turnrestrictions plugin.
 * 
 */
public class TurnRestrictionsPlugin extends Plugin{
    
    public TurnRestrictionsPlugin(PluginInformation info) {
        super(info);        
    }
    
    /**
     * Called when the JOSM map frame is created or destroyed. 
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {             
        if (oldFrame == null && newFrame != null) { // map frame added
            TurnRestrictionsListDialog dialog  = new TurnRestrictionsListDialog();
            // add the dialog
            newFrame.addToggleDialog(dialog);
            CreateOrEditTurnRestrictionAction.getInstance();
        }
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new PreferenceEditor();
    }
}
